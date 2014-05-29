package com.yuri.mykey.db;

import com.yuri.mykey.util.KeyUtil;

import android.net.Uri;
import android.provider.BaseColumns;

public class KeyData{
	public static final String DATABASE_NAME = "key.db";
	public static final int DATABASE_VERSION = 1;

	public static final String AUTHORITY = KeyUtil.PACKAGE + ".db.keyprovider";
	
	/**Key table*/
	public static final class Key implements BaseColumns{
		public static final String TABLE_NAME = "key";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/key");
		public static final Uri CONTENT_FILTER_URI = Uri.parse("content://" + AUTHORITY + "/key_filter");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/key";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/key";
		
		//items
		/**key title. Type:String*/
		public static final String TITLE = "title";
		/**key username. Type:String*/
		public static final String USERNAME = "username";
		/**key password. Type:String*/
		public static final String PASSWORD = "password";
		/**key website. Type:String*/
		public static final String WEBSITE = "website";
		/**key group. Type:String*/
		public static final String GROUP = "key_group";
		/**key note. Type:String*/
		public static final String NOTE = "note";
		/**key create date. Type:Long*/
		public static final String DATE_ADDED = "date_added";
		/**key modify date. Type:Long*/
		public static final String DATE_MODIFIED = "date_modified";
		
		/**order by _id DESC*/
		public static final String SORT_ORDER_DEFAULT = _ID + " DESC"; 
		/**order by time DESC*/
		public static final String SORT_ORDER_DATE = DATE_MODIFIED + " DESC"; 
		/**order by title DESC*/
		public static final String SORT_ORDER_TITLE = TITLE + " DESC"; 
		
	}
	
	/**Key table*/
	public static final class KeyGroup implements BaseColumns{
		public static final String TABLE_NAME = "keygroup";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/keygroup");
		public static final Uri CONTENT_FILTER_URI = Uri.parse("content://" + AUTHORITY + "/keygroup_filter");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/keygroup";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/keygroup";
		
		//items
		/**keygroup name. Type:String*/
		public static final String NAME = "name";
		
		/**keygoup type,default or custom,Type:Integer*/
		public static final String TYPE = "type";
		
	}
}
