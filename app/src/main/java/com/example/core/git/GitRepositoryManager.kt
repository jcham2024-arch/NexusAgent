package com.example.core.git

import com.example.core.model.GitCommitSnapshot
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Local Git repository snapshot and version control manager.
 * Tracks autonomous code mutations, calculates unified line diffs, and enforces auditability.
 */
class GitRepositoryManager {

    private val commits = mutableListOf<GitCommitSnapshot>()

    init {
        // Seed initial commit
        commits.add(
            GitCommitSnapshot(
                commitHash = "e6f2af21a0",
                message = "Initial architecture resumption: Termux 17-skill baseline",
                author = "NexusAgent <agent@local>",
                diffSummary = "+ 17 skills indexed in scripts/INDEX.md\n+ 4 folder agentrules.md created\n+ Model router registry initialized",
                filesChanged = 22,
                linesAdded = 1420,
                linesDeleted = 0,
                timestamp = System.currentTimeMillis() - 86400000L
            )
        )
    }

    /**
     * Returns the full chronological list of Git commits.
     */
    fun getCommitHistory(): List<GitCommitSnapshot> = commits.toList().reversed()

    /**
     * Creates a new Git commit snapshot after a verified unit test pass.
     */
    fun createCommit(
        message: String,
        oldContent: String,
        newContent: String,
        targetFile: String
    ): GitCommitSnapshot {
        val diff = calculateUnifiedDiff(oldContent, newContent, targetFile)
        val hash = generateShortHash(message + System.currentTimeMillis())

        val snapshot = GitCommitSnapshot(
            commitHash = hash,
            message = message,
            author = "NexusAgent <agent@local>",
            diffSummary = diff.diffText,
            filesChanged = 1,
            linesAdded = diff.linesAdded,
            linesDeleted = diff.linesDeleted,
            timestamp = System.currentTimeMillis()
        )

        commits.add(snapshot)
        return snapshot
    }

    /**
     * Calculates line-by-line unified diff between old and new code blocks.
     */
    fun calculateUnifiedDiff(oldText: String, newText: String, filename: String): DiffResult {
        val oldLines = if (oldText.isBlank()) emptyList() else oldText.lines()
        val newLines = if (newText.isBlank()) emptyList() else newText.lines()

        var added = 0
        var deleted = 0
        val sb = StringBuilder()
        sb.appendLine("--- a/$filename")
        sb.appendLine("+++ b/$filename")

        val maxLines = maxOf(oldLines.size, newLines.size)
        for (i in 0 until maxLines) {
            val oldLine = oldLines.getOrNull(i)
            val newLine = newLines.getOrNull(i)

            if (oldLine != null && newLine != null) {
                if (oldLine == newLine) {
                    sb.appendLine("  $oldLine")
                } else {
                    sb.appendLine("- $oldLine")
                    sb.appendLine("+ $newLine")
                    deleted++
                    added++
                }
            } else if (oldLine != null) {
                sb.appendLine("- $oldLine")
                deleted++
            } else if (newLine != null) {
                sb.appendLine("+ $newLine")
                added++
            }
        }

        return DiffResult(sb.toString(), added, deleted)
    }

    data class DiffResult(val diffText: String, val linesAdded: Int, val linesDeleted: Int)

    private fun generateShortHash(seed: String): String {
        val digest = MessageDigest.getInstance("SHA-1").digest(seed.toByteArray())
        return digest.take(5).joinToString("") { "%02x".format(it) }
    }
}
