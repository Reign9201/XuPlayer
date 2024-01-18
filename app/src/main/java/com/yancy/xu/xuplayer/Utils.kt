package com.yancy.xu.xuplayer

import org.koin.core.Koin
import org.koin.core.context.GlobalContext

/**
 *
 * @date: 2024/1/18
 * @author: XuYanjun
 */


fun getKoin() : Koin = GlobalContext.get()