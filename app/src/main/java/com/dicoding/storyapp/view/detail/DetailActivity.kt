package com.dicoding.storyapp.view.detail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.dicoding.storyapp.R
import com.dicoding.storyapp.ViewModelFactory
import com.dicoding.storyapp.data.response.ListStoryItem
import com.dicoding.storyapp.databinding.ActivityDetailBinding
import com.dicoding.storyapp.view.main.MainViewModel

class DetailActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private var _binding: ActivityDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menerima data dari intent
        val photoUrl = intent.getStringExtra(DETAIL_PHOTO_URL)
        val name = intent.getStringExtra(DETAIL_NAME)
        val description = intent.getStringExtra(DETAIL_DESCRIPTION)

        // Membuat objek ListStoryItem dari data yang diterima
        val story = ListStoryItem(
            photoUrl = photoUrl,
            name = name,
            description = description,
            id = ""
        )

        setupData(story)
        setupAction()
    }


    private fun setupData(storyItem: ListStoryItem) {
        Glide.with(applicationContext)
            .load(storyItem.photoUrl)
            .into(binding.ivAvatar)
        binding.tvName.text = storyItem.name
        binding.tvDesc.text = storyItem.description
    }

    companion object {
        const val DETAIL_PHOTO_URL = "detail_photo_url"
        const val DETAIL_NAME = "detail_name"
        const val DETAIL_DESCRIPTION = "detail_description"
    }

    private fun setupAction() {
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_logout -> {
                    viewModel.logout()
                    true
                }
                R.id.menu_language ->{
                    startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                    true
                }
                else -> false
            }
        }
    }
}