package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    TaskEntity::class,
    SubtaskEntity::class,
    HabitEntity::class,
    HabitCompletionRecordEntity::class,
    SettingsEntity::class
  ],
  version = 3,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun taskDao(): TaskDao
  abstract fun subtaskDao(): SubtaskDao
  abstract fun habitDao(): HabitDao
  abstract fun habitCompletionRecordDao(): HabitCompletionRecordDao
  abstract fun settingsDao(): SettingsDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
      override fun migrate(db: SupportSQLiteDatabase) {
        try {
          db.execSQL("ALTER TABLE tasks ADD COLUMN isShortTerm INTEGER NOT NULL DEFAULT 0")
        } catch (e: Exception) {
          com.example.util.SafeLog.w("AppDatabase", "MIGRATION_2_3: isShortTerm column already exists or error", e)
        }
      }
    }

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "habit_task_database"
        )
          .addCallback(DatabaseCallback())
          .addMigrations(MIGRATION_2_3)
          .fallbackToDestructiveMigration(true)
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        insertDefaultSettingsSql(db)
      }

      override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
        super.onDestructiveMigration(db)
        insertDefaultSettingsSql(db)
      }

      private fun insertDefaultSettingsSql(db: SupportSQLiteDatabase) {
        try {
          db.execSQL(
            """
            INSERT OR REPLACE INTO app_settings (
              id, pushNotificationsEnabled, taskRemindersEnabled, habitRemindersEnabled,
              soundAlertsEnabled, dailySummaryEnabled, dailySummaryTime, defaultReminderMinutesBefore,
              defaultHabitReminderTime, autoMarkTaskOnSubtasksDone, themeMode, hasPromptedPermissions,
              calendarDefaultView, startOfWeek, showCompletedTasksInCalendar, showCompletedHabitsInCalendar,
              showOverdueInCalendar, timeFormat24Hour
            ) VALUES (
              1, 1, 1, 1, 0, 1, '20:00', 15, '08:00', 0, 'SYSTEM', 0, 'MONTH', 'MONDAY', 1, 1, 1, 0
            )
            """.trimIndent()
          )
        } catch (e: Exception) {
          com.example.util.SafeLog.w("AppDatabase", "Settings migration fallback", e)
        }
      }
    }

    suspend fun populateInitialData(database: AppDatabase) {
      // Clean start with ZERO demo data
      database.settingsDao().insertOrUpdateSettings(SettingsEntity())
    }
  }
}
