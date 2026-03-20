package com.arws.hrcalltracker;

/**
 * CallLogHelper — Reads the latest call log entry from Android's CallLog.Calls.
 *
 * After a call ends (IDLE detected by CallTrackingService),
 * this helper queries the call log database, sorts by date descending,
 * and extracts the most recent call's details.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\b\u0004\u0018\u0000 \u00122\u00020\u0001:\u0002\u0011\u0012B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0002J\b\u0010\t\u001a\u0004\u0018\u00010\nJ\u0010\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\rH\u0002J\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\n0\u000f2\u0006\u0010\u0010\u001a\u00020\bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/arws/hrcalltracker/CallLogHelper;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "formatDate", "", "millis", "", "getLatestCallLog", "Lcom/arws/hrcalltracker/CallLogHelper$CallInfo;", "mapCallType", "type", "", "scanCallsSince", "", "timestamp", "CallInfo", "Companion", "app_debug"})
public final class CallLogHelper {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "CallLogHelper";
    @org.jetbrains.annotations.NotNull()
    public static final com.arws.hrcalltracker.CallLogHelper.Companion Companion = null;
    
    public CallLogHelper(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    /**
     * Read the latest call log entry.
     *
     * @return CallInfo with the latest call details, or null if no entries found.
     */
    @org.jetbrains.annotations.Nullable()
    public final com.arws.hrcalltracker.CallLogHelper.CallInfo getLatestCallLog() {
        return null;
    }
    
    /**
     * Scan call log for entries newer than a given timestamp.
     * Used by PeriodicSyncWorker for batch uploads.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.arws.hrcalltracker.CallLogHelper.CallInfo> scanCallsSince(long timestamp) {
        return null;
    }
    
    /**
     * Map Android call type integer to human-readable string.
     */
    private final java.lang.String mapCallType(int type) {
        return null;
    }
    
    /**
     * Format a timestamp in milliseconds to a readable date string.
     * Example: "2026-03-19 10:40"
     */
    private final java.lang.String formatDate(long millis) {
        return null;
    }
    
    /**
     * Data class to hold extracted call details.
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0012\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B5\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\u0003\u0012\u0006\u0010\t\u001a\u00020\n\u00a2\u0006\u0002\u0010\u000bJ\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\nH\u00c6\u0003JE\u0010\u001b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\nH\u00c6\u0001J\u0013\u0010\u001c\u001a\u00020\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001f\u001a\u00020\nH\u00d6\u0001J\t\u0010 \u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\rR\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\rR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\rR\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014\u00a8\u0006!"}, d2 = {"Lcom/arws/hrcalltracker/CallLogHelper$CallInfo;", "", "phoneNumber", "", "callType", "duration", "date", "", "formattedDate", "subscriptionId", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JLjava/lang/String;I)V", "getCallType", "()Ljava/lang/String;", "getDate", "()J", "getDuration", "getFormattedDate", "getPhoneNumber", "getSubscriptionId", "()I", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "", "other", "hashCode", "toString", "app_debug"})
    public static final class CallInfo {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String phoneNumber = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String callType = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String duration = null;
        private final long date = 0L;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String formattedDate = null;
        private final int subscriptionId = 0;
        
        public CallInfo(@org.jetbrains.annotations.NotNull()
        java.lang.String phoneNumber, @org.jetbrains.annotations.NotNull()
        java.lang.String callType, @org.jetbrains.annotations.NotNull()
        java.lang.String duration, long date, @org.jetbrains.annotations.NotNull()
        java.lang.String formattedDate, int subscriptionId) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getPhoneNumber() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getCallType() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getDuration() {
            return null;
        }
        
        public final long getDate() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getFormattedDate() {
            return null;
        }
        
        public final int getSubscriptionId() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component3() {
            return null;
        }
        
        public final long component4() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component5() {
            return null;
        }
        
        public final int component6() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.arws.hrcalltracker.CallLogHelper.CallInfo copy(@org.jetbrains.annotations.NotNull()
        java.lang.String phoneNumber, @org.jetbrains.annotations.NotNull()
        java.lang.String callType, @org.jetbrains.annotations.NotNull()
        java.lang.String duration, long date, @org.jetbrains.annotations.NotNull()
        java.lang.String formattedDate, int subscriptionId) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/arws/hrcalltracker/CallLogHelper$Companion;", "", "()V", "TAG", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}