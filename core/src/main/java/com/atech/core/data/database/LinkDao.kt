package com.atech.core.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.atech.core.data.model.LinkModel

@Dao
interface LinkDao {

    @Query("SELECT * FROM link order by created desc")
    fun getAllLinks(): LiveData<List<LinkModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(linkModel: LinkModel)


    @Update
    suspend fun updateLink(linkModel: LinkModel) : Int


    @Delete
    suspend fun deleteLink(linkModel: LinkModel)


    @Query("DELETE FROM link")
    suspend fun deleteAllLinks()

}