package com.yuri.mykey.db;

import com.yuri.mykey.db.KeyData.KeyGroup;
import com.yuri.mykey.util.LogUtils;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class KeyProvider extends ContentProvider {
	private static final String TAG = "KeyProvider";
	
	private SQLiteDatabase mSqLiteDatabase;
	private DatabaseHelper mDatabaseHelper;
	
	public static final int KEY_COLLECTION = 1;
	public static final int KEY_SINGLE = 2;
	public static final int KEY_FILTER = 3;
	
	public static final int KEYGROUP_COLLECTION = 11;
	public static final int KEYGROUP_SINGLE = 12;
	public static final int KEYGROUP_FILTER = 13;
	
	public static final UriMatcher uriMatcher;
	
	static{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(KeyData.AUTHORITY, "key", KEY_COLLECTION);
		uriMatcher.addURI(KeyData.AUTHORITY, "key/#", KEY_SINGLE);
		uriMatcher.addURI(KeyData.AUTHORITY, "key_filter/*", KEY_FILTER);
		
		uriMatcher.addURI(KeyData.AUTHORITY, "keygroup", KEYGROUP_COLLECTION);
		uriMatcher.addURI(KeyData.AUTHORITY, "keygroup/#", KEYGROUP_SINGLE);
		uriMatcher.addURI(KeyData.AUTHORITY, "keygroup_filter/*", KEYGROUP_FILTER);
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(Context context) {
			super(context, KeyData.DATABASE_NAME, null, KeyData.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			//key table
			db.execSQL("create table " + KeyData.Key.TABLE_NAME
					+ " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ KeyData.Key.TITLE + " TEXT, "
					+ KeyData.Key.USERNAME + " TEXT, "
					+ KeyData.Key.PASSWORD + " TEXT, "
					+ KeyData.Key.WEBSITE + " TEXT, "
					+ KeyData.Key.GROUP + " TEXT, "
					+ KeyData.Key.NOTE + " TEXT, "
					+ KeyData.Key.DATE_ADDED + " LONG, "
					+ KeyData.Key.DATE_MODIFIED + " LONG);"
					);
			
			db.execSQL("create table " + KeyData.KeyGroup.TABLE_NAME
					+ " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ KeyData.KeyGroup.NAME + " TEXT, "
					+ KeyData.KeyGroup.TYPE + " INTEGER);"
					);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//如果数据库版本发生变化，则删掉重建
			db.execSQL("DROP TABLE IF EXISTS " + KeyData.Key.TABLE_NAME);
			
			db.execSQL("DROP TABLE IF EXISTS " + KeyData.KeyGroup.TABLE_NAME);
			onCreate(db);
		}
		
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
		
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case KEY_COLLECTION:
			count = mSqLiteDatabase.delete(KeyData.Key.TABLE_NAME, selection, selectionArgs);
			break;
		case KEY_SINGLE:
			String segment = uri.getPathSegments().get(1);
			if (selection != null && segment.length() > 0) {
				selection = "_id=" + segment + " AND (" + selection + ")";
			}else {
				selection = "_id=" +  segment;
			}
			count = mSqLiteDatabase.delete(KeyData.Key.TABLE_NAME, selection, selectionArgs);
			break;
		case KEYGROUP_COLLECTION:
			count = mSqLiteDatabase.delete(KeyData.KeyGroup.TABLE_NAME, selection, selectionArgs);
			break;
		case KEYGROUP_SINGLE:
			String segment2 = uri.getPathSegments().get(1);
			if (selection != null && segment2.length() > 0) {
				selection = "_id=" + segment2 + " AND (" + selection + ")";
			}else {
				selection = "_id=" +  segment2;
			}
			count = mSqLiteDatabase.delete(KeyData.KeyGroup.TABLE_NAME, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("UnKnow Uri:" + uri);
		}
		
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case KEY_COLLECTION:
			return KeyData.Key.CONTENT_TYPE;
		case KEY_SINGLE:
			return KeyData.Key.CONTENT_TYPE_ITEM;
		case KEYGROUP_COLLECTION:
			return KeyData.KeyGroup.CONTENT_TYPE;
		case KEYGROUP_SINGLE:
			return KeyData.KeyGroup.CONTENT_TYPE_ITEM;
		default:
			throw new IllegalArgumentException("Unkonw uri:" + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(TAG, "insert db:" + uri);
		switch (uriMatcher.match(uri)) {
		case KEY_COLLECTION:
		case KEY_SINGLE:
			mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
			long rowId = mSqLiteDatabase.insertWithOnConflict(KeyData.Key.TABLE_NAME, "", 
					values, SQLiteDatabase.CONFLICT_REPLACE);
			if (rowId > 0) {
				Uri rowUri = ContentUris.withAppendedId(KeyData.Key.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(uri, null);
				return rowUri;
			}
			throw new IllegalArgumentException("Cannot insert into uri:" + uri);
		case KEYGROUP_COLLECTION:
		case KEYGROUP_SINGLE:
			mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
			long rowId2 = mSqLiteDatabase.insertWithOnConflict(KeyData.KeyGroup.TABLE_NAME, "", 
					values, SQLiteDatabase.CONFLICT_REPLACE);
			if (rowId2 > 0) {
				Uri rowUri = ContentUris.withAppendedId(KeyData.KeyGroup.CONTENT_URI, rowId2);
				getContext().getContentResolver().notifyChange(uri, null);
				return rowUri;
			}
			throw new IllegalArgumentException("Cannot insert into uri:" + uri);
		default:
			throw new IllegalArgumentException("Unknow uri:" + uri);
		}
	}

	@Override
	public boolean onCreate() {
		mDatabaseHelper = new DatabaseHelper(getContext());
		return (mDatabaseHelper == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		switch (uriMatcher.match(uri)) {
		case KEY_COLLECTION:
			qb.setTables(KeyData.Key.TABLE_NAME);
			break;
		case KEY_SINGLE:
			qb.setTables(KeyData.Key.TABLE_NAME);
			qb.appendWhere("_id=");
			qb.appendWhere(uri.getPathSegments().get(1));
			break;
		case KEY_FILTER:
			LogUtils.d(TAG, "KEY_FILTER:" + uri + ",serarch:" + uri.getPathSegments().get(1));
			qb.setTables(KeyData.Key.TABLE_NAME);

			qb.appendWhere(KeyData.Key.TITLE + " like \'%"
					+ uri.getPathSegments().get(1) + "%\'");
			qb.appendWhere(" or ");

			qb.appendWhere(KeyData.Key.USERNAME + " like \'%"
					+ uri.getPathSegments().get(1) + "%\'");
			
			qb.appendWhere(" or ");
			qb.appendWhere(KeyData.Key.WEBSITE + " like \'%"
					+ uri.getPathSegments().get(1) + "%\'");
			
			qb.appendWhere(" or ");
			qb.appendWhere(KeyData.Key.NOTE + " like \'%"
					+ uri.getPathSegments().get(1) + "%\'");
			break;
		case KEYGROUP_COLLECTION:
			qb.setTables(KeyGroup.TABLE_NAME);
			break;
		case KEYGROUP_SINGLE:
			qb.setTables(KeyGroup.TABLE_NAME);
			qb.appendWhere("_id=");
			qb.appendWhere(uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknow uri:" + uri);
		}
		
		mSqLiteDatabase = mDatabaseHelper.getReadableDatabase();
		Cursor ret = qb.query(mSqLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder);
		
		if (ret != null) {
			ret.setNotificationUri(getContext().getContentResolver(), uri);
		}
		
		return ret;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count;
		long rowId = 0;
		int match = uriMatcher.match(uri);
		mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
		
		switch (match) {
		case KEY_SINGLE:
			String segment = uri.getPathSegments().get(1);
			rowId = Long.parseLong(segment);
			
			count = mSqLiteDatabase.update(KeyData.Key.TABLE_NAME, values, "_id=" + rowId, null);
			break;
		case KEY_COLLECTION:
			count = mSqLiteDatabase.update(KeyData.Key.TABLE_NAME, values, selection, null);
			break;
		case KEYGROUP_SINGLE:
			String segment2 = uri.getPathSegments().get(1);
			rowId = Long.parseLong(segment2);
			
			count = mSqLiteDatabase.update(KeyData.KeyGroup.TABLE_NAME, values, "_id=" + rowId, null);
			break;
		case KEYGROUP_COLLECTION:
			count = mSqLiteDatabase.update(KeyData.KeyGroup.TABLE_NAME, values, selection, null);
			break;
		default:
			throw new UnsupportedOperationException("Cannot update uri:" + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return count;
	}

}
