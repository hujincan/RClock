package org.bubbble.rclock.drawable

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import org.bubbble.rclock.R
import org.bubbble.rclock.utils.dp
import org.bubbble.rclock.utils.logger
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author Andrew
 * @date 2020/08/14 20:45
 */
class ClockDrawable(private val context: Context) : Drawable(), Animatable, ValueAnimator.AnimatorUpdateListener {

    /**
     * 小时指针的位置
     */
    private var hourPosition = 0F

    /**
     * 分钟指针的位置
     */
    private var minutePosition = 0F

    /**
     * 秒指针的位置
     */
    private var secondPosition = 0F

    /**
     * 用于更新主要分钟刻度颜色的变量
     */
    private var minute = 0F

    /**
     * 用于更新次要秒点刻度颜色的变量
     */
    private var second = 0F

    /**
     * 一个专门存储三个秒点透明度的列表
     */
    private val secondCircleList = FloatArray(3)

    /**
     * 表盘半径
     */
    private var dialRadius = -1F

    /**
     * 小时圆点的半径
     */
    private var hourRadius = 16F.dp

    /**
     * 时钟的主要画笔
     */
    private val paint = Paint().apply {
        // 抗锯齿
        isAntiAlias = true
        // 填充
        style = Paint.Style.FILL
        // 颜色
        color = ContextCompat.getColor(context, R.color.clockDial)
    }

    private var strokeSize = 0F

    /**
     * 时钟的主要画笔
     */
    private val dialPaint = Paint().apply {
        // 抗锯齿
        isAntiAlias = true
        // 填充
        style = Paint.Style.STROKE
        // 颜色
        color = Color.WHITE
        strokeWidth = strokeSize
    }

    /**
     * 是否正在动画
     */
    private var isRunning = false

