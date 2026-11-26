package com.example.carromai

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs

class OverlayService:Service() {
    private lateinit var wm:WindowManager
    private var bubble:TextView?=null
    private var panel:LinearLayout?=null
    private lateinit var bp:WindowManager.LayoutParams

    override fun onBind(intent:Intent?):IBinder?=null
    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int {
        if(bubble==null) createBubble()
        return START_STICKY
    }
    private fun lp(w:Int,h:Int)=WindowManager.LayoutParams(
        w,h,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply { gravity=Gravity.TOP or Gravity.START; x=24; y=220 }

    private fun createBubble() {
        wm=getSystemService(WINDOW_SERVICE) as WindowManager
        val b=TextView(this).apply {
            text="AI"; textSize=17f; typeface=Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE); gravity=Gravity.CENTER
            setBackgroundColor(0xff1769aa.toInt()); setPadding(18,14,18,14)
        }
        bp=lp(-2,-2)
        var downX=0; var downY=0; var sx=0; var sy=0
        b.setOnTouchListener { _,e ->
            when(e.action) {
                MotionEvent.ACTION_DOWN->{downX=e.rawX.toInt();downY=e.rawY.toInt();sx=bp.x;sy=bp.y;true}
                MotionEvent.ACTION_MOVE->{bp.x=sx+e.rawX.toInt()-downX;bp.y=sy+e.rawY.toInt()-downY;wm.updateViewLayout(b,bp);true}
                MotionEvent.ACTION_UP->{if(abs(e.rawX.toInt()-downX)+abs(e.rawY.toInt()-downY)<8) toggle();true}
                else->false
            }
        }
        bubble=b; wm.addView(b,bp)
    }
    private fun toggle() { if(panel!=null) closePanel() else openPanel() }
    private fun openPanel() {
        val p=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL;setPadding(16,14,16,14);setBackgroundColor(0xee24150f.toInt()) }
        p.addView(TextView(this).apply { text="CARROM AI • QUICK STRATEGY";textSize=15f;typeface=Typeface.DEFAULT_BOLD;setTextColor(Color.WHITE) })
        val output=TextView(this).apply { text="Choose a situation.";textSize=13f;setTextColor(Color.WHITE);setPadding(0,8,0,8) }
        fun add(label:String,advice:String) {
            p.addView(Button(this).apply { text=label;setOnClickListener { output.text=advice } })
        }
        add("Direct shot","DIRECT: align striker centre through the target coin. Aim the coin towards the pocket centre. Begin with low-to-medium force.")
        add("Cushion / rebound","REBOUND: visualise equal entry and exit angles at the cushion. Check that the rebound path is not blocked. Use controlled medium force.")
        add("Blocked path","BLOCKED: avoid forcing a crowded line. Select another coin with a clear pocket path and avoid leaving an easy reply.")
        add("End game","END GAME: identify remaining coins, pocket the safest one first and plan the next striker position.")
        p.addView(output)
        p.addView(Button(this).apply { text="Close";setOnClickListener { closePanel() } })
        panel=p
        val pp=lp(310,-2).apply { x=70;y=260 }
        wm.addView(p,pp)
    }
    private fun closePanel() { panel?.let { try{wm.removeView(it)}catch(_:Exception){} };panel=null }
    override fun onDestroy() {
        closePanel()
        bubble?.let { try{wm.removeView(it)}catch(_:Exception){} }
        bubble=null
        super.onDestroy()
    }
}
