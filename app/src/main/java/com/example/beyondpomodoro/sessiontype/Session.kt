package com.example.beyondpomodoro.sessiontype

import android.content.Context
import androidx.room.*

@Entity
data class Session (
    @ColumnInfo(name = "title") var title: String?,
    @ColumnInfo(name = "session_time") var sessionTime: Int?,
    @ColumnInfo(name = "break_time") var breakTime: Int?,
    @ColumnInfo(name = "used_at") var usedAt: Long?,
    @ColumnInfo(name = "tags") var tags: Set<String>?,
    @PrimaryKey(autoGenerate = true) val sid: Int = 0
) {
    constructor(): this("", 1500, 300, null, null)
}

data class Title (
    @ColumnInfo(name = "title") var title: String?,
    @PrimaryKey(autoGenerate = true) val sid: Int = 0
)

data class Pomodoro (
    @ColumnInfo(name = "title") var title: String?,
    @ColumnInfo(name = "session_time") var sessionTime: Int?,
    @ColumnInfo(name = "used_at") var usedAt: Long?,
    @ColumnInfo(name = "tags") var tags: Set<String>?,
    @PrimaryKey(autoGenerate = true) val sid: Int = 0
)

data class Break (
    @ColumnInfo(name = "break_time") var sessionTime: Int?,
    @PrimaryKey(autoGenerate = true) val sid: Int = 0
)

class Converters {
    @TypeConverter
    fun toString(tags: Set<String>): String {
        return when(tags.size) {
            0 -> ""
            else -> tags.reduce { acc, s ->
                "$acc<TAGSEP>$s"
            }
        }
    }

    @TypeConverter
    fun fromString(s: String): Set<String> {
        return when(s.isNotEmpty()){
            true -> s.split("<TAGSEP>").toSet()
            false -> setOf<String>()
        }
    }
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM session WHERE used_at = (select MAX(s.used_at) from session as s)")
    suspend fun getLatestSession(): Session

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSession(session: Session): Long

    @Update(entity = Session::class)
    suspend fun updatePomodoro(pomodoro: Pomodoro)

    @Update(entity = Session::class)
    suspend fun updateBreak(b: Break)

    @Update(entity = Session::class)
    suspend fun updateTitle(t: Title)

    @Query("SELECT * FROM session WHERE sid = :sid")
    suspend fun getSession(sid: Int): Session

    @Query("SELECT * FROM session ORDER BY used_at")
    suspend fun getSessions(): List<Session>
}

@Database(entities = arrayOf(Session::class), version = 1)
@TypeConverters(Converters::class)
abstract class SessionDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    companion object{
        @Volatile
        private var dbInstance: SessionDatabase? = null
        fun getInstance(context: Context): SessionDatabase {
            synchronized(this) {
                dbInstance = when(dbInstance) {
                    null -> {
                        // create database instance
                        Room.databaseBuilder(
                            context,
                            SessionDatabase::class.java, "session-types"
                        ).build()
                    }
                    else -> {
                        dbInstance!!
                    }
                }
            }
            println("DEBUG: database build complete")
            return dbInstance!!
        }

        fun destroyInstance() {
            when(dbInstance) {
                null -> {}
                else -> {dbInstance?.close()}
            }
            dbInstance = null
        }
    }

}
