package org.bubbble.rclock

import android.app.Application
import android.content.Context

/**
 * @author Andrew
 * @date 2020/08/19 10:20
 */
class RClock : Application() {

    companion object {

        lateinit var context: Context
        var style = false
    }


    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

}