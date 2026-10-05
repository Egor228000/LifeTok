package com.example.lifetok

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.lifetok.ui.theme.LifeTokTheme
import androidx.core.net.toUri
import androidx.media3.common.Player

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LifeTokTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    VideoFeed(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

private val videoUris = listOf(
    "android.resource://com.example.lifetok/raw/img_2710",
    "android.resource://com.example.lifetok/raw/video1",
    "android.resource://com.example.lifetok/raw/img_2738"
)

@Composable
fun VideoFeed(modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = { videoUris.size })

    VerticalPager(state = pagerState, modifier = modifier.fillMaxSize()) { page ->
        VideoPlayerPage(uri = videoUris[page], isVisible = page == pagerState.currentPage)
    }
}




@OptIn(UnstableApi::class)
@SuppressLint("RememberReturnType")
@Composable
fun VideoPlayerPage(uri: String, isVisible: Boolean) {
    val context = LocalContext.current
    val view = LocalView.current

    val player = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri.toUri()))
            prepare()
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
        }

    }
    var isPlaying by remember { mutableStateOf(false) }
    var isLike by remember { mutableStateOf(false) }
    LaunchedEffect(isVisible) {
        if (isVisible) player.play() else player.pause()
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playWhenReady: Boolean) {
                isPlaying = playWhenReady
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { player.release() }
    }
    CompositionLocalProvider(
        value = LocalRippleConfiguration provides null
    ) {


        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clickable {
                    if (isPlaying) player.pause() else player.play()
                }
        ) {


            AndroidView(
                factory = {
                    PlayerView(it).apply {
                        this.player = player
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

                    }
                },
            )

            IconButton(
                onClick = {
                    isLike = !isLike
                },
                modifier = Modifier
                    .size(64.dp)

                    .align(Alignment.CenterEnd)
            ) {
                Icon(
                    painter = painterResource(R.drawable.heart),
                    null,
                    tint = if (isLike) Color(0xFFFF0000) else Color(0x51FFFFFF),
                    modifier = Modifier
                        .size(64.dp)

                )
            }


            if (!isPlaying) {

                Icon(
                    painter = painterResource(R.drawable.player_play),
                    null,
                    tint = Color(0x51FFFFFF),
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}