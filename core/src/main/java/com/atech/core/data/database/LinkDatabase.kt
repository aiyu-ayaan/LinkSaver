package com.atech.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.atech.core.data.model.FilterModel
import com.atech.core.data.model.LinkModel

@Database(entities = [LinkModel::class, FilterModel::class], version = 4, exportSchema = false)
abstract class LinkDatabase : RoomDatabase() {
    abstract fun linkDao(): LinkDao

    abstract fun filterDao(): FilterDao

    companion object {
        const val DATABASE_NAME = "link_database"

        val migration_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE link ADD COLUMN shortDes TEXT NOT NULL DEFAULT ''")
            }
        }
        val migration_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE link ADD COLUMN filter TEXT NOT NULL DEFAULT ''")
            }
        }
        val migration_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE filters (filter TEXT NOT NULL DEFAULT 'undefined', id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")
                database.execSQL("CREATE UNIQUE INDEX index_filters_filter ON filters (filter ASC)")
            }
        }
    }
}