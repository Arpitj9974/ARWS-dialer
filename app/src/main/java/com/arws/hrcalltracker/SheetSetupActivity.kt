package com.arws.hrcalltracker

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton

class SheetSetupActivity : AppCompatActivity() {

    private val appsScriptCode = """
/**
 * GOOGLE APPS SCRIPT — Paste this into your Google Sheet's Apps Script editor.
 *
 * HOW TO SET UP:
 * 1. Open your Google Sheet
 * 2. Go to Extensions → Apps Script
 * 3. Delete any existing code and paste this entire script
 * 4. IMPORTANT: Change the target sheet name below if yours is not "Sheet1"
 * 5. Click Deploy → New Deployment
 * 6. Select "Web app"
 * 7. Set "Execute as" → Me
 * 8. Set "Who has access" → Anyone
 * 9. Click Deploy
 * 10. Copy the Web App URL and paste it into the Setup inside this app.
 */

// ⬇️ CHANGE THIS IF YOUR SHEET HAS A DIFFERENT NAME
const R_A_TARGET_SHEET_NAME = "Sheet1";

/**
 * MANDATORY WEB APP ENTRY POINT
 * Google Apps Script strictly requires this function to be named exactly doPost(e)
 * to intercept HTTP POST requests. 
 */
function doPost(e) {
  return R_A_RecordCallData(e);
}

/**
 * UNIQUE R&A PROCESSING FUNCTION
 * Prefixed with R_A_ to ensure no naming collisions if you have other scripts.
 */
function R_A_RecordCallData(e) {
  try {
    // Open the spreadsheet and target the customizable sheet name
    var R_A_sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(R_A_TARGET_SHEET_NAME);
    if (!R_A_sheet) {
      // Fallback to the first sheet if the specified name doesn't exist
      R_A_sheet = SpreadsheetApp.getActiveSpreadsheet().getSheets()[0];
    }

    // Parse the incoming JSON data securely
    var R_A_data = JSON.parse(e.postData.contents);

    // Append a new row with the call data
    R_A_sheet.appendRow([
      new Date(),                // Timestamp
      R_A_data.hr_name,          // HR Name
      R_A_data.phone_number,     // Phone Number
      R_A_data.call_type,        // Call Type (Incoming/Outgoing/Missed)
      R_A_data.duration,         // Duration in seconds
      R_A_data.date,             // Call date from device
      R_A_data.sim               // SIM card name
    ]);

    // Return success response formatted for Android App
    return ContentService.createTextOutput(
      JSON.stringify({ "status": "success", "message": "Data recorded securely by R&A" })
    ).setMimeType(ContentService.MimeType.JSON);

  } catch (error) {
    // Return error response
    return ContentService.createTextOutput(
      JSON.stringify({ "status": "error", "message": error.toString() })
    ).setMimeType(ContentService.MimeType.JSON);
  }
}
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sheet_setup)

        val toolbar = findViewById<Toolbar>(R.id.toolbarLayout)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val tvCodeBlock = findViewById<TextView>(R.id.tvCodeBlock)
        val btnCopyCode = findViewById<MaterialButton>(R.id.btnCopyCode)

        tvCodeBlock.text = appsScriptCode

        btnCopyCode.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = android.content.ClipData.newPlainText("Apps Script Code", appsScriptCode)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
        }
    }
}
