package com.example.carromai

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.math.*

class MainActivity : ComponentActivity() {
    private lateinit var board: BoardView
    private lateinit var status: TextView
    private lateinit var mode: Spinner
    private val marks = mutableListOf<Mark>()
    private var image: Bitmap? = null
    private var recommendation = "Import a screenshot, mark the striker and target coin, then calculate a shot."

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { u ->
            contentResolver.openInputStream(u)?.use { image = BitmapFactory.decodeStream(it) }
            marks.clear()
            board.invalidate()
            status.text = if(image != null) "Screenshot loaded. Mark the striker and coins on the image." else "Could not load image."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(16,16,16,12)
        }
        root.addView(TextView(this).apply {
            text="CARROM AI  •  ALL-IN-ONE"
            textSize=23f; setTextColor(Color.rgb(67,35,17)); setTypeface(null,android.graphics.Typeface.BOLD)
        })
        root.addView(TextView(this).apply {
            text="Disc Pool screenshot planner + floating in-game strategy assistant"
            textSize=13f; setPadding(0,4,0,10)
        })
        val top=LinearLayout(this)
        top.addView(Button(this).apply { text="Import screenshot"; setOnClickListener { pickImage.launch("image/*") } }, LinearLayout.LayoutParams(0,-2,1f))
        top.addView(Button(this).apply { text="Start floating AI"; setOnClickListener { startFloating() } }, LinearLayout.LayoutParams(0,-2,1f))
        root.addView(top)

        mode=Spinner(this).apply {
            adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,
                listOf("Mark striker","Mark target coin","Mark other coin / obstacle","Mark pocket"))
        }
        root.addView(mode)
        board=BoardView(this)
        root.addView(board,LinearLayout.LayoutParams(-1,0,1f))
        val controls=LinearLayout(this)
        controls.addView(Button(this).apply { text="Undo"; setOnClickListener { if(marks.isNotEmpty()) marks.removeAt(marks.lastIndex); board.invalidate(); status.text="Last mark removed." } })
        controls.addView(Button(this).apply { text="Clear"; setOnClickListener { marks.clear(); board.shot=null; board.invalidate(); status.text="Marks cleared." } })
        controls.addView(Button(this).apply { text="Find shot"; setOnClickListener { analyse() } })
        root.addView(controls)
        status=TextView(this).apply { text=recommendation; textSize=13f; setPadding(0,8,0,6) }
        root.addView(status)
        root.addView(TextView(this).apply {
            text="How to use: import your board screenshot, mark the striker, target coin and nearest pocket. The app estimates a line and relative power. Results are guidance, not guaranteed physics."
            textSize=11f
        })
        setContentView(root)
    }

    private fun startFloating() {
        if(Settings.canDrawOverlays(this)) {
            startService(Intent(this,OverlayService::class.java))
            status.text="Floating assistant started. Open Carrom Pool and tap the AI bubble."
        } else {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            status.text="Enable 'Allow display over other apps', return here, then tap Start floating AI."
        }
    }

    private fun analyse() {
        val striker=marks.lastOrNull{it.kind=="striker"}
        val target=marks.lastOrNull{it.kind=="target"}
        val pocket=marks.lastOrNull{it.kind=="pocket"}
        if(striker==null || target==null) {
            status.text="Mark one striker and one target coin first."
            return
        }
        val dx=target.x-striker.x
        val dy=target.y-striker.y
        val angle=Math.toDegrees(atan2(-dy,dx).toDouble())
        val distance=hypot(dx,dy)
        val power=when { distance<85 -> "LOW"; distance<180 -> "MEDIUM"; else -> "HIGH" }
        val pocketAdvice=if(pocket!=null) {
            val a=atan2(pocket.y-target.y,pocket.x-target.x)
            val ghostX=target.x-cos(a)*28f
            val ghostY=target.y-sin(a)*28f
            board.ghost=PointF(ghostX,ghostY)
            "Pocket line marked; aim the object coin toward the pocket."
        } else {
            board.ghost=null
            "Mark a pocket too for a more useful pocketing line."
        }
        board.shot=Pair(striker,target); board.invalidate()
        recommendation="Suggested striker-to-target angle: ${"%.1f".format(angle)}° • relative distance: ${"%.0f".format(distance)} px • starting power: $power. $pocketAdvice"
        status.text=recommendation
    }

    private data class Mark(val x:Float,val y:Float,val kind:String)

    private inner class BoardView(ctx: android.content.Context): View(ctx) {
        var shot:Pair<Mark,Mark>?=null
        var ghost:PointF?=null
        private val p=Paint(Paint.ANTI_ALIAS_FLAG)
        override fun onDraw(c:Canvas) {
            super.onDraw(c)
            c.drawColor(Color.rgb(239,231,218))
            val bmp=image
            if(bmp!=null) {
                val scale=min(width.toFloat()/bmp.width,height.toFloat()/bmp.height)
                val w=bmp.width*scale; val h=bmp.height*scale
                val dst=RectF((width-w)/2,(height-h)/2,(width+w)/2,(height+h)/2)
                c.drawBitmap(bmp,null,dst,p)
            } else {
                p.color=Color.DKGRAY; p.textSize=22f
                c.drawText("Import a Disc Pool screenshot",18f,height/2f,p)
            }
            marks.forEach { m ->
                p.color=when(m.kind){"striker"->Color.BLUE;"target"->Color.RED;"pocket"->Color.GREEN;else->Color.YELLOW}
                p.style=Paint.Style.FILL; c.drawCircle(m.x,m.y,12f,p)
                p.color=Color.WHITE; p.textSize=18f; c.drawText(m.kind.take(1).uppercase(),m.x-5,m.y+6,p)
            }
            shot?.let {
                p.color=Color.CYAN; p.strokeWidth=4f; p.style=Paint.Style.STROKE
                c.drawLine(it.first.x,it.first.y,it.second.x,it.second.y,p); p.style=Paint.Style.FILL
            }
            ghost?.let {
                p.color=Color.MAGENTA; p.style=Paint.Style.STROKE; p.strokeWidth=3f
                c.drawCircle(it.x,it.y,13f,p); p.style=Paint.Style.FILL
            }
        }
        override fun onTouchEvent(e:MotionEvent):Boolean {
            if(e.action==MotionEvent.ACTION_DOWN) {
                val kind=when(mode.selectedItemPosition){0->"striker";1->"target";3->"pocket";else->"coin"}
                marks.add(Mark(e.x,e.y,kind)); shot=null; ghost=null; invalidate()
                status.text="Marked $kind. Continue marking or tap Find shot."
                return true
            }
            return true
        }
    }
}
