package com.arws.hrcalltracker

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton

class AboutActivity : AppCompatActivity() {

    private val appsScriptCode = """
/**
 * GOOGLE APPS SCRIPT — Paste this into your Google Sheet's Apps Script editor.
 *
 * HOW TO SET UP:
 * 1. Open your Google Sheet
 * 2. Go to Extensions → Apps Script
 * 3. Delete any existing code and paste this entire script
 * 4. Click Deploy → New Deployment
 * 5. Select "Web app"
 * 6. Set "Execute as" → Me
 * 7. Set "Who has access" → Anyone
 * 8. Click Deploy
 * 9. Copy the Web App URL
 * 10. Paste the URL into the Setup inside this app.
 */

function doPost(e) {
  try {
    // Open the current active spreadsheet
    var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("Sheet1");
    if (!sheet) {
      sheet = SpreadsheetApp.getActiveSpreadsheet().getSheets()[0];
    }

    // Parse the incoming JSON data
    var data = JSON.parse(e.postData.contents);

    // Append a new row with the call data
    sheet.appendRow([
      new Date(),        // Timestamp
      data.hr,           // HR Name
      data.number,       // Phone Number
      data.type,         // Call Type (Incoming/Outgoing/Missed)
      data.duration,     // Duration in seconds
      data.date,         // Call date from device
      data.sim           // SIM card name
    ]);

    // Return success response
    return ContentService.createTextOutput(
      JSON.stringify({ "status": "success", "message": "Data recorded" })
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
        setContentView(R.layout.activity_about)

        val toolbar = findViewById<Toolbar>(R.id.toolbarAbout)
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

        // Auto-scroll logic based on intent
        val scrollTo = intent.getStringExtra("SCROLL_TO")
        val scrollView = findViewById<androidx.core.widget.NestedScrollView>(R.id.aboutScrollView)
        
        scrollView.post {
            when (scrollTo) {
                "ABOUT" -> scrollView.smoothScrollTo(0, findViewById<View>(R.id.cardAbout).top)
                "TUTORIAL" -> scrollView.smoothScrollTo(0, findViewById<View>(R.id.cardTutorial).top)
                "SETUP" -> scrollView.smoothScrollTo(0, findViewById<View>(R.id.cardSetup).top)
            }
        }
    }
}
