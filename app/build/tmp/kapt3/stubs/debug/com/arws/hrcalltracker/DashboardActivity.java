package com.arws.hrcalltracker;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u0014J\b\u0010\u0011\u001a\u00020\u000eH\u0014J\b\u0010\u0012\u001a\u00020\u000eH\u0002J\b\u0010\u0013\u001a\u00020\u000eH\u0002J\b\u0010\u0014\u001a\u00020\u000eH\u0002J\b\u0010\u0015\u001a\u00020\u000eH\u0002J\b\u0010\u0016\u001a\u00020\u000eH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/arws/hrcalltracker/DashboardActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "btnEditConfig", "Lcom/google/android/material/button/MaterialButton;", "btnSyncNow", "prefs", "Lcom/arws/hrcalltracker/PrefsManager;", "tvLastScanTime", "Landroid/widget/TextView;", "tvPendingCount", "tvScriptUrlText", "tvSimNameValue", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "onResume", "runManualSync", "setupObservers", "setupUI", "startPeriodicWork", "updateLastScanTimeUI", "app_debug"})
public final class DashboardActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.arws.hrcalltracker.PrefsManager prefs;
    private android.widget.TextView tvSimNameValue;
    private android.widget.TextView tvScriptUrlText;
    private android.widget.TextView tvLastScanTime;
    private android.widget.TextView tvPendingCount;
    private com.google.android.material.button.MaterialButton btnSyncNow;
    private com.google.android.material.button.MaterialButton btnEditConfig;
    
    public DashboardActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void setupUI() {
    }
    
    private final void setupObservers() {
    }
    
    private final void updateLastScanTimeUI() {
    }
    
    private final void startPeriodicWork() {
    }
    
    private final void runManualSync() {
    }
    
    @java.lang.Override()
    protected void onResume() {
    }
}