package com.mynewproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mynewproject.R
import com.mynewproject.ShortNumberFun.shortNumber
import com.mynewproject.databinding.CardPostBinding
import com.mynewproject.dto.Post

interface OnInteractionListener {
    fun like(post: Post)
    fun share(post: Post)
    fun remove(post: Post)
    fun edit(post: Post)
    fun onVideoClick(videoUrl: String)
    fun onPostClick(post: Post)
}

class PostAdapter(
    private val onInteractionListener: OnInteractionListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content

            Glide.with(itemView)
                .load("http://10.0.2.2:9999/avatars/${post.authorAvatar}")
                .timeout(10_000)
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_error)
                .circleCrop()
                .into(binding.avatar)


            if (post.video != null) {
                videoGroup.visibility = View.VISIBLE
                videoGroup.setOnClickListener {
                    onInteractionListener.onVideoClick(post.video)
                }
                playButton.setOnClickListener {
                    onInteractionListener.onVideoClick(post.video)
                }
            } else {
                videoGroup.visibility = View.GONE
            }

            icLikes.isChecked = post.likedByMe
            icLikes.text = shortNumber(post.likes)
            icShare.isChecked = post.sharedByMe
            icShare.text = shortNumber(post.shares)

            icLikes.setOnClickListener {
                onInteractionListener.like(post)
            }
            icShare.setOnClickListener {
                onInteractionListener.share(post)
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.remove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.edit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            root.setOnClickListener { view ->
                val ignoredIds = listOf(
                    menu.id,
                    icLikes.id,
                    icShare.id,
                    videoGroup.id,
                    playButton.id
                )
                if (ignoredIds.contains(view.id)) return@setOnClickListener

                onInteractionListener.onPostClick(post)
            }
        }
    }
}

object PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
        oldItem == newItem
}