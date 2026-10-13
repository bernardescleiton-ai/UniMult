package com.example.data.remote

import com.example.data.remote.dto.XtreamAuthResponse
import com.example.data.remote.dto.XtreamCategoryDto
import com.example.data.remote.dto.XtreamLiveStreamDto
import com.example.data.remote.dto.XtreamSeriesStreamDto
import com.example.data.remote.dto.XtreamVodStreamDto
import com.example.domain.model.AccountInfo
import com.example.domain.model.Category
import com.example.domain.model.EpisodeItem
import com.example.domain.model.LiveChannel
import com.example.domain.model.MediaType
import com.example.domain.model.SeriesItem
import com.example.domain.model.ServerProfile
import com.example.domain.model.VodItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class XtreamCodesClient {

  private val moshi: Moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

  private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .writeTimeout(15, TimeUnit.SECONDS)
    .followRedirects(true)
    .followSslRedirects(true)
    .build()

  private fun getApiForServer(baseUrl: String): XtreamCodesApi {
    val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
    return Retrofit.Builder()
      .baseUrl(normalizedUrl)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(XtreamCodesApi::class.java)
  }

  suspend fun authenticate(server: ServerProfile): Result<AccountInfo> = withContext(Dispatchers.IO) {
    try {
      val api = getApiForServer(server.baseUrl)
      val response = api.authenticate(server.username, server.password)

      if (!response.isSuccessful) {
        return@withContext Result.failure(
          Exception("Falha na comunicação HTTP com o servidor (${response.code()})")
        )
      }

      val body = response.body() ?: return@withContext Result.failure(
        Exception("Resposta vazia do servidor.")
      )

      val userInfo = body.userInfo
      if (userInfo == null || userInfo.auth != 1) {
        val msg = userInfo?.message ?: "Credenciais inválidas ou acesso não autorizado."
        return@withContext Result.failure(Exception(msg))
      }

      val status = userInfo.status ?: "Active"
      if (!status.equals("Active", ignoreCase = true)) {
        return@withContext Result.failure(Exception("Conta do servidor com status: $status"))
      }

      val expTimestamp = userInfo.expDate?.toLongOrNull()
      val expFormatted = if (expTimestamp != null && expTimestamp > 0) {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        sdf.format(Date(expTimestamp * 1000L))
      } else {
        "Ilimitado"
      }

      val accountInfo = AccountInfo(
        username = userInfo.username ?: server.username,
        status = status,
        expDateFormatted = expFormatted,
        maxConnections = userInfo.maxConnections ?: "1",
        activeConnections = userInfo.activeCons ?: "0",
        serverUrl = body.serverInfo?.url ?: server.baseUrl,
        serverPort = body.serverInfo?.port ?: "",
        isTrial = userInfo.isTrial == "1"
      )

      Result.success(accountInfo)
    } catch (e: Exception) {
      Result.failure(Exception("Erro ao conectar ao servidor: ${e.localizedMessage ?: e.message}"))
    }
  }

  suspend fun getLiveCategories(server: ServerProfile): Result<List<Category>> = withContext(Dispatchers.IO) {
    try {
      val api = getApiForServer(server.baseUrl)
      val response = api.getLiveCategories(server.username, server.password)
      if (response.isSuccessful) {
        val list = response.body()?.mapNotNull { dto ->
          val id = dto.categoryId ?: return@mapNotNull null
          val name = dto.categoryName ?: "Categoria $id"
          Category(id = id, name = name, type = MediaType.LIVE)
        } ?: emptyList()
        Result.success(list)
      } else {
        Result.failure(Exception("Erro ${response.code()} ao buscar categorias de canais"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun getLiveStreams(server: ServerProfile, categoryId: String? = null): Result<List<LiveChannel>> = withContext(Dispatchers.IO) {
    try {
      val api = getApiForServer(server.baseUrl)
      val response = api.getLiveStreams(server.username, server.password, categoryId = categoryId)
      if (response.isSuccessful) {
        val base = server.baseUrl.trimEnd('/')
        val list = response.body()?.mapNotNull { dto ->
          val streamId = dto.streamId?.toString() ?: return@mapNotNull null
          val streamUrl = "$base/live/${server.username}/${server.password}/$streamId.ts"
          LiveChannel(
            streamId = streamId,
            num = dto.num?.toString() ?: "",
            name = dto.name ?: "Canal $streamId",
            streamIcon = dto.streamIcon,
            epgChannelId = dto.epgChannelId,
            categoryId = dto.categoryId ?: "",
            streamUrl = streamUrl
          )
        } ?: emptyList()
        Result.success(list)
      } else {
        Result.failure(Exception("Erro ${response.code()} ao buscar canais"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun getVodCategories(server: ServerProfile): Result<List<Category>> = withContext(Dispatchers.IO) {
    try {
      val api = getApiForServer(server.baseUrl)
      val response = api.getVodCategories(server.username, server.password)
      if (response.isSuccessful) {
        val list = response.body()?.mapNotNull { dto ->
          val id = dto.categoryId ?: return@mapNotNull null
          val name = dto.categoryName ?: "Categoria $id"
          Category(id = id, name = name, type = MediaType.VOD)
        } ?: emptyList()
        Result.success(list)
      } else {
        Result.failure(Exception("Erro ${response.code()} ao buscar categorias de filmes"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun getVodStreams(server: ServerProfile, categoryId: String? = null): Result<List<VodItem>> = withContext(Dispatchers.IO) {
    try {
      val api = getApiForServer(server.baseUrl)
      val response = api.getVodStreams(server.username, server.password, categoryId = categoryId)
      if (response.isSuccessful) {
        val base = server.baseUrl.trimEnd('/')
        val list = response.body()?.mapNotNull { dto ->
          val streamId = dto.streamId?.toString() ?: return@mapNotNull null
          val ext = dto.containerExtension ?: "mp4"
          val streamUrl = "$base/movie/${server.username}/${server.password}/$streamId.$ext"
          val rating5 = when (val r = dto.rating5based) {
            is Number -> r.toDouble()
            is String -> r.toDoubleOrNull() ?: 0.0
            else -> 0.0
          }
          VodItem(
            streamId = streamId,
            num = dto.num?.toString(),
            name = dto.name ?: "Filme $streamId",
            streamIcon = dto.streamIcon,
            rating = dto.rating?.toString(),
            rating5based = rating5,
            categoryId = dto.categoryId ?: "",
            containerExtension = ext,
            streamUrl = streamUrl
          )
        } ?: emptyList()
        Result.success(list)
      } else {
        Result.failure(Exception("Erro ${response.code()} ao buscar filmes"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun getSeriesCategories(server: ServerProfile): Result<List<Category>> = withContext(Dispatchers.IO) {
    try {
      val api = getApiForServer(server.baseUrl)
      val response = api.getSeriesCategories(server.username, server.password)
      if (response.isSuccessful) {
        val list = response.body()?.mapNotNull { dto ->
          val id = dto.categoryId ?: return@mapNotNull null
          val name = dto.categoryName ?: "Categoria $id"
          Category(id = id, name = name, type = MediaType.SERIES)
        } ?: emptyList()
        Result.success(list)
      } else {
        Result.failure(Exception("Erro ${response.code()} ao buscar categorias de séries"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun getSeriesStreams(server: ServerProfile, categoryId: String? = null): Result<List<SeriesItem>> = withContext(Dispatchers.IO) {
    try {
      val api = getApiForServer(server.baseUrl)
      val response = api.getSeriesStreams(server.username, server.password, categoryId = categoryId)
      if (response.isSuccessful) {
        val list = response.body()?.mapNotNull { dto ->
          val seriesId = dto.seriesId?.toString() ?: return@mapNotNull null
          SeriesItem(
            seriesId = seriesId,
            num = dto.num?.toString(),
            name = dto.name ?: "Série $seriesId",
            cover = dto.cover,
            plot = dto.plot,
            cast = dto.cast,
            director = dto.director,
            genre = dto.genre,
            releaseDate = dto.releaseDate,
            rating = dto.rating?.toString(),
            categoryId = dto.categoryId ?: ""
          )
        } ?: emptyList()
        Result.success(list)
      } else {
        Result.failure(Exception("Erro ${response.code()} ao buscar séries"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun getSeriesEpisodes(server: ServerProfile, seriesId: String): Result<List<EpisodeItem>> = withContext(Dispatchers.IO) {
    try {
      val api = getApiForServer(server.baseUrl)
      val response = api.getSeriesInfo(server.username, server.password, seriesId = seriesId)
      if (response.isSuccessful) {
        val jsonStr = response.body()?.string() ?: return@withContext Result.success(emptyList())
        val json = JSONObject(jsonStr)
        val episodesObj = json.optJSONObject("episodes") ?: return@withContext Result.success(emptyList())
        val result = mutableListOf<EpisodeItem>()
        val base = server.baseUrl.trimEnd('/')

        val keys = episodesObj.keys()
        while (keys.hasNext()) {
          val seasonKey = keys.next()
          val seasonArray = episodesObj.optJSONArray(seasonKey) ?: continue
          for (i in 0 until seasonArray.length()) {
            val epObj = seasonArray.getJSONObject(i)
            val epId = epObj.optString("id", "")
            val epNum = epObj.optInt("episode_num", i + 1)
            val title = epObj.optString("title", "Episódio $epNum")
            val ext = epObj.optString("container_extension", "mp4")
            val plot = epObj.optString("plot", "")
            val duration = epObj.optString("duration", "")
            val streamUrl = "$base/series/${server.username}/${server.password}/$epId.$ext"
            result.add(
              EpisodeItem(
                id = epId,
                episodeNum = epNum,
                title = title,
                containerExtension = ext,
                plot = plot,
                duration = duration,
                streamUrl = streamUrl
              )
            )
          }
        }
        Result.success(result)
      } else {
        Result.failure(Exception("Erro ${response.code()} ao buscar episódios"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
