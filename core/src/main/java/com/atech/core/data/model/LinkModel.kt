package com.atech.core.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.atech.core.util.getDate
import kotlinx.parcelize.Parcelize

@Entity(tableName = "link")
@Parcelize
@Keep
data class LinkModel(
    @PrimaryKey(autoGenerate = false)
    val url: String,
    val title: String? = null,
    val description: String? = null,
    val icon: String? = null,
    val thumbnail: String? = null,
    val isArchive: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val isThumbnailLoaded: Boolean = false,
    val created: Long = System.currentTimeMillis()
) : Parcelable {
    @get:Ignore
    val formattedDate: String
        get() = created.getDate()
}


class LinkDiffCallback : DiffUtil.ItemCallback<LinkModel>() {
    override fun areItemsTheSame(oldItem: LinkModel, newItem: LinkModel): Boolean {
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: LinkModel, newItem: LinkModel): Boolean {
        return oldItem == newItem
    }
}