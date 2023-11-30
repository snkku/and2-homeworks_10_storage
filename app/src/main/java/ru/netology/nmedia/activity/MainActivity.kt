package ru.netology.nmedia.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.viewmodel.PostViewModel
import androidx.activity.viewModels
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.adapter.onInteractionListener
import ru.netology.nmedia.dto.Post


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: PostViewModel by viewModels()
        val binding = ActivityMainBinding.inflate(layoutInflater)

        val newPostLauncher = registerForActivityResult(NewPostContract) { text ->
            if (text.isNullOrBlank()) {
                viewModel.clearEdited()
                return@registerForActivityResult
            }
            viewModel.save(text)
        }

        val interaction = object : onInteractionListener {
            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, post.content)
                }
                val chooser = Intent.createChooser(intent, getString(R.string.app_name))
                startActivity(intent)
                viewModel.share(post.id)
            }

            override fun onPlayVideo(post: Post) {
                val id = post.videoURL?.let { Regex("\\.*\\?v=([\\w\\-]+)(\\S+)?\$").findAll(it).map { it.groupValues[1] }.joinToString() }
                val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:"+id))
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v="+id))
                val chooser = Intent.createChooser(intent, getString(R.string.app_name))
                try {
                    startActivity(appIntent)
                } catch(ex: ActivityNotFoundException) {
                    startActivity(webIntent)
                }
            }

            override fun onLike(post: Post) {
                viewModel.like(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.remove(post.id)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                newPostLauncher.launch(post.content)
            }

        }
        setContentView(binding.root)
        val adapter = PostAdapter(interaction)
        binding.recycler.adapter = adapter

        viewModel.data.observe(this) { posts ->
            val curSize = adapter.currentList.size
            adapter.submitList(posts) {
                if (curSize < posts.size)
                    binding.recycler.smoothScrollToPosition(0)
            }
        }
        viewModel.edited.observe(this) { post ->

        }

        binding.add.setOnClickListener {
            newPostLauncher.launch(null)
        }

//        binding.content.doOnTextChanged { text, start, before, count ->
//            with(binding.save)
//            {
//                if (text.toString()
//                        .trim().length > 0 && viewModel.edited.value?.content != text.toString()
//                ) {
//                    setEnabled(true)
//                    setClickable(true)
//                    imageAlpha = 255
//                } else {
//                    setEnabled(false)
//                    setClickable(false)
//                    imageAlpha = 50
//                }
//            }
//        }

//        binding.cancelButton.setOnClickListener {
//            binding.content.setText("")
//            viewModel.clearEdited()
//            AndroidUtils.hideKeyboard(binding.root)
//            binding.vgEdit.visibility = View.GONE
//
//        }
//
//        binding.save.setOnClickListener {
//            with(binding.content) {
//                if (text.isNullOrBlank()) {
//                    Toast.makeText(
//                        this@MainActivity,
//                        context.getString(R.string.error_empty_content),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    return@setOnClickListener
//                }
//            }
//            viewModel.save(binding.content.text.toString())
//            binding.cancelButton.performClick()
//        }
    }
}