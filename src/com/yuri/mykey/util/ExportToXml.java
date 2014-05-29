package com.yuri.mykey.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import com.yuri.mykey.db.KeyData;
import com.yuri.mykey.db.KeyData.Key;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.util.Xml;

//将记录导出到xml保存
public class ExportToXml {
	private static final String TAG = "ExportXml";

	/**
	 * write db data to xml file
	 * @param cr
	 * @return the xml file
	 */
	public static File createXml(ContentResolver cr){
		FileOutputStream outputStream = null;
		XmlSerializer serializer = null;
		
		File dir = new File(XmlUtil.SAVE_PATH);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		File file = new File(XmlUtil.SAVE_PATH + XmlUtil.BACKUP_XML_FILE_NAME);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}

			outputStream = new FileOutputStream(file);
			serializer = Xml.newSerializer();
			serializer.setOutput(outputStream, "UTF-8");
			serializer.startDocument("UTF-8", true);
			serializer.startTag(null, XmlUtil.START_TAG);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Cursor cursor = null;
		try {
			String[] COLUMNS = new String[] {
				KeyData.Key._ID, KeyData.Key.TITLE,
				KeyData.Key.USERNAME, KeyData.Key.GROUP,
				KeyData.Key.PASSWORD, KeyData.Key.WEBSITE,
				KeyData.Key.NOTE, KeyData.Key.DATE_ADDED,
				KeyData.Key.DATE_MODIFIED
				};
			cursor = cr.query(Key.CONTENT_URI, 
					COLUMNS, null, null, 
					"_id ASC");
			if (cursor.moveToFirst()) {
				String title;  
				String username;
				String group;
				String password;
				String website;
				String note;
				long date_added;
				long date_modified;
                do {
                	title = cursor.getString(cursor.getColumnIndex(Key.TITLE));  
                    if (title == null) {  
                    	title = "";  
                    }  
                    
                    username = cursor.getString(cursor.getColumnIndex(Key.USERNAME));  
                    if (username == null) {  
                    	username = "";  
                    }  
                    
                    group = cursor.getString(cursor.getColumnIndex(Key.GROUP));  
                    if (group == null) {  
                    	group = "";  
                    }  
                    
                    password = cursor.getString(cursor.getColumnIndex(Key.PASSWORD));  
                    if (password == null) {  
                    	password = "";  
                    }  
                    
                    note = cursor.getString(cursor.getColumnIndex(Key.NOTE));  
                    if (note == null) {  
                    	note = "";  
                    }  
                    
                    website = cursor.getString(cursor.getColumnIndex(Key.WEBSITE));  
                    if (website == null) {  
                    	website = "";  
                    }  
                    
                    date_added = cursor.getLong(cursor.getColumnIndex(Key.DATE_ADDED));  
                    date_modified = cursor.getLong(cursor.getColumnIndex(Key.DATE_MODIFIED));  
                    
                    serializer.startTag(null, XmlUtil.ITEM_TAG);  
                    serializer.attribute(null, Key.GROUP, group);  
                    serializer.attribute(null, Key.TITLE, title);  
                    serializer.attribute(null, Key.USERNAME, username);  
                    serializer.attribute(null, Key.PASSWORD, password);  
                    serializer.attribute(null, Key.WEBSITE, website);  
                    serializer.attribute(null, Key.NOTE, note);  
                    serializer.attribute(null, Key.DATE_ADDED, date_added + "");  
                    serializer.attribute(null, Key.DATE_MODIFIED, date_modified + "");  
                    serializer.endTag(null, XmlUtil.ITEM_TAG);
				} while (cursor.moveToNext());
                
                serializer.endTag(null, XmlUtil.START_TAG);
        		serializer.endDocument();
        		outputStream.flush();
        		outputStream.close();
			}else {
				return null;
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
			Log.e(TAG, "SQLiteExeption:" + e.getMessage());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if (cursor != null) {
				cursor.close();
			}
		}
		return file;
	}
}
