package com.stickman.tower.android

import android.os.Bundle

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.stickman.tower.StickmanGame

/** Launches the Android application. */
class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize(StickmanGame(), AndroidApplicationConfiguration().apply {
            useImmersiveMode = true
            useWakelock = true
            useAccelerometer = false
            useCompass = false
        })
    }
}
