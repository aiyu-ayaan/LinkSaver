package com.atech.core.data.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "link")
data class LinkModel(
    @PrimaryKey(autoGenerate = false)
    private val title: String,
    private val url: String,
    private val icon: String,
    private val bitmap: Bitmap? = null,
    private val created : Long = System.currentTimeMillis()
)
