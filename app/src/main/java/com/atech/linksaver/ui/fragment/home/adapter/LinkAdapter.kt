package com.atech.linksaver.ui.fragment.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.decode.SvgDecoder
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.atech.core.data.model.LinkDiffCallback
import com.atech.core.data.model.LinkModel
import com.atech.core.util.openLink
import com.atech.linksaver.R
import com.atech.linksaver.databinding.RowLinksBinding

class LinkAdapter : ListAdapter<LinkModel, LinkAdapter.LinkViewHolder>(LinkDiffCallback()) {

    inner class LinkViewHolder(
        private val binding: RowLinksBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.materialButton.setOnClickListener {
                adapterPosition.let { position ->
                    if (position != RecyclerView.NO_POSITION) {
                        getItem(position).let { linkModel ->
                            binding.root.context.openLink(linkModel.url)
                        }
                    }
                }
            }
        }

        fun bind(linkModel: LinkModel) {
            binding.apply {
                textViewLink.text = linkModel.url
                bottomItemLogic(linkModel)
            }
        }

        private fun bottomItemLogic(linkModel: LinkModel) {
            linkModel.apply {
                if (title.isNullOrEmpty()) {
                    binding.textViewTitle.isVisible = false
                } else {
                    binding.textViewTitle.isVisible = true
                    binding.textViewTitle.text = title!!.trim()
                }
                if (description.isNullOrEmpty()) {
                    binding.textViewDes.isVisible = false
                } else {
                    binding.textViewDes.isVisible = true
                    binding.textViewDes.text = description!!.trim()
                }
                if (icon.isNullOrEmpty()) {
                    binding.imageViewIcon.load(R.drawable.avatar_svgrepo_com)
                } else {
                    binding.imageViewIcon.load(icon) {
                        crossfade(true)
                        placeholder(R.drawable.avatar_svgrepo_com)
                        if (icon!!.endsWith(".svg")) {
                            decoderFactory { result, options, _ ->
                                SvgDecoder(
                                    result.source,
                                    options
                                )
                            }
                        }
                        transformations(CircleCropTransformation())
                    }
                }
                if (thumbnail.isNullOrEmpty()) {
                    binding.imageViewThumbnail.isVisible = false
                } else {
                    binding.imageViewThumbnail.isVisible = true
                    binding.imageViewThumbnail.load(thumbnail) {
                        crossfade(true)
                        placeholder(R.drawable.loading_svgrepo_com)
                        transformations(RoundedCornersTransformation(16f))
                        scale(coil.size.Scale.FILL)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkViewHolder =
        LinkViewHolder(RowLinksBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: LinkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}