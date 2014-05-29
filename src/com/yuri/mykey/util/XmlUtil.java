package com.yuri.mykey.util;

import android.os.Environment;

public class XmlUtil {
	public static final String START_TAG = "MyKey";
	public static final String ITEM_TAG = "item";
	
	public static final String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyKey/";
	public static final String BACKUP_XML_FILE_NAME = "mykey_backup.xml";
}
