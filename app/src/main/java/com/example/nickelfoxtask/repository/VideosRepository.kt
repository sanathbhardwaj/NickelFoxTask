package com.example.nickelfoxtask.repository

import com.example.nickelfoxtask.data.model.YoutubeResponse
import com.example.nickelfoxtask.data.remote.YoutubeApi
import com.example.nickelfoxtask.util.PART
import com.example.nickelfoxtask.util.TYPE
import dagger.hilt.android.HiltAndroidApp
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideosRepository  @Inject constructor(
    private val youtubeApi: YoutubeApi,
) {

    suspend fun getYoutubeVideos(pageToken: String): Response<YoutubeResponse> {
        return youtubeApi.getVideos(PART, TYPE, pageToken)
    }
}