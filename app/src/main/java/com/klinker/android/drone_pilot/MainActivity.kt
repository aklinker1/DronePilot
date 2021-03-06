package com.klinker.android.drone_pilot

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var strafeStick: JoyStickView
    private lateinit var liftStick: JoyStickView
    private lateinit var strafeXPreview: TextView
    private lateinit var strafeYPreview: TextView
    private lateinit var anglePreview: TextView
    private lateinit var liftPreview: TextView
    private lateinit var pingPreview: TextView
    private lateinit var ipAddress: EditText
    private lateinit var limiter: Switch
    private lateinit var vibrator: Vibrator
    private var isRunning: Boolean = false
    private var controller = Controller()
    private lateinit var queue: RequestQueue

    private var prevs: DoubleArray = doubleArrayOf(0.0, 0.0, 0.0, 0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        strafeStick = findViewById(R.id.strafe_joystick)
        liftStick = findViewById(R.id.alt_angle_joystick)
        strafeXPreview = findViewById(R.id.strafe_x)
        strafeYPreview = findViewById(R.id.strafe_y)
        anglePreview = findViewById(R.id.angle)
        liftPreview = findViewById(R.id.lift)
        pingPreview = findViewById(R.id.ping)
        limiter = findViewById(R.id.limiter)
        ipAddress = findViewById(R.id.ip_address)

        findViewById<JoyStickView>(R.id.strafe_joystick).setListener(strafeListener)
        findViewById<JoyStickView>(R.id.alt_angle_joystick).setListener(altListener)
        ipAddress.setOnEditorActionListener(ipListener)
        findViewById<CheckBox>(R.id.armed).setOnCheckedChangeListener(armListener)
        findViewById<CheckBox>(R.id.hover).setOnCheckedChangeListener(hoverListener)
        findViewById<CheckBox>(R.id.drop).setOnCheckedChangeListener(dropListener)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val ip = prefs.getString("ip-address", "192.168.86.35")
        ipAddress.setHint(ip)
        controller.setIPAddress(ip)
    }

    override fun onResume() {
        super.onResume()
        queue = Volley.newRequestQueue(this)
        isRunning = true

        Thread {
            while (isRunning) {
                Utils.sleep(1000)
                val request = PingRequest(
                        Response.Listener { response -> updatePing(response) },
                        Response.ErrorListener { error -> pingPreview.text = "No Ping" })
                queue.add(request)
            }
        }.start()

        Thread {
            while (isRunning) {
                Utils.sleep(100)
                val request = ControlRequest(
                        prevs[0], prevs[1], prevs[2], prevs[3],
                        Response.Listener { response -> println(response) },
                        Response.ErrorListener { error -> println(error) }
                )
                queue.add(request)
            }
        }.start()
    }

    override fun onPause() {
        super.onPause()
        isRunning = false

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().putString(
                "ip-address",
                ipAddress.hint.toString()
        ).apply()
    }

    private val strafeListener = object : JoyStickView.JoyStickListener {
        override fun onMove(joyStick: JoyStickView?, x: Double, y: Double) {
            var m = 1.0
            if (limiter.isChecked) m = 0.25
            val xDif = x*m - prevs[0]
            val yDif = y*m - prevs[1]
            prevs[0] = x*m
            prevs[1] = y*m
            val change = Math.sqrt(xDif * xDif + yDif * yDif)
            val timing = Math.round(map(change, 0.0, 1.0, 0.0, 50.0))
            if (timing > 0) vibrator.vibrate(timing)

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
                ml = 1.0
                ma = 0.5
            }
            val xDif = x*ma - prevs[2]
            val yDif = y*ml - prevs[3]
            prevs[2] = x*ma
            prevs[3] = y*ml
            val change = Math.sqrt(xDif * xDif + yDif * yDif)
            val timing = Math.round(map(change, 0.0, 1.0, 0.0, 50.0))
            if (timing > 0) vibrator.vibrate(timing)

            val lp = "Lift: ${Math.round(ml * y * 100)}%"
            val ap = "Angle: ${Math.round(ma * x * 100)}%"
            anglePreview.text = ap
            liftPreview.text = lp
        }

        override fun onTap() {}
        override fun onDoubleTap() {}
    }

    private val armListener = CompoundButton.OnCheckedChangeListener { view, checked ->
        val request = ArmRequest(
                checked,
                Response.Listener { response -> println(response) },
                Response.ErrorListener { error -> println(error) }
        )
        queue.add(request)
    }

    private val hoverListener = CompoundButton.OnCheckedChangeListener { view, checked ->
        val request = HoverRequest(
                checked,
                Response.Listener { response -> println(response) },
                Response.ErrorListener { error -> println(error) }
        )
        queue.add(request)
    }

    private val dropListener = CompoundButton.OnCheckedChangeListener { view, checked ->
        val request = DropRequest(
                checked,
                Response.Listener { response -> println(response) },
                Response.ErrorListener { error -> println(error) }
        )
        queue.add(request)
    }

    fun map(x: Double, inLow: Double, inHigh: Double, outLow: Double, outHigh: Double): Double {
        return (x - inLow) * (outHigh - outLow) / (inHigh - inLow) + outLow
    }

    fun updatePing(json: String) {
        val o = JSONObject(json)
        if (o.has("calledAt")) {
            pingPreview.text = "${System.currentTimeMillis() - o.getLong("calledAt")} ms"
        } else {
            pingPreview.text = "No Ping"
        }
    }

    private val ipListener = object : TextView.OnEditorActionListener {
        override fun onEditorAction(textView: TextView?, action: Int, event: KeyEvent?): Boolean {
            if (action == EditorInfo.IME_ACTION_GO && textView != null) {
                controller.setIPAddress(textView.text.toString())
                textView.hint = textView.text.toString()
                textView.text = ""
                return true
            }
            return false
        }
    }

}