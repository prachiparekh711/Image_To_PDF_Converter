package com.example.imagetopdfkotlin.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import androidx.viewpager.widget.ViewPager

class CustomViewPager(context: Context?, attrs: AttributeSet?) :
    ViewPager(context!!, attrs) {
    var isPagingEnabled = true

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (isPagingEnabled) super.onTouchEvent(event) else false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return isPagingEnabled && super.onInterceptTouchEvent(event)
    }

    fun setDurationScroll(millis: Int) {
        try {
            val viewpager: Class<*> = ViewPager::class.java
            val scroller = viewpager.getDeclaredField("mScroller")
            scroller.isAccessible = true
            scroller[this] = OwnScroller(context, millis)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class OwnScroller(context: Context?, durationScroll: Int) :
        Scroller(context, DecelerateInterpolator()) {
        private var durationScrollMillis = 300
        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, durationScrollMillis)
        }

        init {
            durationScrollMillis = durationScroll
        }
    }
}
