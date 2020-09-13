package org.bubbble.rclock.service

import android.os.Build
import android.service.dreams.DreamService
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.bubbble.rclock.R
import org.bubbble.rclock.view.ClockView

/**
 * @author Andrew
 * @date 2020/08/19 19:25
 */
class ClockDreamService : DreamService() {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isFullscreen = true
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        setContentView(R.layout.dream_layout)
        initView()
    }

    private lateinit var clockView: ClockView

    private fun initView() {
        clockView = findViewById(R.id.clockView)
        clockView.setupDreamColor()
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
        clockView.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        clockView.stop()
    }
}