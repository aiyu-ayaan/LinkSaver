package com.atech.backup.backup.model

import android.os.Parcelable
import com.google.errorprone.annotations.Keep
import com.google.gson.GsonBuilder
import kotlinx.parcelize.Parcelize


data class BackUpModel(
    val links: List<LinkBackUpModel>
)

@Keep
@Parcelize
data class LinkBackUpModel(
    val url: String,
    val shortDes: String,
    val isArchive: Boolean,
    val isDeleted: Boolean,
    val deletedAt: Long?,
    val created: Long
) : Parcelable

fun BackUpModel.toJson(): String {
    return GsonBuilder().create().toJson(this)
}

fun String.toBackUpModel(): BackUpModel {
    return GsonBuilder().create().fromJson(this, BackUpModel::class.java)
}