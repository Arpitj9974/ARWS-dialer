package com.arws.hrcalltracker.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@SuppressWarnings({"unchecked", "deprecation"})
public final class CallDao_Impl implements CallDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CallEntity> __insertionAdapterOfCallEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCall;

  public CallDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCallEntity = new EntityInsertionAdapter<CallEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `pending_calls` (`id`,`hrName`,`phoneNumber`,`callType`,`duration`,`date`,`simName`,`uniqueCallId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CallEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getHrName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getHrName());
        }
        if (entity.getPhoneNumber() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getPhoneNumber());
        }
        if (entity.getCallType() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCallType());
        }
        if (entity.getDuration() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getDuration());
        }
        if (entity.getDate() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getDate());
        }
        if (entity.getSimName() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getSimName());
        }
        if (entity.getUniqueCallId() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getUniqueCallId());
        }
      }
    };
    this.__preparedStmtOfDeleteCall = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM pending_calls WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertCall(final CallEntity call, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfCallEntity.insertAndReturnId(call);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCall(final long callId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCall.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, callId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteCall.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getPendingCalls(final Continuation<? super List<CallEntity>> $completion) {
    final String _sql = "SELECT * FROM pending_calls ORDER BY date ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CallEntity>>() {
      @Override
      @NonNull
      public List<CallEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHrName = CursorUtil.getColumnIndexOrThrow(_cursor, "hrName");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfCallType = CursorUtil.getColumnIndexOrThrow(_cursor, "callType");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfSimName = CursorUtil.getColumnIndexOrThrow(_cursor, "simName");
          final int _cursorIndexOfUniqueCallId = CursorUtil.getColumnIndexOrThrow(_cursor, "uniqueCallId");
          final List<CallEntity> _result = new ArrayList<CallEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CallEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpHrName;
            if (_cursor.isNull(_cursorIndexOfHrName)) {
              _tmpHrName = null;
            } else {
              _tmpHrName = _cursor.getString(_cursorIndexOfHrName);
            }
            final String _tmpPhoneNumber;
            if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
              _tmpPhoneNumber = null;
            } else {
              _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            }
            final String _tmpCallType;
            if (_cursor.isNull(_cursorIndexOfCallType)) {
              _tmpCallType = null;
            } else {
              _tmpCallType = _cursor.getString(_cursorIndexOfCallType);
            }
            final String _tmpDuration;
            if (_cursor.isNull(_cursorIndexOfDuration)) {
              _tmpDuration = null;
            } else {
              _tmpDuration = _cursor.getString(_cursorIndexOfDuration);
            }
            final String _tmpDate;
            if (_cursor.isNull(_cursorIndexOfDate)) {
              _tmpDate = null;
            } else {
              _tmpDate = _cursor.getString(_cursorIndexOfDate);
            }
            final String _tmpSimName;
            if (_cursor.isNull(_cursorIndexOfSimName)) {
              _tmpSimName = null;
            } else {
              _tmpSimName = _cursor.getString(_cursorIndexOfSimName);
            }
            final String _tmpUniqueCallId;
            if (_cursor.isNull(_cursorIndexOfUniqueCallId)) {
              _tmpUniqueCallId = null;
            } else {
              _tmpUniqueCallId = _cursor.getString(_cursorIndexOfUniqueCallId);
            }
            _item = new CallEntity(_tmpId,_tmpHrName,_tmpPhoneNumber,_tmpCallType,_tmpDuration,_tmpDate,_tmpSimName,_tmpUniqueCallId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<CallEntity>> getPendingCallsLiveData() {
    final String _sql = "SELECT * FROM pending_calls ORDER BY date ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"pending_calls"}, false, new Callable<List<CallEntity>>() {
      @Override
      @Nullable
      public List<CallEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHrName = CursorUtil.getColumnIndexOrThrow(_cursor, "hrName");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfCallType = CursorUtil.getColumnIndexOrThrow(_cursor, "callType");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfSimName = CursorUtil.getColumnIndexOrThrow(_cursor, "simName");
          final int _cursorIndexOfUniqueCallId = CursorUtil.getColumnIndexOrThrow(_cursor, "uniqueCallId");
          final List<CallEntity> _result = new ArrayList<CallEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CallEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpHrName;
            if (_cursor.isNull(_cursorIndexOfHrName)) {
              _tmpHrName = null;
            } else {
              _tmpHrName = _cursor.getString(_cursorIndexOfHrName);
            }
            final String _tmpPhoneNumber;
            if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
              _tmpPhoneNumber = null;
            } else {
              _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            }
            final String _tmpCallType;
            if (_cursor.isNull(_cursorIndexOfCallType)) {
              _tmpCallType = null;
            } else {
              _tmpCallType = _cursor.getString(_cursorIndexOfCallType);
            }
            final String _tmpDuration;
            if (_cursor.isNull(_cursorIndexOfDuration)) {
              _tmpDuration = null;
            } else {
              _tmpDuration = _cursor.getString(_cursorIndexOfDuration);
            }
            final String _tmpDate;
            if (_cursor.isNull(_cursorIndexOfDate)) {
              _tmpDate = null;
            } else {
              _tmpDate = _cursor.getString(_cursorIndexOfDate);
            }
            final String _tmpSimName;
            if (_cursor.isNull(_cursorIndexOfSimName)) {
              _tmpSimName = null;
            } else {
              _tmpSimName = _cursor.getString(_cursorIndexOfSimName);
            }
            final String _tmpUniqueCallId;
            if (_cursor.isNull(_cursorIndexOfUniqueCallId)) {
              _tmpUniqueCallId = null;
            } else {
              _tmpUniqueCallId = _cursor.getString(_cursorIndexOfUniqueCallId);
            }
            _item = new CallEntity(_tmpId,_tmpHrName,_tmpPhoneNumber,_tmpCallType,_tmpDuration,_tmpDate,_tmpSimName,_tmpUniqueCallId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object checkExists(final String uniqueId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM pending_calls WHERE uniqueCallId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (uniqueId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, uniqueId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
