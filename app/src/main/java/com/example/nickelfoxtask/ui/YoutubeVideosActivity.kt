package com.example.nickelfoxtask.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nickelfoxtask.adapter.VideoListAdapter
import com.example.nickelfoxtask.data.model.Items
import com.example.nickelfoxtask.databinding.ActivityMainBinding
import com.example.nickelfoxtask.util.QUERY_PAGE_SIZE
import com.example.nickelfoxtask.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import android.content.ActivityNotFoundException

import android.content.Intent
import android.net.Uri
import android.view.View.GONE
import android.view.View.VISIBLE
import com.example.nickelfoxtask.util.NetworkUtil
import com.google.android.material.snackbar.Snackbar


private const val TAG = "YoutubeVideosActivity"
@AndroidEntryPoint
class YoutubeVideosActivity : AppCompatActivity(), VideoListAdapter.OnItemClickListener {

    private var binding: ActivityMainBinding? = null

    private val viewModel: YoutubeVideosViewModel by viewModels()

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding?.root!!
        setContentView(view)
        viewModel.getVideosList()


        val videoListAdapter = VideoListAdapter(this)


        binding?.apply {
            rvVideoList.apply {
                adapter = videoListAdapter
                setHasFixedSize(true)
                addOnScrollListener(this@YoutubeVideosActivity.scrollListener)
            }
            btRetry.setOnClickListener {
                viewModel.getVideosList()
                llNoInternet.visibility = GONE
            }
        }

        viewModel.videos.observe(this) {
            when (it) {
                is Resource.Success -> {
                    paginationProgressBar.visibility = View.INVISIBLE
                    isLoading = false
                    it.data?.let { videosResponse ->
                        videoListAdapter.submitList(videosResponse.items?.toList())
                        val totalPages = videosResponse.pageInfo?.totalResults!!/QUERY_PAGE_SIZE + 2
                        isLastPage = (viewModel.videoListPage* videosResponse.pageInfo.resultsPerPage!!)>= totalPages
                        if (isLastPage)
                            rvVideoList.setPadding(0, 0, 0, 0)
                    }
                }
                is Resource.Error -> {
                    if (!NetworkUtil.hasInternetConnection(this@YoutubeVideosActivity)){
                        llNoInternet.visibility = VISIBLE
                    }
                    else{
                        llNoInternet.visibility = GONE
                    }
                    paginationProgressBar.visibility = View.INVISIBLE
                    isLoading = true
                    it.message?.let { message ->
                        view.snack(message)
                        Log.e(TAG, "Error: $message")
                    }
                }
                is Resource.Loading -> {
                    paginationProgressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) { //State is scrolling
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val totalVisibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + totalVisibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate =
                isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewModel.getVideosList()
                isScrolling = false
            }
        }
    }

    override fun onItemClick(items: Items) {
        Log.d(TAG, "onItemClick: "+items.id)
        watchYoutubeVideo(items.id?.videoId!!)
    }
    private fun watchYoutubeVideo(id: String) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id"))
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://www.youtube.com/watch?v=$id")
        )
        try {
            startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            startActivity(webIntent)
        }
    }
    private fun View.snack(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(this, message, duration).show()
    }
}
