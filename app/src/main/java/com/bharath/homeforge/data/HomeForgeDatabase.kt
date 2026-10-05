package com.bharath.homeforge.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [WorkoutSession::class, LoggedSet::class, Measurement::class],
    version = 2,
    exportSchema = false,
)
abstract class HomeForgeDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var instance: HomeForgeDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `measurements` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`time` INTEGER NOT NULL, " +
                        "`type` TEXT NOT NULL, " +
                        "`value` REAL NOT NULL)",
                )
            }
        }

        fun get(context: Context): HomeForgeDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    HomeForgeDatabase::class.java,
                    "homeforge.db",
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
