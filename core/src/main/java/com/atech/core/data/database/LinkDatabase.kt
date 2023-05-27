package com.atech.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.atech.core.data.model.BitMapConverter
import com.atech.core.data.model.LinkModel

@Database(entities = [LinkModel::class], version = 1, exportSchema = false)
@TypeConverters(BitMapConverter::class)
abstract class LinkDatabase : RoomDatabase() {
    abstract fun linkDao(): LinkDao

    companion object {
        const val DATABASE_NAME = "link_database"
    }
}