
package com.example.carromai

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: LinearLayout? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        if (floatingView == null) {
            showFloatingWindow()
        }
        return START_NOT_STICKY
    }

    private fun showFloatingWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(Color.DKGRAY)
        }

        panel.addView(TextView(this).apply {
            text = "CARROM AI"
            textSize = 18f
            setTextColor(Color.WHITE)
        })

        panel.addView(TextView(this).apply {
            text = "Floating assistant is active."
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(0, 12, 0, 12)
        })

        panel.addView(Button(this).apply {
            text = "Direct Shot Advice"
            setOnClickListener {
                (panel.getChildAt(1) as TextView).text =
                    "Align the striker through the target coin towards the pocket."
            }
        })

        panel.addView(Button(this).apply {
            text = "Close"
            setOnClickListener {
                stopSelf()
            }
        })

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 20
            y = 200
        }

        floatingView = panel
        windowManager?.addView(panel, params)
    }

    override fun onDestroy() {
        floatingView?.let {
            windowManager?.removeView(it)
        }
        floatingView = null
        super.onDestroy()
    }
}
