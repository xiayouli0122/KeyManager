package com.yuri.mykey.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.Editable;
import android.text.Selection;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.yuri.mykey.R;
import com.yuri.mykey.db.KeyData;
import com.yuri.mykey.db.KeyData.KeyGroup;

public class KeyUtil {
	public static final String TAG = "KeyUtil";
	
	public static final int MENU_SAVE = 0x01;
	public static final int MENU_CLOSE = 0x02;
	public static final int MENU_EDIT = 0x03;
	public static final int MENU_ADD = 0x04;
	public static final int MENU_OPEN = 0x05;
	public static final int MENU_DELETE = 0x06;
	public static final int MENU_DEBUG = 0x07;
	public static final int MENU_BACKUP = 0x08;
	public static final int MENU_SELETEALL = 0x09;
	public static final int MENU_CANCELALL = 0x10;
	public static final int MENU_DELETE_MULTI = 0x11;
	public static final int MENU_SETTING = 0x12;
	public static final int MENU_EDIT_TITLE = 0x13;
	public static final int MENU_RECOVER = 0x14;
	public static final int MENU_SHARE = 0x15;
	public static final int MENU_SEARCH = 0x16;

	public static final String ITEM_ID_INDEX = "index";

	public static final String MENU_MODE = "menu_mode";
	
	public static final String PACKAGE = "com.yuri.mykey";

//	// cloumns
	public static final String[] COLUMNS_LOADER = new String[] {
			KeyData.Key._ID, KeyData.Key.TITLE,
			KeyData.Key.USERNAME};
	
	public static final String[] COLUMNS = new String[] {
		KeyData.Key._ID, KeyData.Key.TITLE,
		KeyData.Key.USERNAME, KeyData.Key.GROUP,
		KeyData.Key.PASSWORD, KeyData.Key.WEBSITE,
		KeyData.Key.NOTE
		};

	// Service Action
	public static final String ACTION_INSERT = PACKAGE + ".action_insert";
	public static final String ACTION_DELETE = PACKAGE + ".action_delete";
	public static final String ACTION_UPDATE = PACKAGE + ".action_update";
	public static final String ACTION_QUERY_ALL = PACKAGE + ".action_query_all";
	public static final String ACTION_QUERY_SINGLE = PACKAGE + ".action_query_one";

	public static final String EXTENSION_TXT = ".txt";
	public static final String EXTENSION_ZIP = ".zip";
	public static final String EXTENSION_XML = ".xml";

	public static final SimpleDateFormat FILE_FORMAT = new SimpleDateFormat(
			"yyyyMMdd_HHmm");

	// SharedPreferences
	public static final String SHARED_NAME = "key_share";
	// 判断是否设置图案密码. Type:boolean
	public static final String PATTERN_INIT_KEY = "pattern_inited";
	//设置是否设置密码登陆. Type：boolean
	public static final String USE_PASSWORD = "user_password";
	// 设定登陆方式. Type:int
	public static final String LOGIN_MODE = "login_mode";
	// 保存密码. Type:String
	public static final String PASSWORD = "password";
	// 备份邮件. Type:String
	public static final String MAIL = "mail";
	// 第一次启动，Type:boolean
	public static final String FIRST_START = "first_start";

	public static final String KEY_SEED = "SDBDSBKNDDKSJKDD";
	
	public static final String ENTER = "\n";
	
	public static final int KEYGROUP_DEFAULT = 0;
	public static final int KEYGROUP_CUSTOM = 1;

	public static final int MODE_NORMAL = 0;
	public static final int MODE_MENU = 1;

	// get time
	public static String getTime() {
		String time = "";
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		long hour = calendar.get(Calendar.HOUR_OF_DAY);
		long minute = calendar.get(Calendar.MINUTE);

		time = "" + year + "-" + month + "-" + day + " / " + hour + ":"
				+ minute;
		return time;
	}

