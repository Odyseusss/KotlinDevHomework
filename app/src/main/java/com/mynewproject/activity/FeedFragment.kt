package com.mynewproject.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mynewproject.R
import com.mynewproject.adapter.OnInteractionListener
import com.mynewproject.adapter.PostAdapter
import com.mynewproject.databinding.FragmentFeedBinding
import com.mynewproject.dto.Post
import com.mynewproject.viewmodel.PostViewModel
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.mynewproject.activity.SinglePostFragment.Companion.postIdArg

class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostAdapter(object : OnInteractionListener {

            override fun like(post: Post) = viewModel.likeById(post)

            override fun share(post: Post) {
                viewModel.shareById(post.id)
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, post.content)
                }
                startActivity(Intent.createChooser(intent, getString(R.string.chooser_share_post)))
            }

            override fun remove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun edit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            }

            override fun onVideoClick(videoUrl: String) {
                openVideo(videoUrl)
            }

            override fun onPostClick(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_singlePostFragment,
                    Bundle().apply { postIdArg = post.id }
                )
            }
        })

        binding.list.adapter = adapter
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.empty.isVisible = state.empty
        }
        viewModel.newerCount.observe(viewLifecycleOwner) { count ->
            binding.newPosts.isVisible = count > 0
            binding.newPosts.setText(R.string.new_posts)
        }

        binding.newPosts.setOnClickListener {
            viewModel.showNewPosts()
            binding.list.smoothScrollToPosition(0)
            binding.newPosts.isVisible = false
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.SmthWentWrong, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.retry) {
                        viewModel.load()
                    }
                    .show()
            }
            binding.swipeRefresh.isRefreshing = state.refreshing
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }


        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry) {
                    viewModel.load()
                }
                .show()
        }

        binding.add.setOnClickListener {
            viewModel.cancelEdit()
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root
    }

    private fun openVideo(videoUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, videoUrl.toUri())
        startActivity(intent)
    }
}