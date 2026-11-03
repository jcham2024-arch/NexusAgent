package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.ModelProvider
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Top App Bar API Provider Switcher Menu.
 * Enables switching between Google Gemini, Groq, and OpenRouter free tiers.
 */
@Composable
fun ApiProviderMenu(
    activeProvider: ModelProvider,
    onSelectProvider: (ModelProvider) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .background(TerminalSurface, RoundedCornerShape(8.dp))
                .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("api_provider_selector"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (activeProvider.isFreeTierPreferred) NeonEmerald else CyanGlow, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = activeProvider.displayName,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Expand",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(TerminalSurface)
                .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
        ) {
            ModelProvider.entries.forEach { provider ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (provider.isFreeTierPreferred) NeonEmerald else CyanGlow, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = provider.displayName,
                                color = if (provider == activeProvider) NeonEmerald else TextPrimary,
                                fontWeight = if (provider == activeProvider) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${provider.defaultModel.take(15)})",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    },
                    onClick = {
                        onSelectProvider(provider)
                        expanded = false
                    },
                    modifier = Modifier.testTag("provider_option_${provider.name}")
                )
            }
        }
    }
}
