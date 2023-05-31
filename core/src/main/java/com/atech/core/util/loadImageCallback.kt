package com.atech.core.util

import android.util.Log
import com.atech.core.data.model.LinkModel
import com.atech.urlimageloader.kotlin.UrlImageLoader
import com.atech.urlimageloader.utils.extractQueryFromUrl
import com.atech.urlimageloader.utils.makeValidUrl
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


suspend fun loadImageCallback(link: String): LinkModel = suspendCoroutine { continuation ->
    runBlocking {
        Log.d("AAA", "loadImageCallback: ${Thread.currentThread().name}")
        UrlImageLoader.getLinkDetailsUrl(link.extractQueryFromUrl()) { details, error ->
            if (error != null) continuation.resume(
                LinkModel(
                    url = link.makeValidUrl(),
                    title = null,
                    description = null,
                    icon = null,
                    thumbnail = null
                )
            )
            else continuation.resume(
                LinkModel(
                    url = link.makeValidUrl(),
                    title = details?.title,
                    description = details?.description,
                    icon = details?.iconLink,
                    thumbnail = details?.imageLink
                )
            )
        }
    }
}
