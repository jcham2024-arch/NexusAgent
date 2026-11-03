package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.git.GitRepositoryManager
import com.example.core.model.AttachedFile
import com.example.core.model.ChatSession
import com.example.core.model.ClaudeSkill
import com.example.core.model.FolderPersona
import com.example.core.model.GitCommitSnapshot
import com.example.core.model.MessageRole
import com.example.core.model.ModelProvider
import com.example.core.model.ScriptCatalogEntry
import com.example.core.model.SessionMessage
import com.example.core.model.SwarmPhase
import com.example.core.model.TestRunSummary
import com.example.core.orchestrator.SwarmOrchestrator
import com.example.core.orchestrator.SwarmProgressEvent
import com.example.core.sandbox.EmbeddedScriptRunner
import com.example.core.skills.ClaudeSkillBroker
import com.example.core.strategy.ModelRouterRegistry
import com.example.core.workspace.WorkspaceManager
import com.example.data.db.AppDatabase
import com.example.data.repository.NexusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NexusUiState(
    val currentSession: ChatSession? = null,
    val sessions: List<ChatSession> = emptyList(),
    val messages: List<SessionMessage> = emptyList(),
    val activePersona: FolderPersona = FolderPersona.SCRIPTS,
    val activeProvider: ModelProvider = ModelProvider.GEMINI,
    val attachedFiles: List<AttachedFile> = emptyList(),
    val availableSkills: List<ClaudeSkill> = emptyList(),
    val isExecuting: Boolean = false,
    val currentProgressPhase: SwarmPhase? = null,
    val currentProgressDetail: String? = null,
    val telemetryLogs: List<String> = emptyList(),
    val gitCommits: List<GitCommitSnapshot> = emptyList(),
    val scriptCatalog: List<ScriptCatalogEntry> = emptyList(),
    val activeRulesContent: String = "",
    val errorBanner: String? = null
)

class NexusViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val skillBroker = ClaudeSkillBroker()
    private val repository = NexusRepository(db, skillBroker)
    private val workspaceManager = WorkspaceManager(application)
    private val scriptRunner = EmbeddedScriptRunner(application)
    private val gitManager = GitRepositoryManager()
    private val modelRouter = ModelRouterRegistry()
    private val swarmOrchestrator = SwarmOrchestrator(
        modelRouter, skillBroker, workspaceManager, scriptRunner, gitManager
    )

    private val _uiState = MutableStateFlow(NexusUiState())
    val uiState: StateFlow<NexusUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
            loadScriptCatalog()
            loadGitCommits()
            updateActiveRules()
        }

        // Collect sessions
        viewModelScope.launch {
            repository.sessions.collect { sessionList ->
                _uiState.update { state ->
                    val current = state.currentSession ?: sessionList.firstOrNull()
                    state.copy(sessions = sessionList, currentSession = current)
                }
                _uiState.value.currentSession?.let { current ->
                    observeMessagesForSession(current.id)
                }
            }
        }

        // Collect skills
        viewModelScope.launch {
            repository.skills.collect { skillList ->
                _uiState.update { it.copy(availableSkills = skillList) }
            }
        }
    }

    private fun observeMessagesForSession(sessionId: String) {
        viewModelScope.launch {
            repository.getMessagesForSession(sessionId).collect { messageList ->
                _uiState.update { it.copy(messages = messageList) }
            }
        }
    }

    fun sendMessage(prompt: String) {
        if (prompt.isBlank() || _uiState.value.isExecuting) return

        val session = _uiState.value.currentSession ?: return
        val persona = _uiState.value.activePersona
        val provider = _uiState.value.activeProvider
        val attached = _uiState.value.attachedFiles

        viewModelScope.launch {
            // Save user message
            val userMsg = SessionMessage(
                sessionId = session.id,
                role = MessageRole.USER,
                content = prompt,
                attachedFiles = attached
            )
            repository.saveMessage(userMsg)

            // Clear attachments & prepare execution state
            _uiState.update {
                it.copy(
                    attachedFiles = emptyList(),
                    isExecuting = true,
                    currentProgressPhase = SwarmPhase.BLUEPRINTING,
                    currentProgressDetail = "Initializing autonomous swarm pipeline...",
                    telemetryLogs = emptyList(),
                    errorBanner = null
                )
            }

            try {
                swarmOrchestrator.executeSwarmTask(
                    prompt = prompt,
                    sessionId = session.id,
                    provider = provider,
                    activePersona = persona,
                    attachedFiles = attached,
                    history = _uiState.value.messages
                ).collect { event ->
                    when (event) {
                        is SwarmProgressEvent.PhaseStarted -> {
                            _uiState.update {
                                it.copy(
                                    currentProgressPhase = event.phase,
                                    currentProgressDetail = event.detail
                                )
                            }
                        }
                        is SwarmProgressEvent.Telemetry -> {
                            _uiState.update {
                                it.copy(telemetryLogs = it.telemetryLogs + "[${event.phase.name}] ${event.message}")
                            }
                        }
                        is SwarmProgressEvent.Completed -> {
                            repository.saveMessage(event.message)
                            loadGitCommits()
                            _uiState.update {
                                it.copy(
                                    isExecuting = false,
                                    currentProgressPhase = null,
                                    currentProgressDetail = null
                                )
                            }
                        }
                        is SwarmProgressEvent.Error -> {
                            _uiState.update {
                                it.copy(
                                    isExecuting = false,
                                    errorBanner = event.throwable.message ?: "Task execution error"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExecuting = false,
                        errorBanner = "Execution exception: ${e.message}"
                    )
                }
            }
        }
    }

    fun createNewSession(title: String = "Autonomous Task") {
        viewModelScope.launch {
            val newSession = ChatSession(
                title = title,
                provider = _uiState.value.activeProvider,
                currentPersona = _uiState.value.activePersona
            )
            repository.saveSession(newSession)
            switchSession(newSession)
        }
    }

    fun switchSession(session: ChatSession) {
        _uiState.update {
            it.copy(
                currentSession = session,
                activeProvider = session.provider,
                activePersona = session.currentPersona
            )
        }
        observeMessagesForSession(session.id)
        updateActiveRules()
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    fun setProvider(provider: ModelProvider) {
        _uiState.update { it.copy(activeProvider = provider) }
        _uiState.value.currentSession?.let { session ->
            val updated = session.copy(provider = provider, modelName = provider.defaultModel)
            viewModelScope.launch { repository.saveSession(updated) }
        }
    }

    fun setPersona(persona: FolderPersona) {
        _uiState.update { it.copy(activePersona = persona) }
        _uiState.value.currentSession?.let { session ->
            val updated = session.copy(currentPersona = persona)
            viewModelScope.launch { repository.saveSession(updated) }
        }
        updateActiveRules()
    }

    fun attachFile(name: String, mimeType: String, content: String) {
        val file = AttachedFile(name, mimeType, content)
        _uiState.update { it.copy(attachedFiles = it.attachedFiles + file) }
    }

    fun removeAttachedFile(name: String) {
        _uiState.update { state ->
            state.copy(attachedFiles = state.attachedFiles.filterNot { it.name == name })
        }
    }

    fun importSkillFromGit(url: String) {
        viewModelScope.launch {
            val result = skillBroker.fetchSkillFromGit(url)
            if (result.isSuccess) {
                repository.saveSkill(result.getOrThrow())
            } else {
                _uiState.update {
                    it.copy(errorBanner = "Failed to fetch skill: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    fun toggleSkillActive(skill: ClaudeSkill) {
        viewModelScope.launch {
            val updated = skill.copy(isActive = !skill.isActive)
            repository.saveSkill(updated)
        }
    }

    private fun updateActiveRules() {
        val rules = workspaceManager.getAgentRulesForPersona(_uiState.value.activePersona)
        _uiState.update { it.copy(activeRulesContent = rules) }
    }

    private fun loadScriptCatalog() {
        val catalog = workspaceManager.getScriptCatalog()
        _uiState.update { it.copy(scriptCatalog = catalog) }
    }

    private fun loadGitCommits() {
        val commits = gitManager.getCommitHistory()
        _uiState.update { it.copy(gitCommits = commits) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorBanner = null) }
    }

    fun exportWorkspace(outputStream: java.io.OutputStream) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                workspaceManager.exportWorkspaceZip(outputStream)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorBanner = "Export failed: ${e.message}") }
            }
        }
    }
}
