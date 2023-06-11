package com.atech.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.atech.core.data.model.LinkModel

@Database(entities = [LinkModel::class], version = 2, exportSchema = false)
abstract class LinkDatabase : RoomDatabase() {
    abstract fun linkDao(): LinkDao

    companion object {
        const val DATABASE_NAME = "link_database"

        val migration_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE link ADD COLUMN shortDes TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}