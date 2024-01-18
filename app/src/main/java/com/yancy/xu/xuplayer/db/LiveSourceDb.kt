package com.yancy.xu.xuplayer.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yancy.xu.xuplayer.LiveSource

/**
 *
 * @date: 2024/1/18
 * @author: XuYanjun
 */

@Database(
    entities = [
        LiveSource::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(LiveSourceConverters::class)
abstract class LiveSourceDb : RoomDatabase() {
    abstract fun liveSourceDao(): LiveSourceDao

    companion object {
        fun create(context: Context, dbName: String): LiveSourceDb {
            return Room.databaseBuilder(context, LiveSourceDb::class.java, dbName)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}