    /**
     * 时钟的阴影画笔
     */
    private val gradientPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        shader = LinearGradient(0F, centerY.toFloat(), 0F, dialRadius + centerY + PROJECTION_LENGTH,
            intArrayOf(ContextCompat.getColor(context, R.color.clockShadowStart), ContextCompat.getColor(context, R.color.transparent)),
            floatArrayOf(0F, 0.8F), Shader.TileMode.CLAMP)
    }

    /**
     * calendar用于获取各种时间格式
     */
    private val calendar: Calendar by lazy {
        Calendar.getInstance()
    }

    /**
     * 秒针以及分针的动画
     */
    private val animator: ValueAnimator by lazy {
        ValueAnimator().apply {
            addUpdateListener(this@ClockDrawable)
        }
    }

    // --------------------------------------------------

    private var clockShadowStart = ContextCompat.getColor(context, R.color.clockShadowStart)
    private var clockPointUnFocus = ContextCompat.getColor(context, R.color.clockPointUnFocus)
    private var clockBackground = ContextCompat.getColor(context, R.color.clockBackground)
    private var clockDial = ContextCompat.getColor(context, R.color.clockDial)
    private var clockPoint = ContextCompat.getColor(context, R.color.clockPoint)

    private var isDreamMod = false

    fun setupDreamColor() {
        clockBackground = Color.parseColor("#000000")
        clockPoint = Color.parseColor("#D7EFFE")
        clockPointUnFocus = Color.parseColor("#33D7EFFE")
        strokeSize = 20F.dp
        dialPaint.strokeWidth = strokeSize
        isDreamMod = true
    }

    fun setupLightColor() {
        clockShadowStart = ContextCompat.getColor(context, R.color.clockShadowStartLight)
        clockPointUnFocus = ContextCompat.getColor(context, R.color.clockPointUnFocusLight)
        clockBackground = ContextCompat.getColor(context, R.color.clockBackgroundLight)
        clockDial = ContextCompat.getColor(context, R.color.clockDialLight)
        clockPoint = ContextCompat.getColor(context, R.color.clockPointLight)
    }

    fun setupDarkColor() {
        clockShadowStart = ContextCompat.getColor(context, R.color.clockShadowStart)
        clockPointUnFocus = ContextCompat.getColor(context, R.color.clockPointUnFocus)
        clockBackground = ContextCompat.getColor(context, R.color.clockBackground)
        clockDial = ContextCompat.getColor(context, R.color.clockDial)
        clockPoint = ContextCompat.getColor(context, R.color.clockPoint)
    }

    companion object {
        // 分钟圆点距离表盘外部的距离
        private val MINUTE_MARGIN = 40F.dp
        // 不在当前分钟的圆点大小
        private val UN_SELECT_MINUTE_SIZE = 1.8F.dp
        // 当前所在分钟的圆点大小
        private val SELECT_MINUTE_SIZE = 6.5F.dp
        // 秒经过时的圆点大小
        private val SECOND_SELECT_SIZE = 1F.dp
        // 秒钟圆点距离表盘外部的距离
        private val SECOND_MARGIN = 12F.dp
        // 当前所在秒钟的圆点大小
        private val SECOND_SIZE = 6F.dp
        // 小时圆点距离表盘外部的距离
        private val HOUR_MARGIN = 40F.dp
        // 投影最外侧相对于表盘外部的距离
        private val PROJECTION_LENGTH = 130F.dp

        private val BREATHE = 26.dp

        private const val ONE_SECOND = 1000L
        private const val ONE_MINUTE = ONE_SECOND * 60
    }

    // 画布的中心X坐标
    private var centerX = 0
    // 画布的中心Y坐标
    private var centerY = 0

    private var lastUpdate = -1F

    fun draw(centerX: Int, centerY: Int, dialRadius: Float, canvas: Canvas) {

        if (isDreamMod) {
            this.dialRadius = (dialRadius - MINUTE_MARGIN) - (value * BREATHE)
            // 2分钟一次随机更新位置
            this.centerX = centerX

            // 第一次的时候等于centerY
            if (lastUpdate < 0) {
                this.centerY = centerY
                lastUpdate = 0F
            }

            if (minute % 2F == 0F && minute != lastUpdate) {
                lastUpdate = minute
                // 纵向的随机
                this.centerY = centerY + Random().nextInt((centerY / 2)) - (centerY / 4)
            }

        } else {
            this.dialRadius = dialRadius - MINUTE_MARGIN
            this.centerX = centerX
            this.centerY = centerY
        }
        this.hourRadius = dialRadius / 8.2F

        logger("dialRadius: $dialRadius centerX: $centerX")
        gradientPaint.apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            shader = LinearGradient(0F, centerY.toFloat(), 0F, dialRadius + centerY + PROJECTION_LENGTH,
                intArrayOf(clockShadowStart, ContextCompat.getColor(context, R.color.transparent)),
                floatArrayOf(0F, 0.8F), Shader.TileMode.CLAMP)
        }
        draw(canvas)
    }

    override fun draw(canvas: Canvas) {

        paint.alpha = 255
        // 绘制背景
        canvas.drawColor(clockBackground)

        if (!isDreamMod) {
            // 绘制表盘长投影
            canvas.save()
            canvas.rotate(-45F, centerX.toFloat(), centerY.toFloat())
            canvas.drawRect(centerX - dialRadius, centerY.toFloat(), centerX + dialRadius, centerY + PROJECTION_LENGTH + dialRadius, gradientPaint)
            canvas.restore()
        }

        // 绘制表盘
        paint.color = clockDial
        if (isDreamMod) {
            canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), dialRadius - (strokeSize / 2), dialPaint)
        } else {
            canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), dialRadius, paint)
        }

        // 绘制整分圆点刻度
        canvas.save()
        paint.color = clockPoint
        for (value in 0 until 12) {

            // 下次将会到达的点变黯淡
            if (!(((value - 1) * 5) <= minute && (value * 5) > minute)) {
                canvas.drawCircle(centerX.toFloat(), centerY - dialRadius - MINUTE_MARGIN, UN_SELECT_MINUTE_SIZE, paint)
                canvas.rotate(30F, centerX.toFloat(), centerY.toFloat())
                logger("value : $value min : $minutePosition")
            } else {
                paint.color = clockPointUnFocus
                canvas.drawCircle(centerX.toFloat(), centerY - dialRadius - MINUTE_MARGIN, UN_SELECT_MINUTE_SIZE, paint)
                canvas.rotate(30F, centerX.toFloat(), centerY.toFloat())
                paint.color = clockPoint
                logger("------- value : $value min : $minutePosition minute : $minute")
            }
        }
        canvas.restore()

        // 绘制小时圆点指针位置
        canvas.save()
        paint.color = Color.WHITE
        canvas.rotate(hourPosition, centerX.toFloat(), centerY.toFloat())
        canvas.drawCircle(centerX.toFloat(), centerY - dialRadius + HOUR_MARGIN + (strokeSize / 2), hourRadius, paint)
        canvas.restore()
        logger("hourPosition : $hourPosition")

        // 绘制分钟圆点指针位置
        canvas.save()
        paint.color = clockPoint
        canvas.rotate(minutePosition, centerX.toFloat(), centerY.toFloat())
        canvas.drawCircle(centerX.toFloat(), centerY - dialRadius - MINUTE_MARGIN, SELECT_MINUTE_SIZE, paint)
        canvas.restore()

        // 绘制秒钟圆点指针位置
        canvas.save()
        paint.color = clockDial
        canvas.rotate(secondPosition, centerX.toFloat(), centerY.toFloat())
        canvas.drawCircle(centerX.toFloat(), centerY - dialRadius - SECOND_MARGIN, SECOND_SIZE, paint)
        canvas.restore()

        // 绘制秒钟圆点经过的刻度
        for (index in secondCircleList.indices) {
            canvas.save()
            paint.color = clockPoint
            paint.alpha = (secondCircleList[index]).toInt()
            canvas.rotate(second + (index * 6F), centerX.toFloat(), centerY.toFloat())
            canvas.drawCircle(centerX.toFloat(), centerY - dialRadius - MINUTE_MARGIN, SECOND_SELECT_SIZE, paint)
            canvas.restore()
        }
        paint.alpha = 255
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        invalidateSelf()
    }

    override fun setAlpha(p0: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

    override fun setColorFilter(p0: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun isRunning(): Boolean {
        return isRunning && animator.isRunning
    }

    /**
     * 开始动画
     */
    override fun start() {
        if (!isRunning) {
            animator.setFloatValues(0F, 1F, 0F)
            animator.duration = ONE_MINUTE * 3
            animator.interpolator = LinearInterpolator()
            animator.repeatCount = ValueAnimator.INFINITE
            animator.start()
            isRunning = !isRunning
        }
    }

    /**
     * 停止所有动画
     */
    override fun stop() {
        animator.cancel()
        isRunning = false
    }

    private var value = 0F

    override fun onAnimationUpdate(p0: ValueAnimator?) {
        // 更新各种时间值，确保位置同步
        calendar.timeInMillis = System.currentTimeMillis()
        hourPosition = calendar.get(Calendar.HOUR_OF_DAY) * 30F
        second = calendar.get(Calendar.SECOND).toFloat() * 6F
        minute = calendar.get(Calendar.MINUTE).toFloat()

        val milliSecond = calendar.get(Calendar.MILLISECOND).toFloat()
        val secondNow = calendar.get(Calendar.SECOND).toFloat()
        secondPosition = ((calendar.get(Calendar.SECOND).toFloat() / 60F) * 360F) + ((milliSecond / 1000) * 6F)
        minutePosition = ((calendar.get(Calendar.MINUTE).toFloat() / 60F) * 360F) + ((secondNow / 60) * 6F)

        secondCircleList[0] = 255F * (1F - ((milliSecond / 1000) * 1F)) // 刚刚过的（1 - 0）
        secondCircleList[1] = 255F * (0.3F + ((milliSecond / 1000) * 0.7F)) // 马上要到的（0.3 - 1）
        secondCircleList[2] = 255F * ((milliSecond / 1000) * 0.3F) // 下次到的（微微显示0 - 0.3）

        value = p0?.animatedValue as Float

        invalidateSelf()
    }
}