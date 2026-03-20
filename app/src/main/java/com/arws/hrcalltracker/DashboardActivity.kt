package com.arws.hrcalltracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.work.*
import com.arws.hrcalltracker.db.AppDatabase
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DashboardActivity : AppCompatActivity() {

    private lateinit var prefs: PrefsManager
    private lateinit var tvHrName: TextView
    private lateinit var tvSimInfo: TextView
    private lateinit var tvUrlStatus: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvPendingCount: TextView
    private lateinit var tvLastScanTime: TextView
    private lateinit var btnSyncNow: Button
    private lateinit var btnEditSetup: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        prefs = PrefsManager(this)

        // Initialize Views
        tvHrName = findViewById(R.id.tvHrNameValue)
        tvSimInfo = findViewById(R.id.tvSimNameValue)
        tvUrlStatus = findViewById(R.id.tvStatusValue) // Just using existing status field for now
        tvStatus = findViewById(R.id.tvStatusValue)
        tvPendingCount = findViewById(R.id.tvPendingCount)
        tvLastScanTime = findViewById(R.id.tvLastScanTime)
        btnSyncNow = findViewById(R.id.btnSyncNow)
        btnEditSetup = findViewById(R.id.btnEditConfig)

        setupUI()
        setupObservers()
        startPeriodicWork()
    }

    private fun setupUI() {
        val hrName = prefs.getHrName()
        tvHrName.text = if (hrName.isNotEmpty()) hrName else "HR Tracker"
        
        val simName = prefs.getCompanySimName()
        tvSimInfo.text = "Tracking: $simName"

        tvStatus.text = "PERIODIC SCAN ACTIVE"
        tvStatus.setTextColor(ContextCompat.getColor(this, R.color.emerald_500))
        val pulse = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.pulse)
        tvStatus.startAnimation(pulse)

        btnSyncNow.setOnClickListener {
            runManualSync()
        }

        btnEditSetup.setOnClickListener {
            val intent = Intent(this, SetupActivity::class.java)
            intent.putExtra("EDIT_MODE", true)
            startActivity(intent)
        }

        updateLastScanTimeUI()
    }

    private fun setupObservers() {
        AppDatabase.getDatabase(this).callDao().getPendingCallsLiveData().observe(this, Observer { calls ->
            tvPendingCount.text = "${calls.size}"
        })
    }

    private fun updateLastScanTimeUI() {
        val lastScan = prefs.getLastUploadedTimestamp()
        if (lastScan == 0L) {
            tvLastScanTime.text = "Never"
        } else {
            val sdf = SimpleDateFormat("HH:mm, MMM dd", Locale.getDefault())
            tvLastScanTime.text = sdf.format(Date(lastScan))
        }
    }

    private fun startPeriodicWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<PeriodicSyncWorker>(30, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            PeriodicSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun runManualSync() {
        Toast.makeText(this, "Syncing Now...", Toast.LENGTH_SHORT).show()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<PeriodicSyncWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueue(request)
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(request.id)
            .observe(this, Observer { info ->
                if (info != null && info.state.isFinished) {
                    updateLastScanTimeUI()
                    Toast.makeText(this, "Sync Completed", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onResume() {
        super.onResume()
        setupUI()
    }
}
