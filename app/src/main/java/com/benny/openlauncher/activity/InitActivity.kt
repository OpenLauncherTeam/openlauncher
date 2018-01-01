package com.benny.openlauncher.activity

import agency.tango.materialintroscreen.MaterialIntroActivity
import agency.tango.materialintroscreen.SlideFragment
import agency.tango.materialintroscreen.SlideFragmentBuilder
import agency.tango.materialintroscreen.widgets.OverScrollViewPager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.benny.openlauncher.R

class InitActivity : MaterialIntroActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!getSharedPreferences("quickSettings", Context.MODE_PRIVATE).getBoolean("firstStart", true)) {
            skipStart()
            return
        }

        val overScrollLayout = findViewById<View>(agency.tango.materialintroscreen.R.id.view_pager_slides) as OverScrollViewPager
        val viewPager = overScrollLayout.overScrollView
        viewPager.overScrollMode = View.OVER_SCROLL_NEVER

        addSlide(CustomSlide())

        addSlide(SlideFragmentBuilder()
                .backgroundColor(R.color.op_red)
                .buttonsColor(R.color.intro_button_color)
                .image(R.drawable.intro_2)
                .description("Just swipe from the left edge.")
                .build())

        addSlide(SlideFragmentBuilder()
                .backgroundColor(R.color.op_green)
                .buttonsColor(R.color.intro_button_color)
                .image(R.drawable.intro_3)
                .description("Classical app drawer!")
                .build())

        addSlide(SlideFragmentBuilder()
                .backgroundColor(R.color.op_blue)
                .buttonsColor(R.color.intro_button_color)
                .image(R.drawable.intro_4)
                .description("Easy Search!")
                .build())
    }

    override fun onFinish() {
        super.onFinish()

        setState()
    }

    private fun skipStart() {
        setState()
        finish()
    }

    private fun setState() {
        getSharedPreferences("quickSettings", Context.MODE_PRIVATE).edit().putBoolean("firstStart", false).apply()

        val intent = Intent(this@InitActivity, Home::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }

    class CustomSlide : SlideFragment() {
        override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.intro_csutom_layout, container, false)
            return view
        }

        override fun backgroundColor(): Int = R.color.op_blue

        override fun buttonsColor(): Int = R.color.intro_button_color
    }

}
