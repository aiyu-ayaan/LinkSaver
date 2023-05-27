package com.atech.core.data.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.atech.core.util.getDate
import kotlinx.parcelize.Parcelize
import java.io.ByteArrayOutputStream

@Entity(tableName = "link")
@Parcelize
@Keep
data class LinkModel(
    @PrimaryKey(autoGenerate = false)
    val title: String,
    val url: String,
    val icon: String,
    val bitmap: Bitmap? = null,
    val created: Long = System.currentTimeMillis()
) : Parcelable {
    @get:Ignore
    val formattedDate: String
        get() = created.getDate()
}


class BitMapConverter {
    @TypeConverter
    fun fromBitmap(bitmap: Bitmap?): String? {
        return bitmap?.let {
            val stream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray().toString()
        }
    }

    @TypeConverter
    fun toBitmap(bitmapString: String?): Bitmap? {
        return bitmapString?.let {
            val byteArray = android.util.Base64.decode(it, android.util.Base64.DEFAULT)
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }
    }
}


class LinkDiffCallback : DiffUtil.ItemCallback<LinkModel>() {
    override fun areItemsTheSame(oldItem: LinkModel, newItem: LinkModel): Boolean {
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: LinkModel, newItem: LinkModel): Boolean {
        return oldItem == newItem
    }
}