package com.yancy.xu.xuplayer.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yancy.xu.xuplayer.LiveSource
import kotlinx.coroutines.flow.Flow

/**
 *
 * @date: 2024/1/18
 * @author: XuYanjun
 */

@Dao
interface LiveSourceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLiveSource(source: List<LiveSource>)

    @Query("select * from live_source")
    fun queryAllLiveSource(): List<LiveSource>

    @Query("select * from live_source")
    fun queryAllLiveSourceFlow(): Flow<List<LiveSource>>

    @Query("select * from live_source where invalid =:invalid")
    fun queryInvalidLiveSource(invalid: Boolean): Flow<List<LiveSource>>

    @Query("select * from live_source where isHd =:isHd")
    fun queryHdLiveSource(isHd: Boolean): Flow<List<LiveSource>>

    @Query("update live_source set invalid =:invalid where title =:title")
    fun updateLiveSource(title: String, invalid: Boolean)
}