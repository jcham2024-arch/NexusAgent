package com.example.core.skills

import com.example.core.model.ClaudeSkill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Pattern

/**
 * Ingests, parses, and formats Claude skills (SKILL.md) for dynamic system prompt injection.
 * Fully supports raw Git URLs and local Markdown skill schemas.
 */
class ClaudeSkillBroker(
    private val client: OkHttpClient = OkHttpClient()
) {

    /**
     * Parses a raw SKILL.md markdown text into a typed [ClaudeSkill] object.
     * Extracts YAML frontmatter (name, description) and instructions.
     */
    fun parseSkillMarkdown(rawMarkdown: String, sourceUrl: String = "", targetFolder: String = "scripts"): ClaudeSkill {
        var name = "CustomSkill"
        var description = "Custom imported skill"
        var guidelines = ""
        var instructions = rawMarkdown

        val frontmatterPattern = Pattern.compile("^---\\s*\\n([\\s\\S]*?)\\n---\\s*\\n([\\s\\S]*)$")
        val matcher = frontmatterPattern.matcher(rawMarkdown.trim())

        if (matcher.find()) {
            val frontmatter = matcher.group(1) ?: ""
            instructions = matcher.group(2) ?: ""

            // Extract name
            val namePattern = Pattern.compile("^name:\\s*[\"']?([^\"'\\n]+)[\"']?", Pattern.MULTILINE)
            val nameMatcher = namePattern.matcher(frontmatter)
            if (nameMatcher.find()) {
                name = nameMatcher.group(1)?.trim() ?: name
            }

            // Extract description
            val descPattern = Pattern.compile("^description:\\s*[>|]?\\s*([\\s\\S]*?)(?:\\n[a-zA-Z0-9_-]+:|$)", Pattern.MULTILINE)
            val descMatcher = descPattern.matcher(frontmatter)
            if (descMatcher.find()) {
                description = descMatcher.group(1)?.replace("\n", " ")?.trim() ?: description
            }
        }

        return ClaudeSkill(
            name = name,
            description = description,
            sourceUrl = sourceUrl,
            rawMarkdown = rawMarkdown,
            guidelines = guidelines,
            instructions = instructions,
            targetFolder = targetFolder,
            isActive = true
        )
    }

    /**
     * Fetches a Claude skill directly from a GitHub or GitLab raw URL.
     */
    suspend fun fetchSkillFromGit(url: String): Result<ClaudeSkill> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP error ${response.code} fetching skill"))
                }
                val body = response.body?.string() ?: ""
                val skill = parseSkillMarkdown(body, sourceUrl = url)
                Result.success(skill)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Formats a list of active skills into an XML-style `<available_skills>` prompt block
     * adhering to Claude agent system prompting conventions.
     */
    fun buildSystemPromptInjection(skills: List<ClaudeSkill>): String {
        if (skills.isEmpty()) return ""

        return buildString {
            appendLine("\n<available_skills>")
            skills.filter { it.isActive }.forEach { skill ->
                appendLine("<skill>")
                appendLine("<name>${skill.name}</name>")
                appendLine("<description>${skill.description}</description>")
                appendLine("<instructions>")
                appendLine(skill.instructions.trim())
                appendLine("</instructions>")
                appendLine("</skill>")
            }
            appendLine("</available_skills>")
        }
    }

    /**
     * Returns the 17 pre-seeded production tools directly imported from the user's Termux backend.
     */
    fun getPreSeededTermuxSkills(): List<ClaudeSkill> {
        return listOf(
            ClaudeSkill(
                name = "exa_search_skill",
                description = "High-precision research intelligence and web discovery engine (from skills/exa_search.py).",
                rawMarkdown = "---\nname: exa_search_skill\ndescription: Web intelligence and research discovery engine\n---\nExecute deep web search queries, synthesize citations, and output structured intelligence briefs without writing unneeded code.",
                instructions = "1. Parse search intents. 2. Filter scholarly and technical docs. 3. Output facts with clear markdown citations.",
                targetFolder = "research"
            ),
            ClaudeSkill(
                name = "subagent_worker_spawner",
                description = "Distributes multi-step tasks across modular specialized agent sub-tasks (from skills/agent_team_task_allocator_and_subagent_worker_spawner_logic.sh).",
                rawMarkdown = "---\nname: subagent_worker_spawner\ndescription: Multi-agent swarm worker spawner\n---\nBreaks high-level goals into parallelizable subagent task units.",
                instructions = "Spawn sub-tasks with explicit inputs, outputs, and timeouts. Enforce isolated workspace session IDs.",
                targetFolder = "scripts"
            ),
            ClaudeSkill(
                name = "project_manager_planner",
                description = "Step-by-step master workflow architect for phase 1 blueprinting (from skills/multi_step_project_manager_breakdown_and_task_planner_script.sh).",
                rawMarkdown = "---\nname: project_manager_planner\ndescription: PM blueprinting engine\n---\nDecomposes user objectives into phased milestone specifications.",
                instructions = "Analyze constraints, define acceptance criteria, and plan dependency order.",
                targetFolder = "scripts"
            ),
            ClaudeSkill(
                name = "script_sandbox_inspector",
                description = "Validates TypeScript and shell scripts in an isolated sandbox (from skills/isolated_bash_environment_script_evaluator_and_dependency_sandbox_inspector.sh).",
                rawMarkdown = "---\nname: script_sandbox_inspector\ndescription: Sandbox script dependency inspector\n---\nInspects AST and execution requirements before execution.",
                instructions = "Check imports, environment variables, and execution safety before sandbox execution.",
                targetFolder = "scripts"
            ),
            ClaudeSkill(
                name = "code_peer_reviewer_gatekeeper",
                description = "Automated syntax and security quality gatekeeper (from skills/automated_code_peer_reviewer_and_syntax_security_gatekeeper.sh).",
                rawMarkdown = "---\nname: code_peer_reviewer_gatekeeper\ndescription: Peer reviewer and security gatekeeper\n---\nEnforces zero-regression rules before code lands in production.",
                instructions = "Audit code for security vulnerabilities, type safety, and 100% unit test pass rates.",
                targetFolder = "production"
            ),
            ClaudeSkill(
                name = "self_correction_compiler_loop",
                description = "Captures 429 exceptions, unhandled compiler flags, and applies regex patches (from skills/self_correction_error_handler_and_adversarial_compiler_response_loop.sh).",
                rawMarkdown = "---\nname: self_correction_compiler_loop\ndescription: Compiler error self-correction handler\n---\nIntercepts errors and executes whitespace & regex patch adjustments.",
                instructions = "Catch failure traces, identify exact error line, and synthesize self-healing diff.",
                targetFolder = "scripts"
            ),
            ClaudeSkill(
                name = "memory_condenser_optimizer",
                description = "Condenses multi-turn LLM chat logs to conserve token allocations (from skills/llm_context_memory_condenser_and_chat_log_token_optimization_script.sh).",
                rawMarkdown = "---\nname: memory_condenser_optimizer\ndescription: Chat log token condenser\n---\nCompresses historical turns into compact semantic state vectors.",
                instructions = "Retain decisions and file mutations; summarize conversational boilerplate.",
                targetFolder = "scripts"
            ),
            ClaudeSkill(
                name = "debate_arbitrator_consensus",
                description = "Multi-model voting and consensus arbitrator between Gemini, Groq, and OpenRouter (from skills/multi_agent_multi_model_debate_arbitrator_and_voting_consensus_processor.sh).",
                rawMarkdown = "---\nname: debate_arbitrator_consensus\ndescription: Multi-model debate arbitrator\n---\nEvaluates multiple model outputs and resolves consensus.",
                instructions = "Compare model candidates side-by-side, score logic consistency, and output unified verdict.",
                targetFolder = "scripts"
            ),
            ClaudeSkill(
                name = "regex_bulk_patcher",
                description = "Safe regex file string replacer avoiding 1-byte trimming traps (from skills/regex_file_string_replacer_and_bulk_patcher_tool.sh).",
                rawMarkdown = "---\nname: regex_bulk_patcher\ndescription: Regex bulk file patcher\n---\nPerforms targeted code mutations without truncating full files.",
                instructions = "Target specific unique anchors; verify block balance before applying regex replacements.",
                targetFolder = "scripts"
            ),
            ClaudeSkill(
                name = "json_formatting_extractor",
                description = "Parses complex JSON matrices and extracts clean data schemas (from skills/json_data_parser_and_text_formatting_extraction_script.sh).",
                rawMarkdown = "---\nname: json_formatting_extractor\ndescription: JSON parser and text formatter\n---\nExtracts structured JSON objects from raw markdown code blocks.",
                instructions = "Strip markdown fences natively and validate schema fields against expected interfaces.",
                targetFolder = "scripts"
            ),
            ClaudeSkill(
                name = "skill_catalog_finder",
                description = "Queries scripts/INDEX.md to reuse existing scripts before coding (from skills/skill_finder.sh).",
                rawMarkdown = "---\nname: skill_catalog_finder\ndescription: Semantic skill and script finder\n---\nScans index files to prevent redundant script creation.",
                instructions = "Always search index first. If matching signature exists, invoke it; never write duplicate code.",
                targetFolder = "scripts"
            ),
            ClaudeSkill(
                name = "log_complexity_matrix",
                description = "Calculates character-complexity weighted matrix of logs and code evolution (from skills/log_analyzer.py).",
                rawMarkdown = "---\nname: log_complexity_matrix\ndescription: Code telemetry and complexity analyzer\n---\nParses metadata to track lines, letters, digits, and syntax symbols over time.",
                instructions = "Map file paths to complexity totals to audit autonomous evolution across Git commits.",
                targetFolder = "scripts"
            ),
            ClaudeSkill(
                name = "self_healer_service",
                description = "Hunts down 0-byte or 1-byte corrupted stubs and self-repairs code loops (from skills/self_healer.py).",
                rawMarkdown = "---\nname: self_healer_service\ndescription: Autonomous self-healer for truncated files\n---\nDetects corrupted stubs, consults changelog/git diffs, and restores code.",
                instructions = "Inspect file sizes; if <= 1 byte, retrieve previous Git snapshot and trigger repair.",
                targetFolder = "production"
            ),
            ClaudeSkill(
                name = "obsidian_wiki_engine",
                description = "Compiles markdown frontmatter, [[wiki_tags]], and #pointer code references into Obsidian vault (from skills/wiki_engine.py).",
                rawMarkdown = "---\nname: obsidian_wiki_engine\ndescription: Obsidian vault compiler and pointer resolver\n---\nTransforms instructions into interconnected wiki notes with direct code pointers.",
                instructions = "Maintain [[Note Title]] bidirectional links and #pointer(file.ts:10-35) exact references.",
                targetFolder = "vault"
            ),
            ClaudeSkill(
                name = "gzip_snapshot_backup",
                description = "Generates compressed (.gz) pre-mutation backups before code mutations (from skills/system_backup.sh).",
                rawMarkdown = "---\nname: gzip_snapshot_backup\ndescription: Compressed pre-mutation backup utility\n---\nCreates gzipped restore points before touching production code.",
                instructions = "Always create timestamped snapshot before writing files; enable one-tap rollback.",
                targetFolder = "production"
            ),
            ClaudeSkill(
                name = "session_reviewer_meta_learner",
                description = "Executes background meta-learning passes from successful execution logs (from skills/session_reviewer.sh).",
                rawMarkdown = "---\nname: session_reviewer_meta_learner\ndescription: Background meta-learning reviewer\n---\nSynthesizes new reusable skills from past successful task runs.",
                instructions = "Analyze execution steps, extract generalized algorithm, and register new skill in catalog.",
                targetFolder = "scripts"
            ),
            ClaudeSkill(
                name = "deep_optimizer",
                description = "Refactors code structures for maximum Android runtime performance and minimal token footprints (from skills/deep_optimizer.sh).",
                rawMarkdown = "---\nname: deep_optimizer\ndescription: Code and token footprint optimizer\n---\nApplies OOP principles, strategy decoupling, and dead code elimination.",
                instructions = "Optimize for algorithmic efficiency and clear module boundaries.",
                targetFolder = "production"
            )
        )
    }
}
