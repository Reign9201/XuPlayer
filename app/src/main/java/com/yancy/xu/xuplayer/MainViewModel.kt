package com.yancy.xu.xuplayer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yancy.xu.xuplayer.db.LiveSourceDb
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 *
 * @date: 2024/1/17
 * @author: XuYanjun
 */
class MainViewModel(private val db: LiveSourceDb) : ViewModel() {
    private val zhoUrl = "https://iptv-org.github.io/iptv/languages/zho.m3u"
    private val cnUrl = "https://iptv-org.github.io/iptv/countries/cn.m3u"
    val liveSources: Flow<List<LiveSource>> = db.liveSourceDao().queryAllLiveSourceFlow()


    fun loadLiveSources(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = coroutineScope {
                val zhoSource = async { M3UDownload.download(zhoUrl, File(context.filesDir, "zho.m3u").path) }
                val cnSource = async { M3UDownload.download(cnUrl, File(context.filesDir, "cn.m3u").path) }
                combineSource(
                    zhoSource.await().getOrNull().m3uFileParse(),
                    cnSource.await().getOrNull().m3uFileParse()
                )
            }
            filterDuplicateSource(result)
        }
    }

    fun updateSourceInvalid(title: String, invalid: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            db.liveSourceDao().updateLiveSource(title, invalid)
        }
    }

    fun selectFilter(currentFilter: LiveSourceFilter): LiveSourceFilter {
        val values = LiveSourceFilter.values()
        var nextIndex = 0
        values.forEachIndexed { index, filter ->
            if (currentFilter == filter) {
                nextIndex = index + 1
                return@forEachIndexed
            }
        }
        if (nextIndex >= values.size) {
            nextIndex = 0
        }
        return values[nextIndex]
    }

    private fun combineSource(source1: Collection<LiveSource>, source2: Collection<LiveSource>): List<LiveSource> {
        val map = source1.associateBy { it.title }.toMutableMap()
        source2.forEach {
            if (map.containsKey(it.title)) {
                map[it.title] = LiveSource(it.title, map[it.title]!!.source.plus(it.source))
            } else {
                map[it.title] = LiveSource(it.title, it.source)
            }
        }
        return map.values.toList()
    }


    private fun filterDuplicateSource(sources: List<LiveSource>) {
        val sourceDao = db.liveSourceDao()
        val localSources = sourceDao.queryAllLiveSource().associateBy { it.title }
        val newSources = mutableListOf<LiveSource>()
        sources.forEach {
            if (localSources.containsKey(it.title)) {
                if (localSources[it.title]!!.source != it.source) {
                    newSources.add(it)
                }
            } else {
                newSources.add(it)
            }
        }
        sourceDao.insertLiveSource(newSources)
    }
}