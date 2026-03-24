/**
 * GOOGLE APPS SCRIPT — Paste this into your Google Sheet's Apps Script editor.
 *
 * 21-RULE ANTI-DUPLICATE DESIGN (Server-Side):
 * - Rule 3:  Checks if uniqueKey already exists before appending
 * - Rule 4:  Dedicated uniqueKey column (Column H) in the sheet
 * - Rule 5:  Two-layer protection — this is the server-side layer
 * - Rule 15: Returns structured response: inserted / skipped_duplicate / invalid_data
 * - Rule 21: uniqueKey check before appendRow
 *
 * SHEET COLUMNS (Row 1 headers):
 * A: Phone Number | B: HR Name | C: Duration | D: Date | E: Time | F: Call Type | G: SIM | H: UniqueKey
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
const R_A_UNIQUE_KEY_COLUMN = 8; // Column H

/**
 * MANDATORY WEB APP ENTRY POINT
 */
function doPost(e) {
  return R_A_RecordCallData(e);
}

/**
 * UNIQUE R&A PROCESSING FUNCTION
 * Prefixed with R_A_ to ensure no naming collisions.
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

    // Rule 11/15: Validate incoming data
    if (!R_A_data.phone_number || !R_A_data.date || !R_A_data.call_type) {
      return ContentService.createTextOutput(
        JSON.stringify({ "status": "error", "action": "invalid_data", "message": "Missing required fields" })
      ).setMimeType(ContentService.MimeType.JSON);
    }

    var R_A_uniqueKey = R_A_data.unique_key || "";

    // Rule 3/21: Check if uniqueKey already exists in the sheet before appending
    if (R_A_uniqueKey !== "") {
      var R_A_isDuplicate = R_A_CheckDuplicate(R_A_sheet, R_A_uniqueKey);
      if (R_A_isDuplicate) {
        // Rule 1: Idempotent behavior — return success but indicate skipped
        return ContentService.createTextOutput(
          JSON.stringify({ "status": "success", "action": "skipped_duplicate", "message": "UniqueKey already exists in sheet" })
        ).setMimeType(ContentService.MimeType.JSON);
      }
    }

    // Rule 4: Append row with uniqueKey in dedicated Column H
    R_A_sheet.appendRow([
      R_A_data.phone_number,     // Column A: Phone Number (or contact name)
      R_A_data.hr_name,          // Column B: HR Name
      R_A_data.duration,         // Column C: Duration (mm:ss)
      R_A_data.date,             // Column D: Date
      R_A_data.time,             // Column E: Time
      R_A_data.call_type,        // Column F: Call Type
      R_A_data.sim,              // Column G: SIM
      R_A_uniqueKey              // Column H: UniqueKey (Rule 4)
    ]);

    // Rule 15: Structured success response
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
 * Rule 3/21: Check if uniqueKey already exists in Column H of the sheet.
 * Scans all existing values in the uniqueKey column.
 * Returns true if duplicate found, false otherwise.
 */
function R_A_CheckDuplicate(sheet, uniqueKey) {
  var lastRow = sheet.getLastRow();
  if (lastRow < 2) return false; // No data rows yet (row 1 = headers)

  // Read all uniqueKey values from Column H at once (efficient batch read)
  var range = sheet.getRange(2, R_A_UNIQUE_KEY_COLUMN, lastRow - 1, 1);
  var values = range.getValues();

  for (var i = 0; i < values.length; i++) {
    if (values[i][0] === uniqueKey) {
      return true; // Duplicate found
    }
  }
  return false; // No duplicate
}

/**
 * Test function — run manually in Apps Script to verify setup.
 */
function testSetup() {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(R_A_TARGET_SHEET_NAME);
  if (!sheet) sheet = SpreadsheetApp.getActiveSpreadsheet().getSheets()[0];
  sheet.appendRow(["0000000000", "TEST", "00:00", "01/01/2026", "00:00:00", "Test", "Test SIM", "TEST_KEY_DELETE_ME"]);
  Logger.log("Test row added successfully!");
}
