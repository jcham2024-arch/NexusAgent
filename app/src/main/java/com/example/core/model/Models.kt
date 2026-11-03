package com.example.core.model

import java.util.UUID

/**
 * Supported Free-Tier Model Providers.
 */
enum class ModelProvider(
    val displayName: String,
    val defaultModel: String,
    val isFreeTierPreferred: Boolean,
    val description: String
) {
    GEMINI(
        displayName = "Google Gemini",
        defaultModel = "gemini-3.5-flash",
        isFreeTierPreferred = true,
        description = "High-speed zero-cost lane for blueprinting, coding & tools"
    ),
    GROQ(
        displayName = "Groq",
        defaultModel = "llama-3.3-70b-versatile",
        isFreeTierPreferred = true,
        description = "Ultra-fast Llama & Mixtral inference engine"
    ),
    OPENROUTER(
        displayName = "OpenRouter",
        defaultModel = "meta-llama/llama-3.2-3b-instruct:free",
        isFreeTierPreferred = false,
        description = "Multi-provider free models with auto-failover"
    )
}

/**
 * 5-Stage Swarm Orchestrator Pipeline Phases.
 */
enum class SwarmPhase(val title: String, val stepNumber: Int) {
    BLUEPRINTING("PM Blueprinting & Task Breakdown", 1),
    SPAWNING("Prompt Spawner & Persona Allocation", 2),
    CODING("Sandbox Coder & TypeScript Generation", 3),
    SECURITY_AUDIT("Peer Review & Unit Test Audit", 4),
    POST_MORTEM("Obsidian Vault Mapping & Git Commit", 5)
}

/**
 * Folder Context Persona for task-specific system prompting.
 */
enum class FolderPersona(
    val folderName: String,
    val personaTitle: String,
    val directive: String
) {
    RESEARCH(
        folderName = "research",
        personaTitle = "Staff Research Analyst",
        directive = "You are a Research Analyst. Synthesize facts, search results, and citations. Do NOT write application code here."
    ),
    SCRIPTS(
        folderName = "scripts",
        personaTitle = "Senior TypeScript Systems Engineer",
        directive = "You are a Systems Engineer. Write clean, typed TypeScript scripts with accompanying unit tests. Check INDEX.md before writing new tools."
    ),
    PRODUCTION(
        folderName = "production",
        personaTitle = "Production Release & QA Architect",
        directive = "You are a QA Architect. Enforce zero-regression policies, test passes, and security checks before creating Git commit diffs."
    ),
    VAULT(
        folderName = "vault",
        personaTitle = "Obsidian Knowledge Curator",
        directive = "You are an Obsidian Ontologist. Manage rules, wikilinks [[tags]], and exact code block pointers #L10-L40."
    )
}

/**
 * Session entity representing an active or saved chat/agent workspace.
 */
data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val provider: ModelProvider = ModelProvider.GEMINI,
    val modelName: String = ModelProvider.GEMINI.defaultModel,
    val currentPersona: FolderPersona = FolderPersona.SCRIPTS,
    val workspacePath: String = "workspace/sessions/${UUID.randomUUID().toString().take(8)}_${ModelProvider.GEMINI.defaultModel}",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Role of message in agent session.
 */
enum class MessageRole {
    USER,
    AGENT,
    SYSTEM,
    TOOL,
    TELEMETRY
}

/**
 * Message within a session containing thought logs, code chunks, and swarm stage status.
 */
data class SessionMessage(
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val role: MessageRole,
    val content: String,
    val thoughts: String? = null,
    val currentPhase: SwarmPhase? = null,
    val codeSnippets: List<CodeSnippet> = emptyList(),
    val attachedFiles: List<AttachedFile> = emptyList(),
    val testRunSummary: TestRunSummary? = null,
    val gitCommitHash: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Extracted or generated code block snippet.
 */
data class CodeSnippet(
    val filename: String,
    val language: String,
    val code: String,
    val isExecutable: Boolean = true
)

/**
 * Uploaded or attached file for context injection.
 */
data class AttachedFile(
    val name: String,
    val mimeType: String,
    val content: String,
    val sizeBytes: Long = content.toByteArray().size.toLong()
)

/**
 * Summary of a local unit test execution pass.
 */
data class TestRunSummary(
    val suiteName: String,
    val totalTests: Int,
    val passedTests: Int,
    val failedTests: Int,
    val durationMs: Long,
    val outputLogs: String
) {
    val isSuccess: Boolean get() = failedTests == 0 && totalTests > 0
}

/**
 * Claude Skill imported from Git or raw Markdown.
 */
data class ClaudeSkill(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val sourceUrl: String = "",
    val rawMarkdown: String,
    val guidelines: String = "",
    val instructions: String = "",
    val targetFolder: String = "scripts",
    val isActive: Boolean = true
)

/**
 * Catalog entry for reusable TypeScript scripts to prevent duplication.
 */
data class ScriptCatalogEntry(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val filename: String,
    val description: String,
    val functionSignature: String,
    val language: String = "TypeScript",
    val hasUnitTest: Boolean = true,
    val path: String
)

/**
 * Local Git commit tracking an autonomous evolution snapshot.
 */
data class GitCommitSnapshot(
    val commitHash: String,
    val message: String,
    val author: String = "NexusAgent <agent@local>",
    val diffSummary: String,
    val filesChanged: Int,
    val linesAdded: Int,
    val linesDeleted: Int,
    val timestamp: Long = System.currentTimeMillis()
)
