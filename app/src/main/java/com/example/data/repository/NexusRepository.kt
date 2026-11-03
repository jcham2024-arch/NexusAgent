package com.example.data.repository

import com.example.core.model.ChatSession
import com.example.core.model.ClaudeSkill
import com.example.core.model.FolderPersona
import com.example.core.model.MessageRole
import com.example.core.model.ModelProvider
import com.example.core.model.SessionMessage
import com.example.core.skills.ClaudeSkillBroker
import com.example.data.db.AppDatabase
import com.example.data.db.ClaudeSkillEntity
import com.example.data.db.MessageEntity
import com.example.data.db.SessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repository pattern implementation managing persistence and domain model mapping.
 */
class NexusRepository(
    private val database: AppDatabase,
    private val skillBroker: ClaudeSkillBroker
) {

    private val dao = database.nexusDao()

    val sessions: Flow<List<ChatSession>> = dao.getAllSessions().map { entities ->
        entities.map { entity ->
            ChatSession(
                id = entity.id,
                title = entity.title,
                provider = ModelProvider.entries.find { it.name == entity.provider } ?: ModelProvider.GEMINI,
                modelName = entity.modelName,
                currentPersona = FolderPersona.entries.find { it.name == entity.currentPersona } ?: FolderPersona.SCRIPTS,
                workspacePath = entity.workspacePath,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }

    val skills: Flow<List<ClaudeSkill>> = dao.getAllSkills().map { entities ->
        entities.map { entity ->
            ClaudeSkill(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                sourceUrl = entity.sourceUrl,
                rawMarkdown = entity.rawMarkdown,
                guidelines = entity.guidelines,
                instructions = entity.instructions,
                targetFolder = entity.targetFolder,
                isActive = entity.isActive
            )
        }
    }

    suspend fun saveSession(session: ChatSession) {
        dao.insertSession(
            SessionEntity(
                id = session.id,
                title = session.title,
                provider = session.provider.name,
                modelName = session.modelName,
                currentPersona = session.currentPersona.name,
                workspacePath = session.workspacePath,
                createdAt = session.createdAt,
                updatedAt = session.updatedAt
            )
        )
    }

    suspend fun deleteSession(sessionId: String) {
        dao.deleteSession(sessionId)
    }

    fun getMessagesForSession(sessionId: String): Flow<List<SessionMessage>> {
        return dao.getMessagesForSession(sessionId).map { entities ->
            entities.map { entity ->
                SessionMessage(
                    id = entity.id,
                    sessionId = entity.sessionId,
                    role = MessageRole.entries.find { it.name == entity.role } ?: MessageRole.AGENT,
                    content = entity.content,
                    thoughts = entity.thoughts,
                    gitCommitHash = entity.gitCommitHash,
                    timestamp = entity.timestamp
                )
            }
        }
    }

    suspend fun saveMessage(message: SessionMessage) {
        dao.insertMessage(
            MessageEntity(
                id = message.id,
                sessionId = message.sessionId,
                role = message.role.name,
                content = message.content,
                thoughts = message.thoughts,
                currentPhase = message.currentPhase?.name,
                codeSnippetsJson = JSONArray(message.codeSnippets.map { it.filename }).toString(),
                testRunSummaryJson = message.testRunSummary?.let {
                    JSONObject().put("passed", it.passedTests).put("total", it.totalTests).toString()
                },
                gitCommitHash = message.gitCommitHash,
                timestamp = message.timestamp
            )
        )
    }

    suspend fun saveSkill(skill: ClaudeSkill) {
        dao.insertSkill(
            ClaudeSkillEntity(
                id = skill.id,
                name = skill.name,
                description = skill.description,
                sourceUrl = skill.sourceUrl,
                rawMarkdown = skill.rawMarkdown,
                guidelines = skill.guidelines,
                instructions = skill.instructions,
                targetFolder = skill.targetFolder,
                isActive = skill.isActive
            )
        )
    }

    /**
     * Seeds initial database state if empty.
     */
    suspend fun seedInitialDataIfNeeded() {
        // Seed 17 Termux production skills
        val preSeeded = skillBroker.getPreSeededTermuxSkills().map { skill ->
            ClaudeSkillEntity(
                id = skill.id,
                name = skill.name,
                description = skill.description,
                sourceUrl = skill.sourceUrl,
                rawMarkdown = skill.rawMarkdown,
                guidelines = skill.guidelines,
                instructions = skill.instructions,
                targetFolder = skill.targetFolder,
                isActive = skill.isActive
            )
        }
        dao.insertSkills(preSeeded)

        // Seed default session
        val defaultSession = ChatSession(
            title = "Autonomous System Evolution",
            provider = ModelProvider.GEMINI,
            modelName = "gemini-3.5-flash",
            currentPersona = FolderPersona.SCRIPTS
        )
        saveSession(defaultSession)

        // Seed welcome message
        saveMessage(
            SessionMessage(
                sessionId = defaultSession.id,
                role = MessageRole.AGENT,
                content = """
                    # 🚀 NexusAgent Core Engine Resumed
                    
                    Autonomous agent platform initialized with native Kotlin/Java engine and embedded TypeScript runtime.
                    
                    ### 📦 Active Systems:
                    - **17 Termux Skills Pre-seeded:** Exa Search, Task Spawner, PM Planner, Peer Reviewer, Self Healer, Wiki Engine, and more.
                    - **Folder-Scoped Governance:** Dedicated `agentrules.md` (≤200 lines) for `research/`, `scripts/`, `production/`, and `vault/`.
                    - **Zero Script Duplication:** `scripts/INDEX.md` catalog active.
                    - **Multi-API Router:** Gemini 3.5 Flash (Free lane) with Groq & OpenRouter automatic 429 failover.
                    - **Git Version Control:** Commit diffs and local unit test validation enabled.
                    
                    Select a folder persona above or enter a task to begin!
                """.trimIndent(),
                thoughts = "Platform ready. Workspace sandboxed under workspace/sessions/."
            )
        )
    }
}
