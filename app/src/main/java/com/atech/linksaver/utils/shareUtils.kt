package com.atech.linksaver.utils

import android.app.Activity
import android.content.Intent
import com.atech.core.data.model.LinkModel
import com.atech.linksaver.R


fun Activity.openShare(model: LinkModel) = this.startActivity(
    Intent.createChooser(Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT, """
            ${model.url}
            
            Shared via Link Saver
            ${getString(R.string.github_link)}
        """.trimIndent()
        )
        type = "text/plain"
    }, "Share Link")
)