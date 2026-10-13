package com.example.domain.model

enum class MediaType {
  LIVE,
  VOD,
  SERIES,
  KIDS,
  ANIME
}

data class Category(
  val id: String,
  val name: String,
  val type: MediaType
)

data class LiveChannel(
  val streamId: String,
  val num: String,
  val name: String,
  val streamIcon: String?,
  val epgChannelId: String?,
  val categoryId: String,
  val currentProgram: String? = null,
  val streamUrl: String
)

data class VodItem(
  val streamId: String,
  val num: String? = null,
  val name: String,
  val streamIcon: String?,
  val rating: String? = null,
  val rating5based: Double = 0.0,
  val year: String? = null,
  val categoryId: String,
  val containerExtension: String = "mp4",
  val plot: String? = null,
  val genre: String? = null,
  val streamUrl: String
)

data class SeriesItem(
  val seriesId: String,
  val num: String? = null,
  val name: String,
  val cover: String?,
  val plot: String? = null,
  val cast: String? = null,
  val director: String? = null,
  val genre: String? = null,
  val releaseDate: String? = null,
  val rating: String? = null,
  val categoryId: String
)

data class EpisodeItem(
  val id: String,
  val episodeNum: Int,
  val title: String,
  val containerExtension: String = "mp4",
  val plot: String? = null,
  val duration: String? = null,
  val streamUrl: String
)

data class MatchGuideItem(
  val id: String,
  val competition: String, // e.g. "Champions League UCL"
  val homeTeam: String,
  val homeTeamLogo: String,
  val awayTeam: String,
  val awayTeamLogo: String,
  val matchTime: String, // e.g. "13:45"
  val matchDate: String, // e.g. "13/10"
  val status: String, // e.g. "Em andamento" or "Agendado"
  val isLive: Boolean = false,
  val channelName: String? = null
)

data class AccountInfo(
  val username: String,
  val status: String,
  val expDateFormatted: String,
  val maxConnections: String,
  val activeConnections: String,
  val serverUrl: String,
  val serverPort: String,
  val isTrial: Boolean = false
)
