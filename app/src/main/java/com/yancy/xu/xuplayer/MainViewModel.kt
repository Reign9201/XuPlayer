package com.yancy.xu.xuplayer

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 *
 * @date: 2024/1/17
 * @author: XuYanjun
 */
class MainViewModel : ViewModel() {
    private val _liveSources = MutableStateFlow<List<LiveSource>>(emptyList())
    val liveSources: StateFlow<List<LiveSource>> = _liveSources
    fun loadLiveSources(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
          // val localSource =  M3UDownload.parseFileFromAssets(context,"source.json")
           val file =  M3UDownload.download(File(context.filesDir,"liveSource.m3u").path)
            Log.e("Yancy","file:$file")
           val cloudSource =  M3UDownload.m3uFileParse(file.getOrNull())
            Log.e("Yancy","cloudSource:$cloudSource")

            _liveSources.value = cloudSource
        }
    }

}