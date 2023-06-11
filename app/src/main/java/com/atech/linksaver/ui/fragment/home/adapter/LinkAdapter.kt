package com.atech.linksaver.ui.fragment.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.atech.core.data.model.LinkDiffCallback
import com.atech.core.data.model.LinkModel
import com.atech.core.util.openLink
import com.atech.linksaver.R
import com.atech.linksaver.databinding.RowLinksBinding
import com.atech.linksaver.utils.loadIcon
import com.atech.linksaver.utils.loadImage

class LinkAdapter(
    private val onItemLongClicked: () -> Unit = { },
    private val onItemClicked: (Pair<LinkModel, View>, Boolean, CheckBox) -> Unit = { _, _, _ -> },
    private val isLongClickable: Boolean = true,
) : ListAdapter<LinkModel, LinkAdapter.LinkViewHolder>(LinkDiffCallback()) {

    private var isLongClickEnable = false


    @SuppressLint("NotifyDataSetChanged")
    fun setLongClick(value: Boolean) {
        isLongClickEnable = value
        notifyDataSetChanged()
    }


    inner class LinkViewHolder(
        private val binding: RowLinksBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (isLongClickEnable) {
                    changeOnLongClickState()
                }

                adapterPosition.let { position ->
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClicked(
                            getItem(position) to binding.root,
                            isLongClickEnable,
                            binding.checkBox
                        )
                    }
                }
            }
            binding.root.setOnLongClickListener {
                if (!isLongClickable) return@setOnLongClickListener false
                if (isLongClickEnable) return@setOnLongClickListener false
                changeOnLongClickState()
                isLongClickEnable = true
                adapterPosition.let { position ->
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClicked(
                            getItem(position) to binding.root,
                            isLongClickEnable,
                            binding.checkBox
                        )
                        onItemLongClicked()
                    }
                }
                true
            }
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
                root.transitionName = linkModel.url
                textViewLink.text = linkModel.url
                bottomItemLogic(linkModel)
            }
            resetOnLongClickState()
        }

        private fun changeOnLongClickState() = binding.checkBox.apply {
            isVisible = true
            isChecked = true
        }

        private fun resetOnLongClickState() = binding.checkBox.apply {
            isVisible = false
            isChecked = false
        }


        private fun bottomItemLogic(linkModel: LinkModel) {
            linkModel.apply {
                if (title.isNullOrEmpty()) {
                    binding.textViewTitle.isVisible = false
                } else {
                    binding.textViewTitle.isVisible = true
                    binding.textViewTitle.text = title!!.trim()
                }
                if (shortDes.isEmpty()) {
                    binding.textViewShortTitle.isVisible = false
                } else {
                    binding.textViewShortTitle.isVisible = true
                    binding.textViewShortTitle.text = shortDes.trim()
                }
                if (description.isNullOrEmpty() || description == title) {
                    binding.textViewDes.isVisible = false
                } else {
                    binding.textViewDes.isVisible = true
                    binding.textViewDes.text = description!!.trim()
                }
                if (icon.isNullOrEmpty()) {
                    binding.imageViewIcon.load(R.drawable.avatar_svgrepo_com)
                } else binding.imageViewIcon.loadIcon(icon!!)

                if (thumbnail.isNullOrEmpty()) {
                    binding.imageViewThumbnail.isVisible = false
                } else {
                    binding.imageViewThumbnail.isVisible = true
                    binding.imageViewThumbnail.loadImage(thumbnail!!)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkViewHolder =
        LinkViewHolder(RowLinksBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: LinkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).url.hashCode().toLong()
    }
}