package com.example.core.workspace

import android.content.Context
import com.example.core.model.FolderPersona
import com.example.core.model.ScriptCatalogEntry
import java.io.File

/**
 * Manages the OOP-partitioned on-device workspace and folder-level agentrules.md files.
 * Enforces strict modularity, session & model identification, and script catalog indexing.
 */
class WorkspaceManager(private val context: Context) {

    private val rootWorkspace: File by lazy {
        File(context.filesDir, "workspace").apply { if (!exists()) mkdirs() }
    }

    init {
        initializeWorkspaceDirectories()
    }

    /**
     * Initializes the standard OOP workspace directories and seeds their respective agentrules.md files (≤ 200 lines).
     */
    fun initializeWorkspaceDirectories() {
        val folders = listOf("scripts", "research", "production", "skills", "vault", "sessions", "telemetry")
        folders.forEach { folderName ->
            val dir = File(rootWorkspace, folderName)
            if (!dir.exists()) dir.mkdirs()
        }

        seedFolderAgentRules()
        seedScriptIndex()
    }

    /**
     * Creates or returns a dedicated sandbox folder for a given session and model identifier.
     */
    fun getSessionSandboxDir(sessionId: String, modelId: String): File {
        val cleanModelId = modelId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val sessionDir = File(rootWorkspace, "sessions/${sessionId}_$cleanModelId")
        if (!sessionDir.exists()) sessionDir.mkdirs()
        return sessionDir
    }

    /**
     * Reads the folder-specific agentrules.md (always kept under 200 lines) for the given persona.
     */
    fun getAgentRulesForPersona(persona: FolderPersona): String {
        val rulesFile = File(rootWorkspace, "${persona.folderName}/agentrules.md")
        return if (rulesFile.exists()) {
            rulesFile.readText()
        } else {
            generateDefaultRulesForPersona(persona)
        }
    }

    /**
     * Returns the list of registered reusable TypeScript scripts from scripts/INDEX.md.
     */
    fun getScriptCatalog(): List<ScriptCatalogEntry> {
        return listOf(
            ScriptCatalogEntry(
                name = "taskExecutor",
                filename = "task_executor.ts",
                description = "Runs autonomous sub-task routines with configurable timeout and error trapping",
                functionSignature = "runAutonomousTask(target: string, params: Record<string, unknown>): Promise<TaskResult>",
                path = "workspace/scripts/task_executor.ts"
            ),
            ScriptCatalogEntry(
                name = "regexStringPatcher",
                filename = "regex_patcher.ts",
                description = "Performs safe multi-line regex replacements avoiding truncation",
                functionSignature = "patchFileContent(content: string, pattern: RegExp, replacement: string): string",
                path = "workspace/scripts/regex_patcher.ts"
            ),
            ScriptCatalogEntry(
                name = "jsonSchemaValidator",
                filename = "json_validator.ts",
                description = "Validates untrusted JSON outputs against expected schemas",
                functionSignature = "validateSchema<T>(rawJson: string, schemaDefinition: Schema): ValidationResult<T>",
                path = "workspace/scripts/json_validator.ts"
            ),
            ScriptCatalogEntry(
                name = "logComplexityParser",
                filename = "complexity_parser.ts",
                description = "Computes complexity metrics for code evolution tracking",
                functionSignature = "computeFileComplexity(sourceCode: string): ComplexityMetrics",
                path = "workspace/scripts/complexity_parser.ts"
            ),
            ScriptCatalogEntry(
                name = "obsidianPointerResolver",
                filename = "pointer_resolver.ts",
                description = "Extracts exact code line blocks (#L12-L45) for Obsidian wiki cards",
                functionSignature = "extractCodeBlockByPointer(filePath: string, startLine: number, endLine: number): string",
                path = "workspace/scripts/pointer_resolver.ts"
            )
        )
    }

    private fun seedFolderAgentRules() {
        FolderPersona.entries.forEach { persona ->
            val folder = File(rootWorkspace, persona.folderName)
            val rulesFile = File(folder, "agentrules.md")
            if (!rulesFile.exists()) {
                rulesFile.writeText(generateDefaultRulesForPersona(persona))
            }
        }
    }

