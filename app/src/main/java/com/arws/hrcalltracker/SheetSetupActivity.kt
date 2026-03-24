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
 * ANTI-DUPLICATE DESIGN (Server-Side):
 * - Checks if uniqueKey already exists before appending
 * - Dedicated uniqueKey column (Column H) in the sheet
 * - Returns structured response: inserted / skipped_duplicate / invalid_data
 *
 * SHEET COLUMNS (Row 1 headers):
 * A: Phone Number | B: Contact Name | C: HR Name | D: Duration | E: Date | F: Time | G: Call Type | H: SIM | I: UniqueKey
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

// Column index (1-based) where uniqueKey is stored
const R_A_UNIQUE_KEY_COLUMN = 9; // Column I

/**
 * MANDATORY WEB APP ENTRY POINT
 */
function doPost(e) {
  return R_A_RecordCallData(e);
}

/**
 * UNIQUE R&A PROCESSING FUNCTION
 */
function R_A_RecordCallData(e) {
  try {
    var R_A_sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(R_A_TARGET_SHEET_NAME);
    if (!R_A_sheet) {
      R_A_sheet = SpreadsheetApp.getActiveSpreadsheet().getSheets()[0];
    }

    // Set Header if empty, and hide the uniqueKey column (Rule 4 / UI polish)
    if (R_A_sheet.getRange(1, R_A_UNIQUE_KEY_COLUMN).getValue() === "") {
      R_A_sheet.getRange(1, R_A_UNIQUE_KEY_COLUMN).setValue("UniqueKey (Do Not Edit)");
      R_A_sheet.hideColumns(R_A_UNIQUE_KEY_COLUMN);
    }

    var R_A_data = JSON.parse(e.postData.contents);

    // Validate incoming data
    if (!R_A_data.phone_number || !R_A_data.date || !R_A_data.call_type) {
      return ContentService.createTextOutput(
        JSON.stringify({ "status": "error", "action": "invalid_data", "message": "Missing required fields" })
      ).setMimeType(ContentService.MimeType.JSON);
    }

    var R_A_uniqueKey = R_A_data.unique_key || "";

    // Check if uniqueKey already exists in the sheet before appending
    if (R_A_uniqueKey !== "") {
      var R_A_isDuplicate = R_A_CheckDuplicate(R_A_sheet, R_A_uniqueKey);
      if (R_A_isDuplicate) {
        return ContentService.createTextOutput(
          JSON.stringify({ "status": "success", "action": "skipped_duplicate", "message": "UniqueKey already exists in sheet" })
        ).setMimeType(ContentService.MimeType.JSON);
      }
    }

    // Append row with uniqueKey in Column I
    R_A_sheet.appendRow([
      R_A_data.phone_number,
      R_A_data.contact_name || "",
      R_A_data.hr_name,
      R_A_data.duration,
      R_A_data.date,
      R_A_data.time,
      R_A_data.call_type,
      R_A_data.sim,
      R_A_uniqueKey
    ]);

    return ContentService.createTextOutput(
      JSON.stringify({ "status": "success", "action": "inserted", "message": "Data recorded by R&A" })
    ).setMimeType(ContentService.MimeType.JSON);

  } catch (error) {
    return ContentService.createTextOutput(
      JSON.stringify({ "status": "error", "action": "error", "message": error.toString() })
    ).setMimeType(ContentService.MimeType.JSON);
  }
}

/**
 * Check if uniqueKey already exists in Column I.
 */
function R_A_CheckDuplicate(sheet, uniqueKey) {
  var lastRow = sheet.getLastRow();
  if (lastRow < 2) return false;

  var range = sheet.getRange(2, R_A_UNIQUE_KEY_COLUMN, lastRow - 1, 1);
  var values = range.getValues();

  for (var i = 0; i < values.length; i++) {
    if (values[i][0] === uniqueKey) {
      return true;
    }
  }
  return false;
}

/**
 * Test function — run manually to verify setup.
 */
function testSetup() {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(R_A_TARGET_SHEET_NAME);
  if (!sheet) sheet = SpreadsheetApp.getActiveSpreadsheet().getSheets()[0];
  sheet.appendRow(["0000000000", "Test Name", "TEST", "00:00", "01/01/2026", "00:00:00", "Test", "Test SIM", "TEST_KEY_DELETE_ME"]);
  Logger.log("Test row added successfully!");
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
