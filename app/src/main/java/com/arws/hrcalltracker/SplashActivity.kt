package com.arws.hrcalltracker

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

class SplashActivity : AppCompatActivity() {

    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        prefs = PrefsManager(this)

        val lottieAnimationView = findViewById<LottieAnimationView>(R.id.lottieSplash)

        // Listen for animation finishing
        lottieAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                // When the animation finishes, transition to the next screen
                if (prefs.isSetupComplete()) {
                    startActivity(Intent(this@SplashActivity, DashboardActivity::class.java))
                } else {
                    startActivity(Intent(this@SplashActivity, SetupActivity::class.java))
                }
                finish() // Destroy splash screen so user cannot hit back button to it
                // Override default transition for a smoother premium crossfade
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }
}
