package com.arws.hrcalltracker;

/**
 * ApiService — Sends call data to Google Sheets via Google Apps Script.
 *
 * Makes an asynchronous HTTP POST request with JSON payload.
 * The Google Apps Script endpoint appends the data as a new row in the sheet.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\u0018\u0000 \u00102\u00020\u0001:\u0001\u0010B\u0005\u00a2\u0006\u0002\u0010\u0002JF\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\b2\u0006\u0010\u000b\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\b2\u0006\u0010\u000e\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/arws/hrcalltracker/ApiService;", "", "()V", "client", "Lokhttp3/OkHttpClient;", "sendCallDataSync", "", "scriptUrl", "", "hrName", "employeeId", "phoneNumber", "callType", "duration", "date", "simName", "Companion", "app_debug"})
public final class ApiService {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "ApiService";
    @org.jetbrains.annotations.NotNull()
    private static final okhttp3.MediaType JSON_MEDIA_TYPE = null;
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient client = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.arws.hrcalltracker.ApiService.Companion Companion = null;
    
    public ApiService() {
        super();
    }
    
    /**
     * Send call data to a specific HR's Google Sheet synchronously.
     *
     * @param scriptUrl   The unique Google Apps Script URL for this HR
     * @param hrName      The HR employee's name
     * @param employeeId  The HR employee's unique ID
     * @param phoneNumber The phone number from the call
     * @param callType    "Incoming", "Outgoing", or "Missed"
     * @param duration    Call duration in seconds
     * @param date        Formatted date string
     * @param simName     The SIM card used
     */
    public final boolean sendCallDataSync(@org.jetbrains.annotations.NotNull()
    java.lang.String scriptUrl, @org.jetbrains.annotations.NotNull()
    java.lang.String hrName, @org.jetbrains.annotations.NotNull()
    java.lang.String employeeId, @org.jetbrains.annotations.NotNull()
    java.lang.String phoneNumber, @org.jetbrains.annotations.NotNull()
    java.lang.String callType, @org.jetbrains.annotations.NotNull()
    java.lang.String duration, @org.jetbrains.annotations.NotNull()
    java.lang.String date, @org.jetbrains.annotations.NotNull()
    java.lang.String simName) {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/arws/hrcalltracker/ApiService$Companion;", "", "()V", "JSON_MEDIA_TYPE", "Lokhttp3/MediaType;", "TAG", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}