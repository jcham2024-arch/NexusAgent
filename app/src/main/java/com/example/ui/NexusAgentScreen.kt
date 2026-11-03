package com.example.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.GitCommitSnapshot
import com.example.ui.components.AgentWorkConsole
import com.example.ui.components.ApiProviderMenu
import com.example.ui.components.GitDiffDialog
import com.example.ui.components.ImportSkillDialog
import com.example.ui.components.SessionDrawerContent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/**
 * Main application screen featuring the large agent workspace console,
 * session drawer, provider switcher menu, document attachment bar, and bottom prompt field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusAgentScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var inputPrompt by remember { mutableStateOf("") }
    var showImportSkillDialog by remember { mutableStateOf(false) }
    var inspectCommitDiff by remember { mutableStateOf<GitCommitSnapshot?>(null) }

    // Android Storage Document Picker for text/code/json/markdown uploads
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val contentResolver = context.contentResolver
                val filename = it.lastPathSegment?.substringAfterLast('/') ?: "document.txt"
                val content = contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                    reader.readText()
                } ?: ""
                val mimeType = contentResolver.getType(it) ?: "text/plain"
                viewModel.attachFile(filename, mimeType, content)
                Toast.makeText(context, "Attached $filename", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Export Workspace to ZIP launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let {
            try {
                val outputStream = context.contentResolver.openOutputStream(it)
                if (outputStream != null) {
                    viewModel.exportWorkspace(outputStream)
                    Toast.makeText(context, "Workspace exported to selected file", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = TerminalSurface,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                SessionDrawerContent(
                    sessions = uiState.sessions,
                    currentSession = uiState.currentSession,
                    activePersona = uiState.activePersona,
                    skills = uiState.availableSkills,
                    scriptCatalog = uiState.scriptCatalog,
                    gitCommits = uiState.gitCommits,
                    activeRulesContent = uiState.activeRulesContent,
                    onSelectSession = { session ->
                        viewModel.switchSession(session)
                        scope.launch { drawerState.close() }
                    },
                    onNewSession = {
                        viewModel.createNewSession()
                        scope.launch { drawerState.close() }
                    },
                    onDeleteSession = { sessionId ->
                        viewModel.deleteSession(sessionId)
                    },
                    onSelectPersona = { persona ->
                        viewModel.setPersona(persona)
                    },
                    onToggleSkill = { skill ->
                        viewModel.toggleSkillActive(skill)
                    },
                    onImportSkillClick = {
                        showImportSkillDialog = true
                    },
                    onViewDiffClick = { commit ->
                        inspectCommitDiff = commit
                    },
                    onExportWorkspace = {
                        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                        exportLauncher.launch("nexus_workspace_$timestamp.zip")
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = uiState.currentSession?.title ?: "NexusAgent",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                ),
                                maxLines = 1
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(CyanGlow, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${uiState.activePersona.folderName}/ (${uiState.activePersona.personaTitle})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CyanGlow,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("drawer_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = TextPrimary
                            )
                        }
                    },
                    actions = {
                        ApiProviderMenu(
                            activeProvider = uiState.activeProvider,
                            onSelectProvider = { provider ->
                                viewModel.setProvider(provider)
                            }
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = TerminalSurface,
                        titleContentColor = TextPrimary
                    )
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalSurface)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    // File Attachments Bar
                    if (uiState.attachedFiles.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            items(uiState.attachedFiles) { file ->
                                Surface(
                                    color = ElectricAmber.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricAmber.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AttachFile,
                                            contentDescription = "Attachment",
                                            tint = ElectricAmber,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = file.name,
                                            color = ElectricAmber,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = ElectricAmber,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { viewModel.removeAttachedFile(file.name) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Prompt Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // File attachment trigger
                        IconButton(
                            onClick = {
                                filePickerLauncher.launch(
                                    arrayOf("text/*", "application/json", "*/*")
                                )
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("attach_file_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Attach File",
                                tint = if (uiState.attachedFiles.isNotEmpty()) ElectricAmber else TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Text input box
                        OutlinedTextField(
                            value = inputPrompt,
                            onValueChange = { inputPrompt = it },
                            placeholder = {
                                Text(
                                    text = "Instruct agent in ${uiState.activePersona.folderName}/...",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("agent_prompt_input"),
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 4,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = TerminalSurfaceVariant,
                                unfocusedContainerColor = TerminalSurfaceVariant,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedIndicatorColor = NeonEmerald,
                                unfocusedIndicatorColor = TerminalBorder
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Send Action Button
                        IconButton(
                            onClick = {
                                if (inputPrompt.isNotBlank() && !uiState.isExecuting) {
                                    val prompt = inputPrompt
                                    inputPrompt = ""
                                    viewModel.sendMessage(prompt)
                                }
                            },
                            enabled = inputPrompt.isNotBlank() && !uiState.isExecuting,
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (inputPrompt.isNotBlank() && !uiState.isExecuting) NeonEmerald else TerminalSurfaceVariant,
                                    CircleShape
                                )
                                .testTag("send_prompt_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (inputPrompt.isNotBlank() && !uiState.isExecuting) TerminalSurface else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            },
            containerColor = ObsidianBg,
            modifier = modifier
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Error / Warning banner if present
                if (uiState.errorBanner != null) {
                    Surface(
                        color = Color(0xFF7F1D1D),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.errorBanner ?: "",
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.clearError() },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
                            }
                        }
                    }
                }

                // Large Agent Workspace Console
                AgentWorkConsole(
                    messages = uiState.messages,
                    isExecuting = uiState.isExecuting,
                    currentPhase = uiState.currentProgressPhase,
                    currentProgressDetail = uiState.currentProgressDetail,
                    telemetryLogs = uiState.telemetryLogs
                )
            }
        }
    }

    // Dialogs
    if (showImportSkillDialog) {
        ImportSkillDialog(
            onDismiss = { showImportSkillDialog = false },
            onImportUrl = { url ->
                viewModel.importSkillFromGit(url)
            }
        )
    }

    inspectCommitDiff?.let { commit ->
        GitDiffDialog(
            commit = commit,
            onDismiss = { inspectCommitDiff = null }
        )
    }
}
