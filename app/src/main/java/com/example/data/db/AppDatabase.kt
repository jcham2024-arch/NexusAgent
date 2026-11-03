package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val provider: String,
    val modelName: String,
    val currentPersona: String,
    val workspacePath: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val role: String,
    val content: String,
    val thoughts: String?,
    val currentPhase: String?,
    val codeSnippetsJson: String,
    val testRunSummaryJson: String?,
    val gitCommitHash: String?,
    val timestamp: Long
)

@Entity(tableName = "claude_skills")
data class ClaudeSkillEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val sourceUrl: String,
    val rawMarkdown: String,
    val guidelines: String,
    val instructions: String,
    val targetFolder: String,
    val isActive: Boolean
)

@Dao
interface NexusDao {
    @Query("SELECT * FROM sessions ORDER BY updatedAt DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM claude_skills ORDER BY name ASC")
    fun getAllSkills(): Flow<List<ClaudeSkillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: ClaudeSkillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<ClaudeSkillEntity>)
}

@Database(
    entities = [SessionEntity::class, MessageEntity::class, ClaudeSkillEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun nexusDao(): NexusDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nexus_agent.db"
                ).build().also { instance = it }
            }
        }
    }
}
