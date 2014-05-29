package com.yuri.mykey.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import com.yuri.mykey.MyKey;

import android.content.Context;
import android.os.Looper;
import android.util.Xml;
import android.widget.Toast;

/**
 * 从xml文件中获得数据
 * @author Yuri
 *
 */
public class ImportFromXml {
	private static final String TAG = "ImportFromXml";
	private Context context;  
	  
    private List<MyKey> mKeys;  
    private String mPath;
  
    public ImportFromXml(Context context,String path) {  
        this.context = context;  
        this.mPath = path;
    }  
    
    public List<MyKey> getKeysFromXml(){  
    	  
        MyKey myKey = null;  
        XmlPullParser parser = Xml.newPullParser();  
        File file = new File(mPath);  
        try {  
            FileInputStream fis = new FileInputStream(file);  
            parser.setInput(fis, "UTF-8");  
            int event = parser.getEventType();  
            while (event != XmlPullParser.END_DOCUMENT) {  
            	LogUtils.d(TAG, "event:" + event);
                switch (event) {  
                case XmlPullParser.START_DOCUMENT:  
                    mKeys = new ArrayList<MyKey>();
                    break;  
  
                case XmlPullParser.START_TAG:
                    if ("item".equals(parser.getName())) {  
                        myKey = new MyKey();
                        myKey.setGroup(parser.getAttributeValue(0));
                        myKey.setTitle(parser.getAttributeValue(1));
                        myKey.setUsername(parser.getAttributeValue(2));
                        myKey.setPassword(parser.getAttributeValue(3));
                        myKey.setWebsite(parser.getAttributeValue(4));
                        myKey.setNote(parser.getAttributeName(5));
                        long date_added = Long.parseLong(parser.getAttributeValue(6));
                        myKey.setDateAdded(date_added);
                        long date_modified = Long.parseLong(parser.getAttributeValue(7));
                        myKey.setDateModified(date_modified);
                        
                        LogUtils.d(TAG, "group:" + myKey.getGroup());
                        LogUtils.d(TAG, "title:" + myKey.getTitle());
                        LogUtils.d(TAG, "username:" + myKey.getUsername());
                        LogUtils.d(TAG, "password:" + myKey.getPassword());
                        LogUtils.d(TAG, "website:" + myKey.getWebsite());
                        LogUtils.d(TAG, "note:" + myKey.getNote());
                        LogUtils.d(TAG, "date_added:" + myKey.getDateAdded());
                        LogUtils.d(TAG, "date_modified:" + myKey.getDateModified());
                    }  
                    break;  
                case XmlPullParser.END_TAG:
                    if ("item".equals(parser.getName())) {  
                        mKeys.add(myKey);  
                        myKey = null;  
                    }  
                    break;  
                }  
                event = parser.next();  
            }  
        } catch (Exception e) {  
            Looper.prepare();  
            Toast.makeText(context,"恢复出错", Toast.LENGTH_SHORT).show();  
            Looper.loop();  
            e.printStackTrace();  
        } 
        return mKeys;  
    }
}
