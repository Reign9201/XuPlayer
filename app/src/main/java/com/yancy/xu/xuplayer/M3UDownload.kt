package com.yancy.xu.xuplayer

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 *
 * @date: 2024/1/17
 * @author: XuYanjun
 */
object M3UDownload {


    private val okHttpClient by lazy { OkHttpClient() }

    suspend fun download(url:String, targetPath: String): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).execute().use { response ->
                val file = File(targetPath)
                val buf = ByteArray(2048)
                var len: Int
                response.body?.byteStream()?.use { inputStream ->
                    FileOutputStream(file).use { fileOut ->
                        while (inputStream.read(buf).also { len = it } != -1) {
                            fileOut.write(buf, 0, len)
                        }
                        fileOut.flush()
                    }
                }
                file
            }
        }
    }



    fun parseFileFromAssets(context: Context, fileName: String): Set<LiveSource> {
        val list = mutableSetOf<LiveSource>()
        try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            val data = JSONObject(String(buffer))
            data.keys().forEach { key ->
                data.optJSONArray(key)?.run {
                    (0 until length()).map { index ->
                        this.optString(index)
                    }
                }?.run {
                    list.add(LiveSource(key, this.toSet()))
                }
            }
        } catch (e: Exception) {
            Log.e("Yancy", "error:${e.stackTraceToString()}")
        }
        return list
    }
}

fun File?.m3uFileParse(): Set<LiveSource> {
    val liveMap = mutableMapOf<String, LiveSource>()
    var tempTitle = ""
    var tempUrl: String
    this?.forEachLine {
        if (it.startsWith("#EXTINF")) {
            tempTitle = it.split(",").getOrNull(1) ?: ""
        } else if (it.startsWith("http")) {
            tempUrl = it
            if (tempTitle.isNotEmpty()) {
                if (liveMap.containsKey(tempTitle)) {
                    liveMap[tempTitle] = liveMap[tempTitle]!!.run {
                        LiveSource(tempTitle, this.source.plus(tempUrl))
                    }
                } else {
                    liveMap[tempTitle] = LiveSource(tempTitle, setOf(tempUrl))
                }
            }
        }
    }
    return liveMap.values.toSet()
}