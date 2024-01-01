package com.jay.expensetracker.entryroomdatabase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

//  Dao of the EntryRoomDatabase
@Dao
interface EntryRoomDatabaseDao {
    //    for inserting the entry
    @Insert
    suspend fun insertEntry(entry: EntryRoomDatabase): Long

    @Update
    suspend fun updateEntry(entry: EntryRoomDatabase)

    @Delete
    suspend fun deleteEntry(entry: EntryRoomDatabase)

    @Query("UPDATE entrydatabase SET entryNumber = :entryNumber WHERE timeStamp = :timeStamp")
    suspend fun updateEntryNumber(entryNumber: Int, timeStamp: String)

    @Query("SELECT timeStamp FROM entrydatabase WHERE entryNumber > :entryNumber ORDER BY entryNumber")
    fun getTimeStamp(entryNumber: Int): List<String>

    //  getting row count
    @Query("SELECT COUNT(*) FROM entrydatabase")
    fun getRowCount(): Int

    //  getting the rows between fromDate and toDate
    @Query("SELECT * FROM entrydatabase WHERE date >= :fromDate AND date <= :toDate")
    fun getEntriesBetweenDates(fromDate: String, toDate: String): LiveData<List<EntryRoomDatabase>>

    //  for getting entries on particular date
    @Query("SELECT * FROM entrydatabase WHERE date = :date")
    fun getTodaysEntries(date: String): LiveData<List<EntryRoomDatabase>>

    @Query("SELECT * FROM entrydatabase WHERE entryNumber = :entryNumber")
    fun getEntryWithEntryNumber(entryNumber: Int): EntryRoomDatabase

    //  for getting all entries
    @Query("SELECT * FROM entrydatabase")
    fun getAllEntry(): LiveData<List<EntryRoomDatabase>>

    @Query("DELETE FROM entrydatabase WHERE entryNumber = :entryNumber")
    fun deleteSpecificEntry(entryNumber: Int)

    //  For deleting all rows in roomDatabase
    @Query("DELETE FROM entrydatabase")
    fun deleteallEntries()
}