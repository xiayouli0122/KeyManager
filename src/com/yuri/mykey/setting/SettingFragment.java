package com.yuri.mykey.setting;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.yuri.mykey.MyKey;
import com.yuri.mykey.R;
import com.yuri.mykey.util.ImportFromXml;
import com.yuri.mykey.util.KeyManager;
import com.yuri.mykey.util.KeyUtil;
import com.yuri.mykey.util.LogUtils;
import com.yuri.mykey.util.XmlUtil;
import com.yuri.mykey.util.ZipUtil;

public class SettingFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener{
	private static final String TAG = "SettingFragment";
	
	private ListPreference listPreference;
	private EditTextPreference editTextPreference;
	private SharedPreferences sp;
	private CheckBoxPreference mUsePwPref;
	
	private PreferenceScreen mBackupScreen;
	private PreferenceScreen mGroupScreen;
	private PreferenceScreen mRestoreScreen;
	
	
	/**设置是否需要密码登陆，ture & false, default is false*/
	private boolean mUsePassword = false;
	/**设置密码登陆方式。0：图案登陆， 1：密码登陆; 默认登陆方式为图案登陆*/
	private String mLoginMode;
	
	private CharSequence[] restore_values = null;
	private CharSequence[] restores = null;
	
	private List<File> fileLists = new ArrayList<File>();
	
	private int mCheckItem = 0;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting_config);
		
		sp = getActivity().getSharedPreferences(KeyUtil.SHARED_NAME, Context.MODE_PRIVATE);
		
		mUsePassword = sp.getBoolean(KeyUtil.USE_PASSWORD, false);
		
		mUsePwPref = (CheckBoxPreference) findPreference("parent_need_pw_preference");
		mUsePwPref.setOnPreferenceClickListener(this);
		mUsePwPref.setChecked(mUsePassword);
		
		listPreference = (ListPreference) findPreference("list_preference");
		listPreference.setOnPreferenceChangeListener(this);
		
		mLoginMode = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(listPreference.getKey(), "-1");
		//设置显示值
		listPreference.setSummary(listPreference.getEntries()[Integer.parseInt(mLoginMode)]);
		
		//set mail
		editTextPreference = (EditTextPreference) findPreference("edit_mail_preference");
		editTextPreference.setOnPreferenceChangeListener(this);
		
		if (null != editTextPreference.getText()) {
			editTextPreference.setSummary(editTextPreference.getText());
		}
		
		//backup
		mBackupScreen = (PreferenceScreen) findPreference("backup_screen");
		mBackupScreen.setOnPreferenceClickListener(this);
		
		//group manager
		mGroupScreen = (PreferenceScreen) findPreference("group_screen");
		mGroupScreen.setOnPreferenceClickListener(this);
		
		//note_restore
		mRestoreScreen = (PreferenceScreen) findPreference("restore_screen");
		mRestoreScreen.setOnPreferenceClickListener(this);
	}
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (listPreference == preference) {
			int index = listPreference.findIndexOfValue((String)newValue);
			listPreference.setSummary(listPreference.getEntries()[index]);
			//更改选中值
			listPreference.setValueIndex(index);
			
			//保存当前选中模式，方便其他Activity使用
			Editor editor = sp.edit();
			editor.putInt(KeyUtil.LOGIN_MODE, index);
			editor.commit();
		}else if (editTextPreference == preference) {
			String backupMail = editTextPreference.getEditText().getText().toString().trim();
			editTextPreference.setSummary(backupMail);
			editTextPreference.setText(backupMail);
			Editor editor = sp.edit();
			editor.putString(KeyUtil.MAIL, backupMail);
			editor.commit();
		}
		return false;
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (mBackupScreen == preference) {
			Intent intent = new Intent(getActivity(), ZipBackupActivity.class);
			startActivity(intent);
			
		}else if (mRestoreScreen == preference) {
			getBackupsFromSdCard();
			if (restores == null) {
				String message = getString(R.string.backup_file_no_exist);
				KeyUtil.showAlertDialog(getActivity(), R.string.key_restore, message);
			} else {
				mCheckItem = restores.length - 1;
				new AlertDialog.Builder(getActivity())
				.setTitle(R.string.key_restore_title)
				.setSingleChoiceItems(restores, restores.length - 1, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mCheckItem = which;
					}
				})
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						RestoreFromZipTask restoreFromZipTask = new RestoreFromZipTask();
						restoreFromZipTask.execute((String)restore_values[mCheckItem]);
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create().show();
			}
		}else if (mUsePwPref == preference) {
			Editor editor = sp.edit();
			editor.putBoolean(KeyUtil.USE_PASSWORD, mUsePwPref.isChecked());
			editor.commit();
		}else if (mGroupScreen == preference) {
			Intent groupIntent = new Intent(getActivity(), GroupManagerActivity.class);
			startActivityForResult(groupIntent, 0);
		}
		return true;
	}
	
	/**从zip文件中恢复数据
	 * */
	public class RestoreFromZipTask extends AsyncTask<String, String, Integer>{
		ProgressDialog progressDialog;
		
		@Override
		protected Integer doInBackground(String... params) {
//			 解加密压缩包
			LogUtils.d(TAG, "doInBackground.restore:" + params[0]);
			boolean ret = ZipUtil.unZipForPw(params[0]);
			if (!ret) {
				return -1;
			}

			// import
			File backupFile = new File(XmlUtil.SAVE_PATH
					+ XmlUtil.BACKUP_XML_FILE_NAME);
			if (!backupFile.exists()) {
				return -1;
			} else {
				ImportFromXml importFromXml = new ImportFromXml(getActivity(),
						backupFile.getAbsolutePath());
				List<MyKey> keys = importFromXml.getKeysFromXml();
				for (MyKey key : keys) {
					//re-insert to db
					KeyManager.insertToDb(getActivity().getContentResolver(),
							key);
				}
				backupFile.delete();
			}
			return 0;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			
			if (progressDialog != null) {
				progressDialog.cancel();
			}
			
			if (result == 0) {
				getActivity().setResult(Activity.RESULT_OK);
				KeyUtil.showToast(getActivity(), R.string.restore_success);
			}else {
				KeyUtil.showToast(getActivity(), R.string.restore_fail);
			}
		}
	}
	
	/**获得备份文件列表*/
	private void getBackupsFromSdCard(){
		fileLists.clear();
		File file = new File(XmlUtil.SAVE_PATH);
		if (!file.exists()) {
			return;
		}
		
		File[] tempFiles = null;
		if (file.isDirectory()) {
			tempFiles = file.listFiles();
			if (tempFiles == null || tempFiles.length == 0) {
				return;
			}
			
			for (int i = 0; i < tempFiles.length; i++) {
				//获取zip文件备份列表
				if (file.listFiles()[i].getAbsolutePath().endsWith("zip")) {
					fileLists.add(tempFiles[i]);
				}
			}
			
			int size = fileLists.size();
			if (size == 0) {
				return;
			}
			
			restore_values = new CharSequence[size];
			for (int i = 0; i < size; i++) {
				restore_values[i] = fileLists.get(i).getPath();
			}
			
			restores = new CharSequence[size];
			for (int i = 0; i < size; i++) {
				restores[i] = fileLists.get(i).getName();
			}
		} else {
			return;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		getActivity().setResult(resultCode);
	}
	
}
