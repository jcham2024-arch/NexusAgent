package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.model.GitCommitSnapshot
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Visual unified line diff dialog showing green additions (+) and red deletions (-).
 */
@Composable
fun GitDiffDialog(
    commit: GitCommitSnapshot,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("git_diff_dialog"),
            colors = CardDefaults.cardColors(containerColor = TerminalSurface),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TerminalBorder))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Commit ${commit.commitHash}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricAmber,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Text(
                        text = "+${commit.linesAdded} / -${commit.linesDeleted}",
                        fontFamily = FontFamily.Monospace,
                        color = NeonEmerald,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = commit.message,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Unified diff viewer
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(8.dp)) {
                        items(commit.diffSummary.lines()) { line ->
                            val color = when {
                                line.startsWith("+") -> NeonEmerald
                                line.startsWith("-") -> Color(0xFFEF4444)
                                line.startsWith("@@") -> ElectricAmber
                                else -> TextSecondary
                            }
                            val bg = when {
                                line.startsWith("+") -> NeonEmerald.copy(alpha = 0.08f)
                                line.startsWith("-") -> Color(0xFFEF4444).copy(alpha = 0.08f)
                                else -> Color.Transparent
                            }
                            Text(
                                text = line,
                                color = color,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bg)
                                    .padding(vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricAmber),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close Diff Viewer", color = TerminalSurface, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
