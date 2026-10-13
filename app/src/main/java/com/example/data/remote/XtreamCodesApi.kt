package com.example.data.remote

import com.example.data.remote.dto.XtreamAuthResponse
import com.example.data.remote.dto.XtreamCategoryDto
import com.example.data.remote.dto.XtreamLiveStreamDto
import com.example.data.remote.dto.XtreamSeriesStreamDto
import com.example.data.remote.dto.XtreamVodStreamDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface XtreamCodesApi {

  @GET("player_api.php")
  suspend fun authenticate(
    @Query("username") username: String,
    @Query("password") password: String
  ): Response<XtreamAuthResponse>

  @GET("player_api.php")
  suspend fun getLiveCategories(
    @Query("username") username: String,
    @Query("password") password: String,
    @Query("action") action: String = "get_live_categories"
  ): Response<List<XtreamCategoryDto>>

  @GET("player_api.php")
  suspend fun getLiveStreams(
    @Query("username") username: String,
    @Query("password") password: String,
    @Query("action") action: String = "get_live_streams",
    @Query("category_id") categoryId: String? = null
  ): Response<List<XtreamLiveStreamDto>>

  @GET("player_api.php")
  suspend fun getVodCategories(
    @Query("username") username: String,
    @Query("password") password: String,
    @Query("action") action: String = "get_vod_categories"
  ): Response<List<XtreamCategoryDto>>

  @GET("player_api.php")
  suspend fun getVodStreams(
    @Query("username") username: String,
    @Query("password") password: String,
    @Query("action") action: String = "get_vod_streams",
    @Query("category_id") categoryId: String? = null
  ): Response<List<XtreamVodStreamDto>>

  @GET("player_api.php")
  suspend fun getSeriesCategories(
    @Query("username") username: String,
    @Query("password") password: String,
    @Query("action") action: String = "get_series_categories"
  ): Response<List<XtreamCategoryDto>>

  @GET("player_api.php")
  suspend fun getSeriesStreams(
    @Query("username") username: String,
    @Query("password") password: String,
    @Query("action") action: String = "get_series",
    @Query("category_id") categoryId: String? = null
  ): Response<List<XtreamSeriesStreamDto>>

  @GET("player_api.php")
  suspend fun getSeriesInfo(
    @Query("username") username: String,
    @Query("password") password: String,
    @Query("action") action: String = "get_series_info",
    @Query("series_id") seriesId: String
  ): Response<okhttp3.ResponseBody>
}
