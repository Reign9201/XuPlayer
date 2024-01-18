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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.yancy.xu.xuplayer.compose.CenterText
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 *
 * @date: 2024/1/16
 * @author: XuYanjun
 */
@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {

    private val viewModel by viewModel<MainViewModel>()

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
                    val filterConditions = remember {
                        mutableStateOf(LiveSourceFilter.NORMAL)
                    }
                    val liveSources = viewModel.liveSources.collectAsState(emptyList())
                    Row(Modifier.padding(paddingValues)) {
                        if (isFullScreen.value.not()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .background(Color.Blue)
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .clickable {
                                            filterConditions.value = viewModel.selectFilter(filterConditions.value)
                                        }
                                ) {
                                    Text(
                                        text = when (filterConditions.value) {
                                            LiveSourceFilter.NORMAL -> "所有节目"
                                            LiveSourceFilter.HD -> "高清节目"
                                            LiveSourceFilter.INVALID -> "有效节目"
                                            LiveSourceFilter.CCTV -> "央视节目"
                                            LiveSourceFilter.CCTV_HD -> "央视高清"
                                        },
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight()
                                        .background(Color.Blue.copy(alpha = 0.1f))
                                ) {
                                    items(liveSources.value.filter {
                                        when (filterConditions.value) {
                                            LiveSourceFilter.NORMAL -> true
                                            LiveSourceFilter.HD -> it.isHd
                                            LiveSourceFilter.INVALID -> it.invalid.not()
                                            LiveSourceFilter.CCTV -> it.title.contains("CCTV")
                                            LiveSourceFilter.CCTV_HD -> it.title.contains("CCTV") && it.isHd
                                        }
                                    }) {
                                        Box(
                                            contentAlignment = Alignment.CenterStart,
                                            modifier = Modifier
                                                .background(if (selectedItem.value?.title == it.title) Color.Red.copy(alpha = 0.1f) else Color.Blue.copy(alpha = 0.1f))
                                                .fillMaxWidth()
                                                .height(40.dp)
                                        ) {
                                            CenterText(
                                                text = it.title, modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(40.dp)
                                                    .padding(5.dp)
                                                    .clickable {
                                                        selectedItem.value = it
                                                    },
                                                contentAlignment = Alignment.CenterStart
                                            )
                                            CenterText(
                                                text = if (it.invalid) "Err" else "OK",
                                                style = androidx.compose.ui.text.TextStyle(
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center,
                                                ),
                                                modifier = Modifier
                                                    .align(Alignment.CenterEnd)
                                                    .background(
                                                        if (it.invalid) Color.Red.copy(alpha = 0.5f) else Color.Green.copy(alpha = 0.5f)
                                                    )
                                                    .width(30.dp)
                                                    .fillMaxHeight()

                                            )
                                            Spacer(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .background(Color.Black)
                                                    .fillMaxWidth()
                                                    .height(0.5.dp)
                                            )
                                        }
                                    }
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
                                    val sources = liveSource.source.toList()
                                    view.setUp(sources[0], true, liveSource.title)
                                    view.startPlayLogic()
                                    view.setVideoAllCallBack(object : GSYSampleCallBack() {
                                        var retryTime = 0
                                        override fun onPlayError(url: String?, vararg objects: Any?) {
                                            if (liveSource.source.size - 1 > retryTime) {
                                                retryTime++
                                                view.setUp(sources[retryTime], true, liveSource.title)
                                                view.startPlayLogic()
                                            } else {
                                                // 源失效
                                                viewModel.updateSourceInvalid(liveSource.title, true)
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