package com.atech.linksaver.utils

import android.widget.ImageView
import coil.decode.SvgDecoder
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.atech.linksaver.R


fun ImageView.loadImage(url: String?) {
    this.load(url) {
        crossfade(true)
        placeholder(R.drawable.loading_svgrepo_com)
        transformations(RoundedCornersTransformation(16f))
        scale(coil.size.Scale.FILL)
        error(R.drawable.loading_svgrepo_com)
    }
}

fun ImageView.loadIcon(url: String?) {
    this.load(url) {
        crossfade(true)
        placeholder(R.drawable.avatar_svgrepo_com)
        if (url!!.endsWith(".svg")) {
            decoderFactory { result, options, _ ->
                SvgDecoder(
                    result.source, options
                )
            }
        }
        error(R.drawable.avatar_svgrepo_com)
        transformations(CircleCropTransformation())
    }
}