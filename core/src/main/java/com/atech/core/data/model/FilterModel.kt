package com.atech.core.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Keep
@Entity(tableName = "filters", indices = [Index(value = ["filter"], unique = true)])
@Parcelize
data class FilterModel(
    val filter: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
) : Parcelable