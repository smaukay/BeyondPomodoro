package com.example.beyondpomodoro.sessiontype

import androidx.room.*

@Entity
data class Session (
    @ColumnInfo(name = "session_time") var sessionTime: Int?,
    @ColumnInfo(name = "break_time") var breakTime: Int?,
    @ColumnInfo(name = "used_at") var usedAt: Long?,
    @ColumnInfo(name = "tags") var tags: List<String>?,
    @PrimaryKey(autoGenerate = true) val sid: Int = 0
) {
    constructor(): this(1500, 300, null, null)
}

class Converters {
    @TypeConverter
    fun toString(tags: List<String>): String {
        return tags.reduce { acc, s ->
            "$acc<TAGSEP>$s"
        }
    }

    @TypeConverter
    fun fromString(s: String): List<String> {
        return s.split("<TAGSEP>")
    }
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM session WHERE used_at = (select MAX(s.used_at) from session as s)")
    suspend fun getLatestSession(): Session

    @Insert
    suspend fun addSession(session: Session)

    @Update
    fun updateSession(session: Session)

    @Query("SELECT * FROM session ORDER BY used_at")
    fun getSessions(): List<Session>
}

@Database(entities = arrayOf(Session::class), version = 1)
@TypeConverters(Converters::class)
abstract class SessionDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}
