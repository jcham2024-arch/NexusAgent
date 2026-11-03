package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.git.GitRepositoryManager
import com.example.core.skills.ClaudeSkillBroker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NexusAgent", appName)
    }

    @Test
    fun `verify claude skill broker parses yaml frontmatter`() {
        val broker = ClaudeSkillBroker()
        val markdown = """
            ---
            name: test_tool
            description: Tool for testing execution
            ---
            # Guidelines
            Execute tests accurately.
        """.trimIndent()
        val skill = broker.parseSkillMarkdown(markdown)
        assertEquals("test_tool", skill.name)
        assertEquals("Tool for testing execution", skill.description)
        assertTrue(skill.instructions.contains("Guidelines"))
    }

    @Test
    fun `verify git repository manager computes line diffs`() {
        val git = GitRepositoryManager()
        val oldCode = "const x = 1;\nconst y = 2;"
        val newCode = "const x = 1;\nconst y = 3;\nconst z = 4;"
        val commit = git.createCommit("Test evolution commit", oldCode, newCode, "test.ts")
        assertTrue(commit.linesAdded >= 2)
        assertEquals("Test evolution commit", commit.message)
    }
}
