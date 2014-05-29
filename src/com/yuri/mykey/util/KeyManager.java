package com.yuri.mykey.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.yuri.mykey.MyKey;
import com.yuri.mykey.db.KeyData.Key;
import com.yuri.mykey.db.KeyData.KeyGroup;

public class KeyManager {

	/**
	 * 将记录插入到数据库中
	 * @param cr
	 * @param myKey
	 */
	public static void insertToDb(ContentResolver cr, MyKey myKey) {
		ContentValues values = null;
		values = new ContentValues();
		values.put(Key.GROUP, myKey.getGroup());
		values.put(Key.TITLE, myKey.getTitle());
		values.put(Key.USERNAME, myKey.getUsername());
		values.put(Key.PASSWORD, myKey.getPassword());
		values.put(Key.WEBSITE, myKey.getWebsite());
		values.put(Key.NOTE, myKey.getNote());
		values.put(Key.DATE_ADDED, myKey.getDateAdded());
		values.put(Key.DATE_MODIFIED, myKey.getDateModified());

		cr.insert(Key.CONTENT_URI, values);
	}
	
	/**
	 * 判断当前群组是否存在
	 * @param cr
	 * @param groupName
	 * @return
	 */
	public static boolean isGroupExist(ContentResolver cr, String groupName){
		String selection = KeyGroup.NAME + "=?";
		String[] selectionArgs = {groupName};
		Cursor cursor = cr.query(KeyGroup.CONTENT_URI, null, selection, selectionArgs, null);
		if (cursor == null) {
			return false;
		}else if(cursor.getCount() == 0){
			cursor.close();
			return false;
		}
		return true;
	}
}
