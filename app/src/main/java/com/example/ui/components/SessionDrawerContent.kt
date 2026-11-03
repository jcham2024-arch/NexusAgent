package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.ChatSession
import com.example.core.model.ClaudeSkill
import com.example.core.model.FolderPersona
import com.example.core.model.GitCommitSnapshot
import com.example.core.model.ScriptCatalogEntry
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Collapsible side panel managing Sessions, Folder Personas & Rules, Claude Skills, and Git Diffs.
 */
@Composable
fun SessionDrawerContent(
    sessions: List<ChatSession>,
    currentSession: ChatSession?,
    activePersona: FolderPersona,
    skills: List<ClaudeSkill>,
    scriptCatalog: List<ScriptCatalogEntry>,
    gitCommits: List<GitCommitSnapshot>,
    activeRulesContent: String,
    onSelectSession: (ChatSession) -> Unit,
    onNewSession: () -> Unit,
    onDeleteSession: (String) -> Unit,
    onSelectPersona: (FolderPersona) -> Unit,
    onToggleSkill: (ClaudeSkill) -> Unit,
    onImportSkillClick: () -> Unit,
    onViewDiffClick: (GitCommitSnapshot) -> Unit,
    onExportWorkspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Sessions", "Workspace", "Skills", "Git Log")

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(TerminalSurface)
            .padding(14.dp)
            .testTag("session_drawer")
    ) {
        // App Identity Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(NeonEmerald.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .border(1.dp, NeonEmerald, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "NexusAgent",
                    tint = NeonEmerald,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "NexusAgent Core",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "v2.0 • Native Engine",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                )
            }
        }

        // New Session & Export Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onNewSession,
                modifier = Modifier
                    .weight(1f)
                    .testTag("new_session_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New", tint = TerminalSurface, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New", color = TerminalSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onExportWorkspace,
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("export_workspace_button"),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanGlow),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = "Export", tint = CyanGlow, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export ZIP", color = CyanGlow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = TerminalSurfaceVariant,
            contentColor = NeonEmerald,
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalSurfaceVariant, RoundedCornerShape(8.dp))
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) NeonEmerald else TextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = TerminalBorder)
        Spacer(modifier = Modifier.height(10.dp))

        // Tab Content
        when (selectedTab) {
            0 -> SessionsTab(
                sessions = sessions,
                currentSession = currentSession,
                onSelectSession = onSelectSession,
                onDeleteSession = onDeleteSession
            )
            1 -> WorkspaceTab(
                activePersona = activePersona,
                scriptCatalog = scriptCatalog,
                activeRulesContent = activeRulesContent,
                onSelectPersona = onSelectPersona
            )
            2 -> SkillsTab(
                skills = skills,
                onToggleSkill = onToggleSkill,
                onImportClick = onImportSkillClick
            )
            3 -> GitLogTab(
                gitCommits = gitCommits,
                onViewDiffClick = onViewDiffClick
            )
        }
    }
}

@Composable
fun SessionsTab(
    sessions: List<ChatSession>,
    currentSession: ChatSession?,
    onSelectSession: (ChatSession) -> Unit,
    onDeleteSession: (String) -> Unit
) {
    if (sessions.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
            Text("No saved sessions", color = TextMuted, fontSize = 12.sp)
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sessions, key = { it.id }) { session ->
            val isSelected = session.id == currentSession?.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSession(session) }
                    .testTag("session_item_${session.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) NeonEmerald.copy(alpha = 0.15f) else TerminalSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) NeonEmerald else TerminalBorder)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = session.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = session.provider.displayName,
                                color = CyanGlow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = " • ", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = session.currentPersona.personaTitle.take(16),
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    if (sessions.size > 1) {
                        IconButton(
                            onClick = { onDeleteSession(session.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkspaceTab(
    activePersona: FolderPersona,
    scriptCatalog: List<ScriptCatalogEntry>,
    activeRulesContent: String,
    onSelectPersona: (FolderPersona) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text(
                text = "FOLDER PERSONA DIRECTIVES",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Bold)
            )
        }

        items(FolderPersona.entries) { persona ->
            val isActive = persona == activePersona
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPersona(persona) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) CyanGlow.copy(alpha = 0.15f) else TerminalSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (isActive) CyanGlow else TerminalBorder)
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Folder",
                        tint = if (isActive) CyanGlow else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "workspace/${persona.folderName}/",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = persona.personaTitle,
                            color = if (isActive) CyanGlow else TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ACTIVE RULES (${activePersona.folderName}/agentrules.md)",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TerminalSurfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = activeRulesContent.take(220) + "...",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "INDEXED REUSABLE SCRIPTS (${scriptCatalog.size})",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Bold)
            )
        }

        items(scriptCatalog) { entry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TerminalSurfaceVariant),
                shape = RoundedCornerShape(6.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = entry.name,
                        color = NeonEmerald,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = entry.description,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SkillsTab(
    skills: List<ClaudeSkill>,
    onToggleSkill: (ClaudeSkill) -> Unit,
    onImportClick: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            OutlinedButton(
                onClick = onImportClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanGlow),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanGlow),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = "Import", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Import Skill from Git / URL", fontSize = 12.sp)
            }
        }

        item {
            Text(
                text = "ACTIVE CLAUDE SKILLS (${skills.size})",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Bold)
            )
        }

        items(skills, key = { it.id }) { skill ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleSkill(skill) },
                colors = CardDefaults.cardColors(
                    containerColor = if (skill.isActive) NeonEmerald.copy(alpha = 0.12f) else TerminalSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (skill.isActive) NeonEmerald else TerminalBorder)
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = skill.name,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = skill.description,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 2
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(if (skill.isActive) NeonEmerald else TerminalBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (skill.isActive) {
                            Icon(Icons.Default.Check, contentDescription = "Active", tint = TerminalSurface, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GitLogTab(
    gitCommits: List<GitCommitSnapshot>,
    onViewDiffClick: (GitCommitSnapshot) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text(
                text = "AUTONOMOUS EVOLUTION LOG (${gitCommits.size} COMMITS)",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Bold)
            )
        }

        items(gitCommits, key = { it.commitHash }) { commit ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewDiffClick(commit) },
                colors = CardDefaults.cardColors(containerColor = TerminalSurfaceVariant),
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TerminalBorder))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "commit ${commit.commitHash}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ElectricAmber,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "+${commit.linesAdded} -${commit.linesDeleted}",
                            fontFamily = FontFamily.Monospace,
                            color = NeonEmerald,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = commit.message,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap to inspect diff",
                        color = CyanGlow,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