    private fun seedScriptIndex() {
        val indexFile = File(rootWorkspace, "scripts/INDEX.md")
        if (!indexFile.exists()) {
            val content = buildString {
                appendLine("# Reusable TypeScript Script Index")
                appendLine("Always query this index before creating new scripts to prevent code duplication.\n")
                getScriptCatalog().forEach { entry ->
                    appendLine("## `${entry.name}`")
                    appendLine("- **File:** `${entry.filename}`")
                    appendLine("- **Signature:** `${entry.functionSignature}`")
                    appendLine("- **Description:** ${entry.description}")
                    appendLine("- **Tests:** Has accompanying unit test spec.")
                    appendLine()
                }
            }
            indexFile.writeText(content)
        }
    }

    private fun generateDefaultRulesForPersona(persona: FolderPersona): String {
        return when (persona) {
            FolderPersona.RESEARCH -> """
                # Rules for workspace/research/
                Role: Senior Research Intelligence Analyst
                
                ## Principles
                1. You are a research analyst, NOT a programmer. Do not write application code here.
                2. Use the Exa Search and Web Scraper skills to discover and verify factual claims.
                3. Structure all outputs with executive summaries, key findings, and explicit source citations.
                4. Always synthesize findings concisely to minimize context memory usage.
                
                ## Permitted Skills
                - exa_search_skill
                - web_scraper_skill
                - memory_condenser_optimizer
            """.trimIndent()

            FolderPersona.SCRIPTS -> """
                # Rules for workspace/scripts/
                Role: Senior TypeScript Systems Engineer
                
                ## Principles
                1. All scripting must be strictly typed TypeScript adhering to enterprise standards.
                2. NEVER create a new script if a matching tool already exists in `INDEX.md`. Always check `INDEX.md` first.
                3. Every script must be accompanied by a unit test spec (`*.test.ts`) that achieves 100% pass rate in the sandbox.
                4. Register newly verified scripts in `INDEX.md` with complete function signatures.
                
                ## Permitted Skills
                - script_sandbox_inspector
                - regex_bulk_patcher
                - json_formatting_extractor
                - skill_catalog_finder
                - log_complexity_matrix
            """.trimIndent()

            FolderPersona.PRODUCTION -> """
                # Rules for workspace/production/
                Role: Production Release & QA Architect
                
                ## Principles
                1. Zero regressions: Code only enters production if all local unit tests pass.
                2. Audit code for security vulnerabilities, type violations, and memory safety.
                3. Always verify Git commit diffs before signing off on autonomous updates.
                4. Deploy gzip snapshot backups before applying code mutations.
                
                ## Permitted Skills
                - code_peer_reviewer_gatekeeper
                - self_healer_service
                - gzip_snapshot_backup
                - deep_optimizer
            """.trimIndent()

            FolderPersona.VAULT -> """
                # Rules for workspace/vault/
                Role: Obsidian Knowledge Base Curator & Ontologist
                
                ## Principles
                1. Write Obsidian-compatible markdown using frontmatter blocks and [[wiki_links]].
                2. Use targeted code pointers (e.g. `#pointer(module.ts:15-32)`) to reference exact code blocks.
                3. Keep notes atomic and linked in a bidirectional graph to avoid full-file context dumps.
                4. Keep instructional rules and guidelines concise and human-readable.
                
                ## Permitted Skills
                - obsidian_wiki_engine
                - session_reviewer_meta_learner
            """.trimIndent()
        }
    }

    /**
     * Packages the entire workspace into a standard ZIP archive for export.
     */
    fun exportWorkspaceZip(outputStream: java.io.OutputStream) {
        val zipOut = java.util.zip.ZipOutputStream(outputStream)
        try {
            zipDirectory(rootWorkspace, "", zipOut)
        } finally {
            zipOut.close()
        }
    }

    private fun zipDirectory(folder: File, parentFolder: String, zipOut: java.util.zip.ZipOutputStream) {
        val files = folder.listFiles() ?: return
        for (file in files) {
            val entryPath = if (parentFolder.isEmpty()) file.name else "$parentFolder/${file.name}"
            if (file.isDirectory) {
                zipDirectory(file, entryPath, zipOut)
            } else {
                val zipEntry = java.util.zip.ZipEntry(entryPath)
                zipOut.putNextEntry(zipEntry)
                file.inputStream().use { it.copyTo(zipOut) }
                zipOut.closeEntry()
            }
        }
    }
}
