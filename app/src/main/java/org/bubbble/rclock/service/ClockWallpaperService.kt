package org.bubbble.rclock.service

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.service.wallpaper.WallpaperService
import android.util.DisplayMetrics
import android.view.SurfaceHolder
import android.view.WindowManager
import org.bubbble.rclock.RClock
import org.bubbble.rclock.drawable.ClockDrawable
import org.bubbble.rclock.utils.dp

/**
 * @author Andrew
 * @date 2020/08/19 8:57
 */
class ClockWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return ClockEngine()
    }

    inner class ClockEngine : Engine(), Runnable {

        /**
         * 时钟默认的边距
         */
        private val clockMargin = 68F.dp

        /**
         * 负责绘制及动画的Drawable
         */
        private val clockDrawable = ClockDrawable(RClock.context)

        /**
         * 对于屏幕中心X坐标
         */
        private var centerX = 0

        /**
         * 对于屏幕中心Y坐标
         */
        private var centerY = 0

        /**
         * 表盘最大半径，通过最小屏幕宽度/高度动态计算
         */
        private var dialRadius = 0F

        private var drawOk = false

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            if (RClock.style) { clockDrawable.setupLightColor() } else { clockDrawable.setupDarkColor() }
            Thread(this).start()
            drawOk = true
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                clockDrawable.start()
                drawOk = true
            } else {
                clockDrawable.stop()
                drawOk = false
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            drawOk = false
        }

        override fun onDestroy() {
            super.onDestroy()
            clockDrawable.stop()
        }

        override fun run() {

            val holder = surfaceHolder
            if (holder!= null) {

                val displayMetrics = DisplayMetrics()
                val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                windowManager.defaultDisplay.getRealMetrics(displayMetrics)

                centerY = displayMetrics.heightPixels / 2
                centerX = displayMetrics.widthPixels / 2

                val canvas = holder.lockCanvas()
                dialRadius = if (displayMetrics.widthPixels >= displayMetrics.heightPixels) {
                    centerY - clockMargin
                } else {
                    centerX - clockMargin
                }
                clockDrawable.draw(centerX, centerY, dialRadius, canvas)
                holder.unlockCanvasAndPost(canvas)

                while (true) {
                    if (drawOk) {
                        val canvasOh = holder.lockCanvas()
                        clockDrawable.draw(canvasOh)
                        holder.unlockCanvasAndPost(canvasOh)
                    }
                }
            }
        }
    }
}