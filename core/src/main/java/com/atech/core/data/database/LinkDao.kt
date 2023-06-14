package com.atech.core.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.atech.core.data.model.LinkModel
import kotlinx.coroutines.flow.Flow

@Dao
interface LinkDao {

    @Query("SELECT * FROM link WHERE isArchive = 0 AND isDeleted = 0 ORDER BY created DESC")
    fun getAllLinks(): LiveData<List<LinkModel>>

    @Query("SELECT * FROM link")
    fun getAllLinksOnes(): List<LinkModel>

    @Query(
        "SELECT * FROM link WHERE url LIKE '%'||:query||'%' or title LIKE '%'||:query||'%' or description LIKE '%'||:query||'%'" +
                "or shortDes LIKE '%'||:query||'%'" +
                "and isArchive = 0 AND isDeleted = 0 ORDER BY created DESC"
    )
    fun getSearchResult(query: String): LiveData<List<LinkModel>>


    @Query("SELECT * FROM link WHERE isArchive = 1 ORDER BY created DESC")
    fun getAllArchivedLinks(): LiveData<List<LinkModel>>

    @Query("SELECT * FROM link WHERE isDeleted = 1 ORDER BY created DESC")
    fun getAllDeletedLinks(): LiveData<List<LinkModel>>

    @Query("SELECT * FROM link WHERE isDeleted = 1 ORDER BY created DESC")
    suspend fun getAllDeletedLinksOnes(): List<LinkModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(linkModel: LinkModel)


    @Update
    suspend fun updateLink(linkModel: LinkModel): Int


    @Delete
    suspend fun deleteLink(linkModel: LinkModel)


    @Query("DELETE FROM link WHERE isDeleted = 1")
    suspend fun deleteAllLinks()

    @Query("DELETE FROM link WHERE isDeleted = 1 AND deletedAt >= date('now','-30 day')")
    suspend fun autoDeleteIn30Days()


    @Query("SELECT * FROM LINK WHERE isThumbnailLoaded = 0")
    fun getAllLinksNotLoaded(): Flow<List<LinkModel>>

}