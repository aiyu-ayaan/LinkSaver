package com.atech.backup.login

import android.os.Parcelable
import com.google.errorprone.annotations.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class UserModelModel(
    val uid: String,
    val mail: String,
    val name: String,
    val profileImage: String,
    val created: Long = System.currentTimeMillis(),
) : Parcelable

@Keep
@Parcelize
data class GetUserModel(
    val uid: String? = null,
    val mail: String? = null,
    val name: String? = null,
    val profileImage: String? = null,
    val created: Long? = null,
) : Parcelable