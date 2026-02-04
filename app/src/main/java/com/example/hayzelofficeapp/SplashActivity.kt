package com.example.hayzelofficeapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var skipText: TextView
    private var isNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // ✅ FORCE CLEAR FIREBASE SESSION (TEMPORARY FOR TESTING)
        Firebase.auth.signOut()
        Log.d("SPLASH_DEBUG", "Firebase session cleared")

        videoView = findViewById(R.id.videoView)
        skipText = findViewById(R.id.skipText)

        val videoPath = "android.resource://$packageName/${R.raw.splash_video}"

        videoView.setVideoURI(Uri.parse(videoPath))

        videoView.setOnPreparedListener {
            it.isLooping = false
            videoView.start()
        }

        skipText.setOnClickListener {
            goToLogin()
        }

        videoView.setOnCompletionListener {
            goToLogin()
        }
    }

    private fun goToLogin() {
        if (isNavigated) return
        isNavigated = true

        // ✅ ALWAYS GO TO LOGIN PAGE (Testing mode)
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onPause() {
        super.onPause()
        if (videoView.isPlaying) {
            videoView.pause()
        }
    }
}