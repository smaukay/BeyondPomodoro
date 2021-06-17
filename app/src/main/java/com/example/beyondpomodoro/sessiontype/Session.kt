package com.example.beyondpomodoro.sessiontype

import android.content.Context
import android.content.res.ColorStateList
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Entity
data class Session (
    @ColumnInfo(name = "title") var title: String?,
    @ColumnInfo(name = "session_time") var sessionTime: Int?,
    @ColumnInfo(name = "break_time") var breakTime: Int?,
    @ColumnInfo(name = "used_at") var usedAt: Long?,
    @ColumnInfo(name = "tags") var tags: Set<String>?,
    @ColumnInfo(name = "dnd", defaultValue = "false") var dnd: Boolean = false,
    @PrimaryKey(autoGenerate = true) val sid: Int = 0
) {
}

@Entity
data class Tags (
    @ColumnInfo(name = "colour") var colour: ColorStateList?,
    @ColumnInfo(name = "name") var name: String?,
    @PrimaryKey(autoGenerate = true) val tid: Int = 0
)

data class Title (
    @ColumnInfo(name = "title") var title: String?,
    @PrimaryKey(autoGenerate = true) val sid: Int = 0
)

data class Pomodoro (
    @ColumnInfo(name = "session_time") var sessionTime: Int?,
    @ColumnInfo(name = "used_at") var usedAt: Long?,
    @ColumnInfo(name = "tags") var tags: Set<String>?,
    @ColumnInfo(name = "dnd") var dnd: Boolean = false,
    @PrimaryKey(autoGenerate = true) val sid: Int = 0
)

data class Dnd (
    @ColumnInfo(name = "dnd") var dnd: Boolean,
    @PrimaryKey(autoGenerate = true) val sid: Int = 0
)

data class Break (
    @ColumnInfo(name = "break_time") var breakTime: Int?,
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

    @TypeConverter
    fun fromColorStateList(c: ColorStateList): Int {
        return c.defaultColor
    }

    @TypeConverter
    fun fromInt(i: Int): ColorStateList {
        return ColorStateList.valueOf(i)
    }
}

@Dao
interface TagsDao {
    @Query("SELECT colour FROM tags WHERE name = :name")
    suspend fun getTagColour(name: String): ColorStateList

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTag(t: Tags): Long

    @Query("SELECT COUNT() FROM tags WHERE name = :name")
    suspend fun exists(name: String): Int
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

    @Query("DELETE FROM session WHERE sid = :sid")
    suspend fun removeSession(sid: Int)

    @Query("SELECT title FROM session WHERE sid = :sid")
    fun _getTitle(sid: Int): Flow<String>

    fun getTitle(sid: Int) = _getTitle(sid).distinctUntilChanged()

    @Query("SELECT dnd FROM session WHERE sid = :sid")
    fun _getDnd(sid: Int): Flow<Boolean>

    fun getDnd(sid: Int) = _getDnd(sid).distinctUntilChanged()

    @Query("SELECT * FROM session ORDER BY used_at DESC")
    suspend fun getSessions(): List<Session>

    @Update(entity = Session::class)
    suspend fun updateDnd(d: Dnd)
}

@Database(
    entities = arrayOf(Session::class, Tags::class),
    version = 3,
    autoMigrations = [
        AutoMigration (from = 1, to = 2),
//        AutoMigration (from = 2, to = 3)
                     ],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SessionDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun tagsDao(): TagsDao
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
                        ).addMigrations(MIGRATION_2_3)
                            .build()
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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE `Tags` (`name` TEXT, `colour` INTEGER, `tid` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT); ")
    }
}