	/** 实现复制粘贴功能 */
	public static void emulateShiftHeld(KeyEvent.Callback view) {
		try {
			KeyEvent shiftPressEvent = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
					KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0);
			shiftPressEvent.dispatch(view);
		} catch (Exception e) {
		}
	}

	/** set dialog dismiss or not, user for AlertDialog */
	public static void setDialogDismiss(DialogInterface dialog, boolean dismiss) {
		try {
			Field field = dialog.getClass().getSuperclass()
					.getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, dismiss);
			dialog.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String BufferReaderDemo(String path) throws IOException {
		File file = new File(path);

		if (!file.exists() || file.isDirectory()) {
			throw new FileNotFoundException();
		}

		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);

		String temp = null;
		StringBuffer sb = new StringBuffer();
		temp = br.readLine();

		while (temp != null) {
			sb.append(temp + "\n");
			temp = br.readLine();
		}

		fr.close();
		br.close();

		return sb.toString();
	}

	public static void showToast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	public static void showToast(Context context, int msgId) {
		Toast.makeText(context, msgId, Toast.LENGTH_SHORT).show();
	}

	public static void showLongToast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	public static void showLongToast(Context context, int msgId) {
		Toast.makeText(context, msgId, Toast.LENGTH_SHORT).show();
	}

	public static void setShowTitleBackButton(Activity activity) {
		final ActionBar bar = activity.getActionBar();
		int flags = ActionBar.DISPLAY_HOME_AS_UP;
		int change = bar.getDisplayOptions() ^ flags;
		bar.setDisplayOptions(change, flags);
	}

	/**
	 * show IME when need by manual
	 * 
	 * @param view
	 *            the view that need show IME
	 * @param hasFoucs
	 *            show or not
	 * */
	public static void onFocusChange(final View view, boolean hasFocus) {
		final boolean isFocus = hasFocus;
		(new Handler()).postDelayed(new Runnable() {
			public void run() {
				InputMethodManager imm = (InputMethodManager) view.getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (isFocus) {
					imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				} else {
					imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
			}
		}, 500);
	}

	/**
	 * 判断密码强度
	 * @param password
	 * @return
	 */
	public static int checkPasswodPower(String password){
		int num = 0;
		num = Pattern.compile("[a-z]").matcher(password).find() ? num + 1 : num;
		num = Pattern.compile("[A-Z]").matcher(password).find() ? num + 1 : num;
		num = Pattern.compile("\\d").matcher(password).find() ? num + 1 : num;
		num = Pattern.compile("[!~@#$%^&*]").matcher(password).find() ? num + 1 : num;
		return num;
	}
	
	/**
	 * 得到群组名称列表
	 * @param context
	 * @param groupList
	 */
	public static void getGroupList(Context context,
			List<String> groupList) {
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = contentResolver.query(KeyGroup.CONTENT_URI, null, null,
				null, null);
		if (null == cursor || cursor.getCount() == 0) {
			LogUtils.d(TAG, "onCreate.groupCount is null,init it ");
			String[] default_group_list = context.getResources()
					.getStringArray(R.array.default_group_list);
			ContentValues values = null;
			for (String name : default_group_list) {
				values = new ContentValues();
				values.put(KeyGroup.NAME, name);
				values.put(KeyGroup.TYPE, KeyUtil.KEYGROUP_DEFAULT);
				contentResolver.insert(KeyGroup.CONTENT_URI, values);
				groupList.add(name);
			}
		} else {
			if (cursor.moveToFirst()) {
				String name = "";
				do {
					name = cursor.getString(cursor
							.getColumnIndex(KeyGroup.NAME));
					groupList.add(name);
//					LogUtils.d(TAG, "name=" + name);
				} while (cursor.moveToNext());
				cursor.close();
			} else {
				LogUtils.e(TAG, "cursor no item");
			}
		}
	}
	
	/**
	 * set the editext selction after the text
	 * @param editText
	 */
	public static void setEditTextSelection(EditText editText){
		//让光标一直 在文本后面
		Editable editable = editText.getText();
		Selection.setSelection(editable, editable.length());
	}
	
	public static void sendToEmail(Context context, String mailAddress, File file){
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{mailAddress});
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "MyKey Backup");
		// intent.putExtra(android.content.Intent.EXTRA_TEXT,"This is Back up for notebook.\n\t"
		// + note.getContent());
		//
		intent.setType("application/octet-stream");
		//单纯的Mail
//		intent.setType("message/rfc822");

		// 当无法确认发送类型的时候使用如下语句
		//	intent.setType("*/*");
		// 当没有附件,纯文本发送时使用如下语句
		// intent.setType("plain/text");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		context.startActivity(Intent.createChooser(intent, "选择应用程序"));
	}
	
	public static void showAlertDialog(Context context, String title, String message){
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setPositiveButton(android.R.string.ok, null);
		dialog.create().show();
	}
	
	public static void showAlertDialog(Context context, int resId, String message){
		String title = context.getResources().getString(resId);
		showAlertDialog(context, title, message);
	}
}
