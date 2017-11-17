package com.benny.openlauncher.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.benny.openlauncher.R
import com.benny.openlauncher.util.AppSettings
import kotlinx.android.synthetic.main.toolbar.*

abstract class ThemeActivity : AppCompatActivity() {

    private var appSettings: AppSettings? = null
    private var currentTheme: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        appSettings = AppSettings.get()
        currentTheme = appSettings!!.theme
        if (appSettings!!.theme == "0") {
            setTheme(R.style.NormalActivity_Light)
        } else {
            setTheme(R.style.NormalActivity_Dark)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = dark(AppSettings.get().primaryColor, 0.8)
            //getWindow().setNavigationBarColor(AppSettings.get().getPrimaryColor());
        }
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (appSettings!!.theme != currentTheme) {
            restart()
        }
    }

    private fun restart() {
        val intent = Intent(this, javaClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        overridePendingTransition(0, 0)
        startActivity(intent)
    }

    fun dark(color: Int, factor: Double): Int {
        val a = Color.alpha(color)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(a, Math.max((r * factor).toInt(), 0), Math.max((g * factor).toInt(), 0), Math.max((b * factor).toInt(), 0))
    }
}
