package com.example.imagetopdfkotlin.views

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import com.example.imagetopdfkotlin.Utils.Util.Companion.mDoodleColor
import com.example.imagetopdfkotlin.Utils.Util.Companion.mPenWidth
import java.util.*

class DrawingView : View {
    private var mPaint: Paint? = null
    var paintBit: Bitmap? = null
        private set
    private var mEraserPaint: Paint? = null
    private var mPaintCanvas: Canvas? = null
    private var last_x = 0f
    private var last_y = 0f

    private val mBitmapBrushDimensions: Vector2? = null
    var bitmap: Bitmap? = null
    private val mPositions: MutableList<Vector2> =
        ArrayList<Vector2>(100)
    private val mPositions1: MutableList<Vector2> =
        ArrayList<Vector2>(100)
    private val TOUCH_TOLERANCE = 4f
    private var mPath: Path? = null
    private val paths = ArrayList<Path>()
    private val undonePaths = ArrayList<Path>()
    private var mX = 0f
    private var mY = 0f
    private val mCanvas: Canvas? = null

    private class Vector2(val x: Float, val y: Float)

    companion object {
        var isEraser = false
    }

    private var mColor = 0

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (paintBit == null) {
            generatorBit()
        }
    }

    private fun generatorBit() {
        paintBit = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        mPaintCanvas = Canvas(paintBit!!)
    }

    private fun init(context: Context) {
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.strokeJoin = Paint.Join.ROUND
        mPaint!!.strokeCap = Paint.Cap.ROUND
        mPaint!!.color = mDoodleColor
        mPaint!!.strokeWidth = mPenWidth
        mEraserPaint = Paint()
        mEraserPaint!!.alpha = 0
        mEraserPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        mEraserPaint!!.isAntiAlias = true
        mEraserPaint!!.isDither = true
        mEraserPaint!!.style = Paint.Style.STROKE
        mEraserPaint!!.strokeJoin = Paint.Join.ROUND
        mEraserPaint!!.strokeCap = Paint.Cap.ROUND
        mEraserPaint!!.strokeWidth = mPenWidth

        mPath = Path()
        paths.add(mPath!!)
        setLayerType(LAYER_TYPE_HARDWARE, mPaint)
    }

    fun setColor(color: Int) {
        mColor = mDoodleColor
        mPaint!!.color = mColor
    }

    fun setWidth(width: Float) {
        mPaint!!.strokeWidth = mPenWidth
    }

    fun setStrokeAlpha(alpha: Float) {
        mPaint!!.alpha = alpha.toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPaint!!.color = mDoodleColor
        mPaint!!.strokeWidth = mPenWidth
        mEraserPaint!!.strokeWidth = mPenWidth
//        if (paintBit != null) {
//            canvas.drawBitmap(paintBit!!, 0f, 0f, null)
//        }
        for (pos in mPositions) {
            canvas.drawBitmap(paintBit!!, pos.x - 20, pos.y - 50, null)
        }
        for (p in paths) {
            canvas.drawPath(p, mPaint!!)
        }
        canvas.drawPath(mPath!!, mPaint!!)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var ret = super.onTouchEvent(event)
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                undonePaths.clear()
                mPath!!.reset()
                mPath!!.moveTo(x, y)
                mX = x
                mY = y
                invalidate()
//                ret = true
//                last_x = x
//                last_y = y
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = Math.abs(x - mX)
                val dy = Math.abs(y - mY)
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    mPath!!.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
                    mX = x
                    mY = y
                }
                invalidate()
//                ret = true
//                mPaintCanvas!!.drawLine(
//                    last_x, last_y, x, y,
//                    (if (isEraser) mEraserPaint else mPaint)!!
//                )
//                last_x = x
//                last_y = y
//                this.postInvalidate()
            }
            MotionEvent.ACTION_CANCEL -> ret = false
            MotionEvent.ACTION_UP -> {
                mPath!!.lineTo(mX, mY)
                mPaint?.let { mCanvas?.drawPath(mPath!!, it) }
                paths.add(mPath!!)
                mPath = Path()
                invalidate()
            }
        }
        return ret
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (paintBit != null && !paintBit!!.isRecycled) {
            paintBit!!.recycle()
        }
    }

    fun setEraser(eraser: Boolean) {
        isEraser = eraser
        mPaint!!.color = if (eraser) Color.TRANSPARENT else mColor
    }

    fun setEraserStrokeWidth(width: Float) {
        mEraserPaint!!.strokeWidth = width
    }

    fun reset() {
        if (paintBit != null && !paintBit!!.isRecycled) {
            paintBit!!.recycle()
        }
        generatorBit()
    }

    fun onClickUndo() {
        if (paths.size > 0) {
            undonePaths.add(paths.removeAt(paths.size - 1))
            invalidate()
        }
        if (mPositions.size > 0) {
            mPositions1.add(mPositions.removeAt(mPositions.size - 1))
            invalidate()
        }
        //toast the user
    }

    fun onClickRedo() {
        if (undonePaths.size > 0) {
            paths.add(undonePaths.removeAt(undonePaths.size - 1))
            invalidate()
        }
        if (mPositions1.size > 0) {
            mPositions.add(mPositions1.removeAt(mPositions1.size - 1))
            invalidate()
        }
    }

}
