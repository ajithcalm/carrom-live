package com.example.carromai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
        }

        layout.addView(TextView(this).apply {
            text = "CARROM AI ASSISTANT"
            textSize = 25f
            gravity = Gravity.CENTER
        })

        layout.addView(TextView(this).apply {
            text = "Floating carrom strategy assistant"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 24)
        })

        layout.addView(Button(this).apply {
            text = "Start Floating Assistant"
            setOnClickListener {
                if (Settings.canDrawOverlays(this@MainActivity)) {
                    startService(
                        Intent(this@MainActivity, OverlayService::class.java)
                    )
                    Toast.makeText(
                        this@MainActivity,
                        "Floating assistant started",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                    )
                }
            }
        })

        setContentView(layout)
    }
}
