package com.example.searchpro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WebBookmarkEntity::class, WebHistoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class SearchDatabase : RoomDatabase() {

    abstract fun webBookmarkDao(): WebBookmarkDao
    abstract fun webHistoryDao(): WebHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: SearchDatabase? = null

        fun getInstance(context: Context): SearchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SearchDatabase::class.java,
                    "searchpro_browser_database"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
