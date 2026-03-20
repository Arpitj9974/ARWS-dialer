package com.arws.hrcalltracker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SetupActivity : AppCompatActivity() {

    private lateinit var prefs: PrefsManager
    private lateinit var etHrName: TextInputEditText
    private lateinit var etScriptUrl: TextInputEditText
    private lateinit var rgSimCards: RadioGroup
    
    private lateinit var tvPermCallLogStatus: TextView
    private lateinit var tvPermPhoneStateStatus: TextView
    private lateinit var btnRequestPerms: MaterialButton
    private lateinit var btnDetectSims: MaterialButton
    private lateinit var btnSave: MaterialButton

    private var availableSims = mutableListOf<SubscriptionInfo>()
    private var selectedSimId: Int = -1
    private var selectedSimName: String = ""

    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PrefsManager(this)

        if (prefs.isSetupComplete() && !intent.getBooleanExtra("EDIT_MODE", false)) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_setup)

        // Bind Views
        etHrName = findViewById(R.id.etHrName)
        etScriptUrl = findViewById(R.id.etScriptUrl)
        rgSimCards = findViewById(R.id.rgSimCards)
        tvPermCallLogStatus = findViewById(R.id.tvPermCallLogStatus)
        tvPermPhoneStateStatus = findViewById(R.id.tvPermPhoneStateStatus)
        btnRequestPerms = findViewById(R.id.btnRequestPerms)
        btnDetectSims = findViewById(R.id.btnDetectSims)
        btnSave = findViewById(R.id.btnSaveSetup)

        // Pre-fill
        etHrName.setText(prefs.getHrName())
        etScriptUrl.setText(prefs.getScriptUrl())

        btnRequestPerms.setOnClickListener { requestRequiredPermissions() }
        btnDetectSims.setOnClickListener { loadSimCards() }
        btnSave.setOnClickListener { saveConfiguration() }

        updatePermissionStatus()
        loadSimCards()
    }

    private fun updatePermissionStatus() {
        val hasCallLog = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
        val hasPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

        tvPermCallLogStatus.text = if (hasCallLog) "GRANTED" else "PENDING"
        tvPermCallLogStatus.setTextColor(ContextCompat.getColor(this, if (hasCallLog) R.color.emerald_500 else R.color.rose_500))

        tvPermPhoneStateStatus.text = if (hasPhoneState) "GRANTED" else "PENDING"
        tvPermPhoneStateStatus.setTextColor(ContextCompat.getColor(this, if (hasPhoneState) R.color.emerald_500 else R.color.rose_500))

        if (hasCallLog && hasPhoneState) {
            btnRequestPerms.visibility = View.GONE
        } else {
            btnRequestPerms.visibility = View.VISIBLE
        }
    }

    private fun requestRequiredPermissions() {
        val perms = mutableListOf(Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_PHONE_STATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        ActivityCompat.requestPermissions(this, perms.toTypedArray(), PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        updatePermissionStatus()
        loadSimCards()
    }

    private fun loadSimCards() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        rgSimCards.removeAllViews()
        val subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        try {
            val list = subscriptionManager.activeSubscriptionInfoList
            availableSims.clear()
            if (!list.isNullOrEmpty()) {
                availableSims.addAll(list)
                val savedId = prefs.getCompanySimId()

                list.forEachIndexed { index, info ->
                    val rb = RadioButton(this)
                    rb.id = View.generateViewId()
                    rb.text = "${info.displayName} (SIM ${info.simSlotIndex + 1})"
                    rb.setTextColor(ContextCompat.getColor(this, R.color.text_slate_50))
                    rb.buttonTintList = android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.emerald_500))
                    
                    val params = RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, 8, 0, 8)
                    rb.layoutParams = params

                    rgSimCards.addView(rb)

                    if (info.subscriptionId == savedId || (savedId == -1 && index == 0)) {
                        rgSimCards.check(rb.id)
                        selectedSimId = info.subscriptionId
                        selectedSimName = info.displayName.toString()
                    }
                }

                rgSimCards.setOnCheckedChangeListener { group, checkedId ->
                    val checkedRb = group.findViewById<RadioButton>(checkedId)
                    val index = group.indexOfChild(checkedRb)
                    if (index != -1) {
                        val info = availableSims[index]
                        selectedSimId = info.subscriptionId
                        selectedSimName = info.displayName.toString()
                    }
                }
            } else {
                Toast.makeText(this, "No active SIM detected", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Phone state permission required to detect SIMs", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveConfiguration() {
        val url = etScriptUrl.text.toString().trim()
        val hrName = etHrName.text.toString().trim()

        if (url.isEmpty()) {
            Toast.makeText(this, "Google Script URL is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedSimId == -1) {
            Toast.makeText(this, "Please select a SIM card", Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Read Call Log permission is required", Toast.LENGTH_SHORT).show()
            return
        }

        prefs.saveHrName(hrName)
        prefs.saveScriptUrl(url)
        prefs.saveCompanySimId(selectedSimId)
        prefs.saveCompanySimName(selectedSimName)
        prefs.setSetupComplete(true)

        Toast.makeText(this, "Setup Saved Successfully", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}
