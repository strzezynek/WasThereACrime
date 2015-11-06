package com.example.admin.wasthereacrime.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.util.Log;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by admin on 2015-11-05.
 */
public class CrimeProvider extends ContentProvider{

    private static final String TAG = CrimeProvider.class.getSimpleName();

    private static final String AUTHORITY = "com.example.admin.wasthereacrime.database";
    private static final String PROVIDER_NAME = AUTHORITY + ".CrimeProvider";
    private static final String URL = "content://" + PROVIDER_NAME + "/crime";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    private MyDatabaseHelper databaseHelper;

    private static final int ALL_CRIMES = 10;
    private static final int SINGLE_CRIME = 20;
    public static final String[] ALL_COLUMNS = new String[]{};

    public static final String DATABASE_NAME = "CrimeProvider";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_CRIME = "crime";
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_CRIME + " ("
            + CrimeColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + CrimeColumns.COL_CRIME_ID + " TEXT UNIQUE, "
            + CrimeColumns.COL_DESCRIPTION + " TEXT NOT NULL, "
            + CrimeColumns.COL_DATE + " TEXT NOT NULL, "
            + CrimeColumns.COL_TIME + " TEXT NOT NULL, "
            + CrimeColumns.COL_LAT + " TEXT NOT NULL, "
            + CrimeColumns.COL_LNG + " TEXT NOT NULL);";

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(PROVIDER_NAME, "crime", ALL_CRIMES);
        uriMatcher.addURI(PROVIDER_NAME, "crime/#", SINGLE_CRIME);
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "Creating db");
        databaseHelper = new MyDatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "QUERY");
        projection = projection == null ? ALL_COLUMNS : projection;
        sortOrder = sortOrder == null ? CrimeColumns._ID : sortOrder;

        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        switch (uriMatcher.match(uri)) {
            case ALL_CRIMES:
                return database.query(TABLE_CRIME, projection, selection, selectionArgs,
                        null, null, sortOrder);
            case SINGLE_CRIME:
                String crimeId = uri.getLastPathSegment();
                selection = fixSelectionString(selection);
                selectionArgs = fixSelectionArgs(selectionArgs, crimeId);
                return database.query(TABLE_CRIME, projection, selection, selectionArgs,
                        null, null, sortOrder);
            default:
                throw new IllegalArgumentException("Incorrect uri: " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        Uri result = null;
        try {
            result = doInsert(uri, values, database);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        database.close();
        getContext().getContentResolver().notifyChange(uri,null);
        return result;
    }

    private Uri doInsert(Uri uri, ContentValues values, SQLiteDatabase database) throws SQLException {
        Uri result = null;
        switch (uriMatcher.match(uri)){
            case ALL_CRIMES:
                long id = database.insert(TABLE_CRIME, "", values);
                if (id == -1) {
                    throw new SQLException("Błąd wstawiania danych!");
                }
                result = Uri.withAppendedPath(uri, String.valueOf(id));
        }
        return result;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] contentValues) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int count = 0;
        try {
            database.beginTransaction();
            for (int i = 0; i < contentValues.length; i++) {
                ContentValues values = contentValues[i];
                Uri resultUri = null;
                try {
                    resultUri = doInsert(uri, values, database);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if (resultUri != null) {
                    count++;
                }else {
                    i++;
                }
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int rowsDeleted = database.delete(TABLE_CRIME, selection, selectionArgs);
        database.close();
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int rowsUpdated = database.update(TABLE_CRIME, values, selection, selectionArgs);
        database.close();
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = { CrimeColumns.COL_CRIME_ID, CrimeColumns.COL_DESCRIPTION,
                CrimeColumns.COL_DATE, CrimeColumns.COL_TIME, CrimeColumns.COL_LAT, CrimeColumns.COL_LNG,
                CrimeColumns._ID };
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));

            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

    private static String fixSelectionString(String selection) {
        selection = selection == null ? CrimeColumns._ID + " = ?" :
                CrimeColumns._ID + " = ? AND (" + selection + ")";
        return selection;
    }

    private String[] fixSelectionArgs(String[] selectionArgs, String crimeId) {
        if (selectionArgs == null){
            selectionArgs = new String[]{crimeId};
        }else{
            String[] newSelectionArgs = new String[selectionArgs.length+1];
            newSelectionArgs[0] = crimeId;
            System.arraycopy(selectionArgs, 0, newSelectionArgs, 1, newSelectionArgs.length);
        }

        return selectionArgs;
    }

    public interface CrimeColumns extends BaseColumns {
        public static final String COL_CRIME_ID = "crime_id";
        public static final String COL_DESCRIPTION = "description";
        public static final String COL_DATE = "date";
        public static final String COL_TIME = "time";
        public static final String COL_LAT = "lat";
        public static final String COL_LNG = "lng";
    }

    public class MyDatabaseHelper extends SQLiteOpenHelper {

        public MyDatabaseHelper(Context context) {
            super(context,DATABASE_NAME,null,DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP IF TABLE EXISTS " + TABLE_CRIME);
            onCreate(db);
        }
    }
}
