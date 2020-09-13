package org.bubbble.rclock.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import org.bubbble.rclock.drawable.ClockDrawable
import org.bubbble.rclock.utils.dp
import org.bubbble.rclock.utils.logger

/**
 * @author Andrew
 * @date 2020/08/14 20:26
 */
class ClockView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    View(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    /**
     * 时钟默认的边距
     */
    private val clockMargin = 68F.dp

    /**
     * 负责绘制及动画的Drawable
     */
    private val clockDrawable = ClockDrawable(context)

    /**
     * 对于View中心X坐标
     */
    private var centerX = 0

    /**
     * 对于View中心Y坐标
     */
    private var centerY = 0

    /**
     * 表盘最大半径，通过最小屏幕宽度/高度动态计算
     */
    private var dialRadius = 0F

    override fun onDraw(canvas: Canvas?) {
        canvas?:return
        clockDrawable.draw(centerX, centerY, dialRadius, canvas)
    }

    private val clockAnimationStart = Runnable {
        clockDrawable.start()
    }
    init {
        clockDrawable.callback = this

        // 初始化View结束时启动动画
        post(clockAnimationStart)
    }

    fun start() {
        clockDrawable.start()
    }

    fun stop() {
        clockDrawable.stop()
    }


    fun setupDreamColor() {
        clockDrawable.setupDreamColor()
    }

    fun setupLightColor() {
        clockDrawable.setupLightColor()
    }

    fun setupDarkColor() {
        clockDrawable.setupDarkColor()
    }

    override fun invalidateDrawable(drawable: Drawable) {
        super.invalidateDrawable(drawable)
        if (drawable == clockDrawable) {
            invalidate()
            invalidateOutline()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // 得到中心坐标X
        centerX = measuredWidth / 2
        // 得到中心坐标Y
        centerY = measuredHeight / 2

        logger("measuredWidth: $measuredWidth measuredHeight: $measuredHeight")

        // 计算最大半径
        dialRadius = if (measuredWidth >= measuredHeight) {
            centerY - clockMargin
        } else {
            centerX - clockMargin
        }
    }
}