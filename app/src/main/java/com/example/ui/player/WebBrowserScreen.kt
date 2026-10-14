package com.example.ui.player

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BurgundyDeep

@Composable
fun WebBrowserScreen(
  url: String,
  title: String,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var webView: WebView? by remember { mutableStateOf(null) }

  BackHandler {
    if (webView?.canGoBack() == true) {
      webView?.goBack()
    } else {
      onBack()
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BurgundyDeep)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFF1B0B10))
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
      }
      Spacer(modifier = Modifier.width(8.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(text = title.ifBlank { "Navegador Web" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
        Text(text = url, color = AccentGold, fontSize = 11.sp, maxLines = 1)
      }
      IconButton(onClick = { webView?.reload() }) {
        Icon(Icons.Default.Refresh, contentDescription = "Recarregar", tint = Color.White)
      }
    }

    Box(modifier = Modifier.fillMaxSize()) {
      AndroidView(
        factory = { ctx ->
          WebView(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.mediaPlaybackRequiresUserGesture = false
            webViewClient = WebViewClient()
            loadUrl(url)
            webView = this
          }
        },
        modifier = Modifier.fillMaxSize()
      )
    }
  }
}
