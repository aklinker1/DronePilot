package com.klinker.android.drone_pilot

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AppCompatDelegate
import android.widget.Switch
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var strafeXPreview : TextView
    private lateinit var strafeYPreview : TextView
    private lateinit var anglePreview : TextView
    private lateinit var liftPreview : TextView
    private lateinit var limiter : Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<JoyStickView>(R.id.strife_joystick).setListener(strafeListener)
        findViewById<JoyStickView>(R.id.alt_angle_joystick).setListener(altListener)

        strafeXPreview = findViewById(R.id.strafe_x)
        strafeYPreview = findViewById(R.id.strafe_y)
        anglePreview = findViewById(R.id.angle)
        liftPreview = findViewById(R.id.lift)
        limiter = findViewById(R.id.limiter)
    }

    private val strafeListener = object : JoyStickView.JoyStickListener {
        override fun onMove(joyStick: JoyStickView?, x: Double, y: Double) {
            var m = 1.0
            if (limiter.isChecked) m = 0.5
            val xp = "Strafe X: ${Math.round(m * x * 100)}%"
            val yp = "Strafe Y: ${Math.round(m * y * 100)}%"
            strafeXPreview.text = xp
            strafeYPreview.text = yp
        }
        override fun onTap() {}
        override fun onDoubleTap() {}
    }

    private val altListener = object : JoyStickView.JoyStickListener {
        override fun onMove(joyStick: JoyStickView?, x: Double, y: Double) {
            var ml = 1.0
            var ma = 1.0
            if (limiter.isChecked) {
                ml = 0.75
                ma = 0.5
            }
            val lp = "Lift X: ${Math.round(ml * y * 100)}%"
            val ap = "Angle Y: ${Math.round(ma * x * 100)}%"
            anglePreview.text = ap
            liftPreview.text = lp
        }
        override fun onTap() {}
        override fun onDoubleTap() {}
    }
    
}