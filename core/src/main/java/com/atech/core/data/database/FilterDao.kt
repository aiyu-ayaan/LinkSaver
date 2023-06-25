package com.atech.core.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.atech.core.data.model.FilterModel

@Dao
interface FilterDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertFilter(filter: FilterModel)

    @Query("SELECT * FROM filters ORDER BY id DESC")
    fun getFilters(): LiveData<List<FilterModel>>


    @Delete
    suspend fun deleteFilter(filter: FilterModel)


    @Query("DELETE FROM filters")
    suspend fun deleteAllFilters()


    @Query("SELECT * FROM filters WHERE filter = :filter")
    suspend fun getFilter(filter: String): FilterModel?

}