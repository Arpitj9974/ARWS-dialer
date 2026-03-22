package com.arws.hrcalltracker

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.work.*
import com.arws.hrcalltracker.db.AppDatabase
import com.google.android.material.button.MaterialButton
import java.util.concurrent.TimeUnit

class DashboardActivity : AppCompatActivity() {

    private lateinit var prefs: PrefsManager
    private lateinit var tvSimNameValue: TextView
    private lateinit var tvScriptUrlText: TextView
    private lateinit var tvLastScanTime: TextView
    private lateinit var tvPendingCount: TextView
    private lateinit var btnSyncNow: MaterialButton
    private lateinit var btnEditConfig: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        prefs = PrefsManager(this)

        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Bind Views
        tvSimNameValue = findViewById(R.id.tvSimNameValue)
        tvScriptUrlText = findViewById(R.id.tvScriptUrlText)
        tvLastScanTime = findViewById(R.id.tvLastScanTime)
        tvPendingCount = findViewById(R.id.tvPendingCount)
        btnSyncNow = findViewById(R.id.btnSyncNow)
        btnEditConfig = findViewById(R.id.btnEditConfig)

        setupUI()
        setupObservers()
        startPeriodicWork()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_about -> {
                startActivity(Intent(this, AboutUsActivity::class.java))
                return true
            }
            R.id.action_tutorial -> {
                startActivity(Intent(this, TutorialActivity::class.java))
                return true
            }
            R.id.action_setup_guide -> {
                startActivity(Intent(this, SheetSetupActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupUI() {
        val simName = prefs.getCompanySimName()
        tvSimNameValue.text = if (simName.isNotEmpty()) simName else "Not Selected"
        
        val url = prefs.getScriptUrl()
        tvScriptUrlText.text = if (url.isNotEmpty()) url else "Not Configured"

        btnSyncNow.setOnClickListener {
            runManualSync()
        }

        btnEditConfig.setOnClickListener {
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
            val now = System.currentTimeMillis()
            val timeDiff = now - lastScan
            
            // Format as "A few seconds ago", "5 minutes ago", etc.
            val relativeTime = when {
                timeDiff < DateUtils.MINUTE_IN_MILLIS -> "A few seconds ago"
                else -> DateUtils.getRelativeTimeSpanString(
                    lastScan,
                    now,
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
                ).toString()
            }
            tvLastScanTime.text = relativeTime
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
