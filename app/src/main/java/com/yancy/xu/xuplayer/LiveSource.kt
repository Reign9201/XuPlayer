package com.yancy.xu.xuplayer

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 *
 * @date: 2024/1/17
 * @author: XuYanjun
 */
@Entity(primaryKeys = ["title"], tableName = "live_source")
data class LiveSource(
    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "source")
    val source: Set<String>,

    @ColumnInfo("invalid")
    val invalid: Boolean = false,

    @ColumnInfo("isHd")
    val isHd: Boolean = title.contains("1080")

)