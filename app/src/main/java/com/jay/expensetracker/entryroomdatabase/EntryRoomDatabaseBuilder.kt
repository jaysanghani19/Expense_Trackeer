package com.jay.expensetracker.entryroomdatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EntryRoomDatabase::class], version = 2, exportSchema = false)
abstract class EntryRoomDatabaseBuilder : RoomDatabase() {
    //    dao for the EntryRoomDatabase
    abstract fun getEntryDatabaseDao(): EntryRoomDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: EntryRoomDatabaseBuilder? = null

        //        Instance of the EntryRoomDatabase
        fun getInstance(context: Context): EntryRoomDatabaseBuilder {
            var instance = INSTANCE
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    EntryRoomDatabaseBuilder::class.java,
                    "entrydatabase"
                ).build()
            }
            return instance
        }
    }
}