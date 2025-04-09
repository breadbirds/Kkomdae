package com.pizza.kkomdae.base

import android.content.Context
import android.content.res.Configuration
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context?) {
        val newOverride = Configuration(newBase?.resources?.configuration)
        val metrics = newBase?.resources?.displayMetrics

        if(metrics?.densityDpi != DisplayMetrics.DENSITY_DEVICE_STABLE){
            newOverride.densityDpi = DisplayMetrics.DENSITY_DEVICE_STABLE
        }

        if(newOverride.fontScale >= 1.0f)
            newOverride.fontScale = 1.0f

        applyOverrideConfiguration(newOverride)
        super.attachBaseContext(newBase)
    }
}