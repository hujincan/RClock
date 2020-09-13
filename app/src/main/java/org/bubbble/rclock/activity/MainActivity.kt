package org.bubbble.rclock.activity

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.bubbble.rclock.R
import org.bubbble.rclock.RClock
import org.bubbble.rclock.service.ClockWallpaperService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        dreamButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_DREAM_SETTINGS));
        }

        wallpaperButton.setOnClickListener {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(this, ClockWallpaperService::class.java))
            startActivity(intent)
        }

        codeLink.setOnClickListener {
            startHttp("https://github.com/hujincan/RClock")
        }

        styleSwitch.setOnCheckedChangeListener { _, b ->
            if (b) {
                clockView.setupLightColor()
                RClock.style = true
            } else {
                clockView.setupDarkColor()
                RClock.style = false
            }
            buttonStyle(b)
        }
    }

    private fun buttonStyle(style: Boolean) {
        var color = ContextCompat.getColor(this, R.color.clockPoint)
        if (style) {
            color = ContextCompat.getColor(this, R.color.clockPointLight)
        }
        dreamButton.setTextColor(color)
        wallpaperButton.setTextColor(color)
        codeLink.setTextColor(color)
    }

    override fun onDestroy() {
        super.onDestroy()
        clockView.stop()
    }


    //打开链接
    private fun startHttp(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(uri)
        startActivity(intent)
    }
}