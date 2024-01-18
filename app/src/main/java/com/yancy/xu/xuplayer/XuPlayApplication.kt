package com.yancy.xu.xuplayer

import android.app.Application
import com.yancy.xu.xuplayer.db.LiveSourceDb
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

/**
 *
 * @date: 2024/1/18
 * @author: XuYanjun
 */
class XuPlayApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@XuPlayApplication)
        }


        loadKoinModules(arrayListOf(
            module {
                single { LiveSourceDb.create(this@XuPlayApplication, "live_source.db") }
                viewModel { MainViewModel(get()) }
            }
        ))
    }
}