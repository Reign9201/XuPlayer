package com.yancy.xu.xuplayer

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer

/**
 *
 * @date: 2024/1/16
 * @author: XuYanjun
 */
@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {

    private val viewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        PlayConfig.init()
        viewModel.loadLiveSources(this)
        setContent {
            MaterialTheme {
                Scaffold(backgroundColor = Color.White) { paddingValues ->
                    val isFullScreen = remember {
                        mutableStateOf(false)
                    }
                    val selectedItem = remember {
                        mutableStateOf<LiveSource?>(null)
                    }
                    val liveSources = viewModel.liveSources.collectAsState()
                    Row(Modifier.padding(paddingValues)) {
                        if (isFullScreen.value.not()) {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(Color.Blue.copy(alpha = 0.1f))
                            ) {
                                items(liveSources.value) {
                                    Text(
                                        text = it.title, modifier = Modifier
                                            .background(if (selectedItem.value?.title == it.title) Color.Red.copy(alpha = 0.1f) else Color.Blue.copy(alpha = 0.1f))
                                            .fillMaxWidth()
                                            .height(30.dp)
                                            .padding(5.dp)
                                            .clickable {
                                                selectedItem.value = it
                                            },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        AndroidView(
                            modifier = Modifier
                                .weight(3f)
                                .fillMaxSize(),
                            factory = { context ->
                                StandardGSYVideoPlayer(context).apply {
                                    fullscreenButton.setOnClickListener {
                                        isFullScreen.value = !isFullScreen.value
                                    }
                                    backButton.setOnClickListener {
                                        if (isFullScreen.value) {
                                            isFullScreen.value = !isFullScreen.value
                                        }
                                    }
                                }
                            }, update = { view ->
                                selectedItem.value?.let { liveSource ->
                                    view.setUp(liveSource.source[0], true, liveSource.title)
                                    view.startPlayLogic()
                                    view.setVideoAllCallBack(object : GSYSampleCallBack() {
                                        var retryTime = 0
                                        override fun onPlayError(url: String?, vararg objects: Any?) {
                                            if (liveSource.source.size - 1 > retryTime) {
                                                retryTime++
                                                view.setUp(liveSource.source[retryTime], true, liveSource.title)
                                                view.startPlayLogic()
                                            }
                                        }
                                    })
                                }
                            })
                    }
                }
            }
        }
    }
}