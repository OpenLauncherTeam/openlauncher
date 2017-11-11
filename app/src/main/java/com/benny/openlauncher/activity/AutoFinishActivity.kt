package com.benny.openlauncher.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

//For kustom support
class AutoFinishActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        finish()
        super.onCreate(savedInstanceState)
    }

    companion object {

        fun start(c: Context) {
            c.startActivity(Intent(c, AutoFinishActivity::class.java))
        }
    }
}
