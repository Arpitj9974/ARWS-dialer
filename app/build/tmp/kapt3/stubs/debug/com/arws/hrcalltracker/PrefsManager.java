package com.arws.hrcalltracker;

/**
 * Centralized SharedPreferences manager for storing HR login data,
 * selected company SIM info, and setup state.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\f\u0018\u0000 \u001d2\u00020\u0001:\u0001\u001dB\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0007\u001a\u00020\bJ\u0006\u0010\t\u001a\u00020\nJ\u0006\u0010\u000b\u001a\u00020\nJ\u0006\u0010\f\u001a\u00020\rJ\u0006\u0010\u000e\u001a\u00020\nJ\u0006\u0010\u000f\u001a\u00020\u0010J\u000e\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\bJ\u000e\u0010\u0014\u001a\u00020\u00122\u0006\u0010\u0015\u001a\u00020\nJ\u000e\u0010\u0016\u001a\u00020\u00122\u0006\u0010\u0015\u001a\u00020\nJ\u000e\u0010\u0017\u001a\u00020\u00122\u0006\u0010\u0018\u001a\u00020\rJ\u000e\u0010\u0019\u001a\u00020\u00122\u0006\u0010\u001a\u001a\u00020\nJ\u000e\u0010\u001b\u001a\u00020\u00122\u0006\u0010\u001c\u001a\u00020\u0010R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001e"}, d2 = {"Lcom/arws/hrcalltracker/PrefsManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "prefs", "Landroid/content/SharedPreferences;", "getCompanySimId", "", "getCompanySimName", "", "getHrName", "getLastUploadedTimestamp", "", "getScriptUrl", "isSetupComplete", "", "saveCompanySimId", "", "simId", "saveCompanySimName", "name", "saveHrName", "saveLastUploadedTimestamp", "timestamp", "saveScriptUrl", "url", "setSetupComplete", "complete", "Companion", "app_debug"})
public final class PrefsManager {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "hr_call_tracker_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_HR_NAME = "hr_name";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_COMPANY_SIM_ID = "company_sim_id";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_COMPANY_SIM_NAME = "company_sim_name";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_SETUP_COMPLETE = "setup_complete";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_LAST_UPLOADED_TIMESTAMP = "last_uploaded_timestamp";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_SCRIPT_URL = "script_url";
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.arws.hrcalltracker.PrefsManager.Companion Companion = null;
    
    public PrefsManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    public final void saveScriptUrl(@org.jetbrains.annotations.NotNull()
    java.lang.String url) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getScriptUrl() {
        return null;
    }
    
    public final void saveHrName(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getHrName() {
        return null;
    }
    
    public final void saveCompanySimId(int simId) {
    }
    
    public final int getCompanySimId() {
        return 0;
    }
    
    public final void saveCompanySimName(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCompanySimName() {
        return null;
    }
    
    public final void setSetupComplete(boolean complete) {
    }
    
    public final boolean isSetupComplete() {
        return false;
    }
    
    public final void saveLastUploadedTimestamp(long timestamp) {
    }
    
    public final long getLastUploadedTimestamp() {
        return 0L;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/arws/hrcalltracker/PrefsManager$Companion;", "", "()V", "KEY_COMPANY_SIM_ID", "", "KEY_COMPANY_SIM_NAME", "KEY_HR_NAME", "KEY_LAST_UPLOADED_TIMESTAMP", "KEY_SCRIPT_URL", "KEY_SETUP_COMPLETE", "PREFS_NAME", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}