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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SetupActivity : AppCompatActivity() {

    private lateinit var prefs: PrefsManager
    private lateinit var etHrName: TextInputEditText
    private lateinit var etScriptUrl: TextInputEditText
    private lateinit var spinnerSimCards: Spinner
    
    private lateinit var llPermissionStatus: LinearLayout
    private lateinit var ivPermissionIcon: ImageView
    private lateinit var tvPermissionStatus: TextView
    private lateinit var tvPermissionHelper: TextView
    private lateinit var btnRequestPerms: MaterialButton
    private lateinit var btnTestConnection: MaterialButton
    private lateinit var btnSave: MaterialButton

    private var availableSims = mutableListOf<SubscriptionInfo>()
    private var selectedSimId: Int = -1
    private var selectedSimName: String = ""

    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PrefsManager(this)

        val isEditMode = intent.getBooleanExtra("EDIT_MODE", false)
        if (prefs.isSetupComplete() && !isEditMode) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_setup)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener {
            if (isEditMode) finish() else Toast.makeText(this, "Please complete setup.", Toast.LENGTH_SHORT).show()
        }

        etHrName = findViewById(R.id.etHrName)
        etScriptUrl = findViewById(R.id.etScriptUrl)
        spinnerSimCards = findViewById(R.id.spinnerSimCards)
        
        llPermissionStatus = findViewById(R.id.llPermissionStatus)
        ivPermissionIcon = findViewById(R.id.ivPermissionIcon)
        tvPermissionStatus = findViewById(R.id.tvPermissionStatus)
        tvPermissionHelper = findViewById(R.id.tvPermissionHelper)
        
        btnRequestPerms = findViewById(R.id.btnRequestPerms)
        btnTestConnection = findViewById(R.id.btnTestConnection)
        btnSave = findViewById(R.id.btnSaveSetup)

        etHrName.setText(prefs.getHrName())
        etScriptUrl.setText(prefs.getScriptUrl())

        btnRequestPerms.setOnClickListener { requestRequiredPermissions() }
        btnTestConnection.setOnClickListener { testConnection() }
        btnSave.setOnClickListener { saveConfiguration() }

        updatePermissionStatus()
        loadSimCards()
    }

    private fun testConnection() {
        val url = etScriptUrl.text.toString().trim()
        val hrName = etHrName.text.toString().trim()
        if (url.isEmpty()) {
            Toast.makeText(this, "Please enter a URL first", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Testing connection...", Toast.LENGTH_SHORT).show()
        
        Thread {
            val api = ApiService()
            val testUniqueKey = "TEST_${System.currentTimeMillis()}"
            val result = api.sendCallDataSync(
                scriptUrl = url,
                hrName = if(hrName.isEmpty()) "Test HR" else hrName,
                phoneNumber = "1234567890", 
                callType = "Incoming",
                duration = "45",
                date = "21/03/2026", 
                time = "18:00:00",
                simName = "SIM1",
                uniqueKey = testUniqueKey
            )
            
            runOnUiThread {
                when (result) {
                    ApiService.UploadResult.INSERTED,
                    ApiService.UploadResult.SKIPPED_DUPLICATE -> {
                        Toast.makeText(this@SetupActivity, "✅ Connection Successful! Check your sheet.", Toast.LENGTH_LONG).show()
                    }
                    ApiService.UploadResult.ERROR -> {
                        Toast.makeText(this@SetupActivity, "❌ Connection Failed. Check URL and Deployment.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }.start()
    }

    private fun updatePermissionStatus() {
        val hasCallLog = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
        val hasPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        val hasContacts = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

        if (hasCallLog && hasPhoneState && hasContacts) {
            llPermissionStatus.setBackgroundResource(R.drawable.bg_success_box)
            ivPermissionIcon.setImageResource(android.R.drawable.checkbox_on_background)
            ivPermissionIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_green))
            tvPermissionStatus.text = "All permissions granted!"
            tvPermissionStatus.setTextColor(ContextCompat.getColor(this, R.color.success_green_text))
            tvPermissionHelper.text = "Call Log & Phone State permissions have been granted."
            btnRequestPerms.visibility = View.GONE
        } else {
            llPermissionStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_light_gray))
            ivPermissionIcon.setImageResource(android.R.drawable.ic_dialog_info)
            ivPermissionIcon.setColorFilter(ContextCompat.getColor(this, R.color.amber_warning))
            tvPermissionStatus.text = "Permissions Required"
            tvPermissionStatus.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            tvPermissionHelper.text = "Please grant the necessary permissions to continue."
            btnRequestPerms.visibility = View.VISIBLE
        }
    }

    private fun requestRequiredPermissions() {
        val perms = mutableListOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) perms.add(Manifest.permission.POST_NOTIFICATIONS)
        ActivityCompat.requestPermissions(this, perms.toTypedArray(), PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        updatePermissionStatus()
        loadSimCards()
    }

    private fun loadSimCards() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) return
        val subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        try {
            val list = subscriptionManager.activeSubscriptionInfoList
            availableSims.clear()
            val simNames = mutableListOf<String>()
            if (!list.isNullOrEmpty()) {
                availableSims.addAll(list)
                val savedId = prefs.getCompanySimId()
                var selectedIndex = 0
                list.forEachIndexed { index, info ->
                    simNames.add("SIM ${info.simSlotIndex + 1} - ${info.displayName}")
                    if (info.subscriptionId == savedId) selectedIndex = index
                }
                spinnerSimCards.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, simNames).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinnerSimCards.setSelection(selectedIndex)
                spinnerSimCards.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val info = availableSims[position]
                        selectedSimId = info.subscriptionId
                        selectedSimName = info.displayName.toString()
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
                if(availableSims.isNotEmpty()) {
                    selectedSimId = availableSims[selectedIndex].subscriptionId
                    selectedSimName = availableSims[selectedIndex].displayName.toString()
                }
            } else {
                spinnerSimCards.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("No active SIM detected"))
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Phone state permission required to detect SIMs", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveConfiguration() {
        val url = etScriptUrl.text.toString().trim()
        val hrName = etHrName.text.toString().trim()

        if (url.isEmpty()) { Toast.makeText(this, "Google Script URL is required", Toast.LENGTH_SHORT).show(); return }
        if (selectedSimId == -1) { Toast.makeText(this, "Please select a SIM card", Toast.LENGTH_SHORT).show(); return }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Read Call Log permission is required", Toast.LENGTH_SHORT).show(); return
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
}
