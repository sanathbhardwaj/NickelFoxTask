package com.example.nickelfoxtask.data.remote

import com.example.nickelfoxtask.data.model.YoutubeResponse
import com.example.nickelfoxtask.util.API_KEY
import com.example.nickelfoxtask.util.PART
import com.example.nickelfoxtask.util.QUERY_PAGE_SIZE
import com.example.nickelfoxtask.util.TYPE
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YoutubeApi {
    @GET("v3/search")
    suspend fun getVideos(
        @Query("part") part:String = "",
        @Query("type") type:String = "",
        @Query("pageToken") pageToken:String = "",
        @Query("maxResults") maxResults: Int = QUERY_PAGE_SIZE,
        @Query("key") apiKey: String = API_KEY
    ): Response<YoutubeResponse>
}