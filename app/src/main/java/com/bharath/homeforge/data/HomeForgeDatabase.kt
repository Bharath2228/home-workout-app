package com.bharath.homeforge.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WorkoutSession::class, LoggedSet::class], version = 1, exportSchema = false)
abstract class HomeForgeDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var instance: HomeForgeDatabase? = null

        fun get(context: Context): HomeForgeDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    HomeForgeDatabase::class.java,
                    "homeforge.db",
                ).build().also { instance = it }
            }
    }
}
