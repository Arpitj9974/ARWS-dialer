package com.arws.hrcalltracker;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\u0015\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0019\u001a\u00020\u001aH\u0002J\u0012\u0010\u001b\u001a\u00020\u001a2\b\u0010\u001c\u001a\u0004\u0018\u00010\u001dH\u0014J-\u0010\u001e\u001a\u00020\u001a2\u0006\u0010\u001f\u001a\u00020\u00042\u000e\u0010 \u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00150!2\u0006\u0010\"\u001a\u00020#H\u0016\u00a2\u0006\u0002\u0010$J\b\u0010%\u001a\u00020\u001aH\u0002J\b\u0010&\u001a\u00020\u001aH\u0002J\b\u0010\'\u001a\u00020\u001aH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\rX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0017X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006("}, d2 = {"Lcom/arws/hrcalltracker/SetupActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "PERMISSION_REQUEST_CODE", "", "availableSims", "", "Landroid/telephony/SubscriptionInfo;", "btnDetectSims", "Lcom/google/android/material/button/MaterialButton;", "btnRequestPerms", "btnSave", "etHrName", "Lcom/google/android/material/textfield/TextInputEditText;", "etScriptUrl", "prefs", "Lcom/arws/hrcalltracker/PrefsManager;", "rgSimCards", "Landroid/widget/RadioGroup;", "selectedSimId", "selectedSimName", "", "tvPermCallLogStatus", "Landroid/widget/TextView;", "tvPermPhoneStateStatus", "loadSimCards", "", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onRequestPermissionsResult", "requestCode", "permissions", "", "grantResults", "", "(I[Ljava/lang/String;[I)V", "requestRequiredPermissions", "saveConfiguration", "updatePermissionStatus", "app_debug"})
public final class SetupActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.arws.hrcalltracker.PrefsManager prefs;
    private com.google.android.material.textfield.TextInputEditText etHrName;
    private com.google.android.material.textfield.TextInputEditText etScriptUrl;
    private android.widget.RadioGroup rgSimCards;
    private android.widget.TextView tvPermCallLogStatus;
    private android.widget.TextView tvPermPhoneStateStatus;
    private com.google.android.material.button.MaterialButton btnRequestPerms;
    private com.google.android.material.button.MaterialButton btnDetectSims;
    private com.google.android.material.button.MaterialButton btnSave;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<android.telephony.SubscriptionInfo> availableSims;
    private int selectedSimId = -1;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String selectedSimName = "";
    private final int PERMISSION_REQUEST_CODE = 100;
    
    public SetupActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void updatePermissionStatus() {
    }
    
    private final void requestRequiredPermissions() {
    }
    
    @java.lang.Override()
    public void onRequestPermissionsResult(int requestCode, @org.jetbrains.annotations.NotNull()
    java.lang.String[] permissions, @org.jetbrains.annotations.NotNull()
    int[] grantResults) {
    }
    
    private final void loadSimCards() {
    }
    
    private final void saveConfiguration() {
    }
}