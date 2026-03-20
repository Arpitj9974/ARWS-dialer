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
 * 10. Paste the URL into ApiService.kt → API_URL constant
 *
 * SHEET COLUMNS (Row 1 headers):
 * A: Timestamp | B: HR Name | C: Phone Number | D: Call Type | E: Duration | F: SIM
 */

function doPost(e) {
  try {
    // Open the spreadsheet — replace YOUR_SHEET_ID with your actual Sheet ID
    var sheet = SpreadsheetApp.openById("YOUR_SHEET_ID").getSheetByName("Sheet1");

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

/**
 * Test function — run this manually in Apps Script to verify the sheet is accessible.
 */
function testSetup() {
  var sheet = SpreadsheetApp.openById("YOUR_SHEET_ID").getSheetByName("Sheet1");
  sheet.appendRow([new Date(), "TEST", "0000000000", "Test", "0", "N/A", "Test SIM"]);
  Logger.log("Test row added successfully!");
}
