# NexusAgent: Autonomous Agentic LLM Platform for Android

An on-device autonomous LLM agent platform engineered in native Kotlin and Java, directly incorporating your active 17-skill Termux production inventory into an enterprise Android workspace. Features the 5-stage swarm orchestrator, embedded TypeScript execution engine, multi-provider model router with 429 failover, folder-scoped `agentrules.md` files (≤200 lines), and local Git version control.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The plan has been updated with the exact Termux Internal Storage path:

### 1. Termux Export to Internal Storage / Download:
On Android, `Internal storage/Download` maps directly to `/storage/emulated/0/Download/` (or `~/storage/downloads/` after running `termux-setup-storage`):
```bash
# 1. Ensure storage permission is enabled in Termux
termux-setup-storage

# 2. Package your workspace with --exclude before directories
cd ~/agent-workspace
tar --exclude='*.pyc' --exclude='__pycache__' -czvf ~/backend_brain_export.tar.gz \
  generate_skill.* \
  swarm_orchestrator.sh \
  skills \
  config \
  CHANGELOG.md

# 3. Copy directly to Internal Storage / Download
cp ~/backend_brain_export.tar.gz /storage/emulated/0/Download/
# OR via the Termux shortcut:
# cp ~/backend_brain_export.tar.gz ~/storage/downloads/

echo "Export complete! File is located in Internal storage/Download/backend_brain_export.tar.gz"
```
*(Note: Even if you do not export from Termux right now, all 17 skills, the 5-stage swarm orchestrator, the 429 self-healing router, and the Obsidian wiki engine are already fully pre-seeded into the Android app's initial workspace!)*

### 2. Direct Mapping of Your 17 Termux Production Skills:
1. **`skills/exa_search.py`** ➔ `ExaSearchSkill` in `workspace/research/` (web intelligence & research analyst persona).
2. **`skills/agent_team_task_allocator_and_subagent_worker_spawner_logic.sh`** ➔ `SubagentWorkerSpawner` (multi-agent swarm task distribution).
3. **`skills/multi_step_project_manager_breakdown_and_task_planner_script.sh`** ➔ `ProjectManagerPlanner` (stage 1 blueprinting engine).
4. **`skills/isolated_bash_environment_script_evaluator_and_dependency_sandbox_inspector.sh`** ➔ `ScriptSandboxInspector` in `workspace/scripts/` (TypeScript sandbox inspector).
5. **`skills/automated_code_peer_reviewer_and_syntax_security_gatekeeper.sh`** ➔ `CodePeerReviewerGatekeeper` in `workspace/production/` (stage 4 audit & QA architect persona).
6. **`skills/self_correction_error_handler_and_adversarial_compiler_response_loop.sh`** ➔ `SelfCorrectionCompilerLoop` (catches 429s, AST syntax errors, whitespace patches).
7. **`skills/llm_context_memory_condenser_and_chat_log_token_optimization_script.sh`** ➔ `MemoryCondenserOptimizer` (trims context, compresses chat turns to preserve token limits).
8. **`skills/multi_agent_multi_model_debate_arbitrator_and_voting_consensus_processor.sh`** ➔ `DebateArbitratorConsensus` (cross-model voting between Gemini, Groq, and OpenRouter).
9. **`skills/regex_file_string_replacer_and_bulk_patcher_tool.sh`** ➔ `RegexBulkPatcherTool` (safe regex string replacer avoiding 1-byte truncation).
10. **`skills/json_data_parser_and_text_formatting_extraction_script.sh`** ➔ `JsonFormattingExtractor` (structured schema extractor).
11. **`skills/skill_finder.sh`** ➔ `SkillCatalogFinder` (queries `INDEX.md` before coding to prevent duplicate scripts).
12. **`skills/log_analyzer.py`** ➔ `LogComplexityMatrix` (character-complexity weighted telemetry tracker).
13. **`skills/self_healer.py`** ➔ `SelfHealerService` (scans for 0/1-byte truncated code, reviews changelogs, self-repairs).
14. **`skills/wiki_engine.py`** ➔ `ObsidianWikiEngine` in `workspace/vault/` (frontmatter blocks, `[[wiki_tags]]`, `#pointer` code links).
15. **`skills/system_backup.sh`** ➔ `GzipSnapshotBackupService` (pre-mutation `.gz` snapshots).
16. **`skills/session_reviewer.sh`** ➔ `SessionReviewerMetaLearner` (post-mortem session learning & skill extraction).
17. **`skills/deep_optimizer.sh`** ➔ `DeepOptimizer` (code refactoring & token minimization).

---

## 1. Overview & Core Concept

### What It Does
NexusAgent transforms an Android smartphone into a native multi-agent workstation. It eliminates Termux bash escaping errors and terminal limitations with a native Kotlin and Jetpack Compose console. It executes tasks across the 5-stage swarm pipeline (`Blueprint ➔ Spawner ➔ Coder ➔ Audit ➔ Post-Mortem`), automatically heals rate limits with model fallback, runs TypeScript code in an embedded sandbox, indexes scripts in `INDEX.md` to prevent duplication, and commits verified code changes to local Git version control.

### Target Audience & Persona
- Power users and developers running multi-agent AI systems on Android who require native responsiveness, zero-permission document attachments, and an auditable Git versioned codebase.

### Key Value
- **Zero Termux overhead**: Native Compose UI with high performance and no battery-draining terminal hang-ups.
- **Pre-packaged 17-Skill Toolkit**: All your custom tools are natively pre-installed and indexed in `INDEX.md`.
- **Bulletproof 429 & Truncation Guards**: Built-in regex strippers and rate-limit circuit breakers prevent the 1-byte trimmer and daily token exhaustion issues.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Agent Workspace Console**:
   - **Top Header Bar**: Active Session Title, Folder / Persona Badge (`[Research Mode - Analyst]`), Model Router Status (`Gemini Flash [Free Lane]`), Provider Switch Menu, and Drawer toggle.
   - **Large Agent Workspace Output Box**: Displays the 5-stage pipeline live:
     - 🔵 Stage 1: Project Manager Blueprinting
     - 🟣 Stage 2: Prompt Spawner & Task Allocation
     - 🟡 Stage 3: Sandbox Coder Execution (TypeScript Sandbox)
     - 🟢 Stage 4: Peer Review & Local Unit Tests
     - ⚪ Stage 5: Post-Mortem & Git Commit
   - **Attachment & Rule Bar**: Active folder `agentrules.md`, attached files, and active Claude skills.
   - **Bottom Input Bar**: Expandable text input field with Send button and task mode selector.
2. **Side Drawer (Workspace & Sessions)**:
   - **Saved Sessions**: Chronological list of sessions with model badges and timestamps.
   - **Folder & Persona Explorer**:
     - `research/` (Exa search, scraper, research summaries)
     - `scripts/` (`agentrules.md`, `INDEX.md`, 17 core skills, unit tests)
     - `production/` (verified code, peer reviews, release builds)
     - `skills/` (Claude skill library from Git)
     - `vault/` (Obsidian wiki notes, `#pointer` links)
   - **Import Termux Archive**: Allows direct one-tap upload of `backend_brain_export.tar.gz`.
   - **Git Version History**: Review commits, line diffs, and test suites.

### Visual Identity & Theme
- **Theme**: Cyber-IDE Developer Theme with Obsidian Slate surfaces and vibrant semantic indicators.
- **Color Palette & Tokens**:
  - `Background`: Obsidian Void `#0D1117`
  - `Surface`: Terminal Card `#161B22`
  - `SurfaceBorder`: Border Stroke `#30363D`
  - `Primary`: Neon Emerald `#10B981` (active state, passed tests, primary buttons)
  - `Secondary`: Cyan Glow `#06B6D4` (research tools, Claude skills, wiki links)
  - `Tertiary`: Electric Amber `#F59E0B` (diff review, warnings, file attachments)
  - `TextPrimary`: High-Contrast White `#F0F6FC`
  - `TextSecondary`: Slate Gray `#8B949E`

---

## 3. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                              NexusAgent UI                             │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ Top Bar: Session Title | Active Persona Badge | Provider Switch  │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ Large Agent Workspace Output (5-Stage Swarm Pipeline Visualizer) │  │
│  │ ├─ Blueprint ➔ ├─ Spawn ➔ ├─ Coder ➔ ├─ Audit ➔ ├─ Post-Mortem   │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ Attachment Chips Bar [agentrules.md] [exa_search] [+ Attach]     │  │
│  ├──────────────────────────────────────────────────────────────────┤  │
│  │ Bottom Input Box [ Enter prompt or autonomous goal...  ] [ Send ]│  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
┌────────────────────────────────────▼───────────────────────────────────┐
│              Collapsible Side Drawer (OOP Workspace Explorer)          │
│  • [+ New Session]                                                     │
│  • Saved Sessions (List with timestamps, model badges, delete)         │
│  • Folder Contexts & Personas:                                         │
│    ├── research/    -> Persona: Researcher | Skills: Exa, Scraper      │
│    ├── scripts/     -> Persona: Engineer   | Skills: TS Sandbox, AST   │
│    ├── production/  -> Persona: QA Arch    | Skills: Git Diff, Review  │
│    ├── skills/      -> Imported Claude SKILL.md library                │
│    └── vault/       -> Persona: Curator    | Skills: Obsidian Pointers │
│  • Git Version History & Diffs                                         │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
┌────────────────────────────────────▼───────────────────────────────────┐
│               NexusAgent Core Architecture (Kotlin & Java)             │
│                                                                        │
│  ┌─────────────────────────┐  ┌──────────────────────────────────────┐ │
│  │ ModelRouterRegistry     │  │ ClaudeSkillBroker                    │ │
│  │ ├─ GeminiStrategy       │  │ ├─ Git / URL Fetcher                 │ │
│  │ ├─ GroqStrategy         │  │ ├─ SKILL.md Parser & Object Builder  │ │
│  │ └─ OpenRouterStrategy   │  │ └─ Dynamic System Prompt Injector    │ │
│  │ └─ 429 Auto-Failover    │  └──────────────────┬───────────────────┘ │
│  └─────────────┬───────────┘                     │                     │
│                │                                 │                     │
│  ┌─────────────▼─────────────────────────────────▼───────────────────┐ │
│  │                 5-Stage Swarm Orchestrator Engine                 │ │
│  │  1. Blueprinting (ProjectManagerPlanner, SubagentWorkerSpawner)   │ │
│  │  2. Prompt Spawner (Persona binding + skill pointer resolution)   │ │
│  │  3. Sandbox Coder (TS Sandbox + RegexBulkPatcherTool)             │ │
│  │  4. Security & Quality Audit (CodePeerReviewerGatekeeper + Tests) │ │
│  │  5. Post-Mortem (ObsidianWikiEngine + Git commit diff)            │ │
│  └─────────────────────────────┬─────────────────────────────────────┘ │
│                                │                                       │
│  ┌─────────────────────────────▼─────────────────────────────────────┐ │
│  │  SelfHealerService       │ LogComplexityMatrix │ Git Engine       │ │
│  │  (0/1-byte fix & repair) │ (Evolution metrics) │ (Commit diffs)   │ │
│  └─────────────────────────────┬─────────────────────────────────────┘ │
│                                │                                       │
│  ┌─────────────────────────────▼─────────────────────────────────────┐ │
│  │      Room Local Database (Sessions, Logs, Skills, Wiki Vault)     │ │
│  └───────────────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
```
