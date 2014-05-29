package com.yuri.mykey;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import com.yuri.mykey.db.KeyData.Key;
import com.yuri.mykey.db.KeyData.KeyGroup;
import com.yuri.mykey.util.KeyManager;
import com.yuri.mykey.util.KeyUtil;
import com.yuri.mykey.util.LogUtils;
import com.yuri.mykey.util.SimpleCrypto;
import com.yuri.mykey.view.KeyPopupMenu;
import com.yuri.mykey.view.KeyPopupMenu.PopupViewClickListener;

/**
 * 记录编辑界面
 * @author Yuri
 *
 */
public class KeyEditorActvity extends Activity implements OnClickListener, OnCheckedChangeListener {
	private static final String TAG = "KeyEditorActvity";
	
	private EditText mTitleEdit;
	private EditText mUserNameEdit;
	private EditText mPasswordEdit;
	private EditText mWebsiteEit;
	private EditText mNoteEdit;
	
	private View mIconView;
	
	private CheckBox mShowPwCB;
	
	private TextView mGroupNameTV;
	private View mGroupNameView;
	
	private RadioButton mWakeRadioButton,mMiddleRadioButton,mPowerRadioButton;
	
	private List<String> mGroupList = new ArrayList<String>();
	private String mGroup = "Default";
	
	/**if key_id=-1,is new add </br>
	 * if key_id!=-1,is edit key
	 * */
	private long key_id = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_main);
		
		if (getIntent().hasExtra("key_id")) {
			key_id = getIntent().getLongExtra("key_id", -1);
		}
		
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			View customActionBarView = inflater.inflate(R.layout.editor_custom_actionbar, null);
			
			View saveMenuItem = customActionBarView.findViewById(R.id.save_menu_item);
			saveMenuItem.setOnClickListener(this);
			
			if (key_id != -1) {
				View cancelMenuItem = customActionBarView.findViewById(R.id.cancel_menu_item);
				cancelMenuItem.setVisibility(View.VISIBLE);
				cancelMenuItem.setOnClickListener(this);
			}
			
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM |
					ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
			actionBar.setCustomView(customActionBarView);
		}
		
		initView();
		
		KeyUtil.getGroupList(getApplicationContext(), mGroupList);
		
		setResult(RESULT_CANCELED);
	}
	
	/**
	 * init view resource
	 */
	private void initView(){
		
		String group = "";
		String title = "";
		String userName = "";
		String password = "";
		String website = "";
		String note = "";
		
		if (key_id != -1) {
			Uri uri = Uri.parse(Key.CONTENT_URI + "/" + key_id);
			Cursor cursor = null;
			try {
				cursor = getContentResolver().query(uri, KeyUtil.COLUMNS, null, null, null);
				if (cursor.moveToFirst()) {
					group = cursor.getString(cursor.getColumnIndex(Key.GROUP));
					title = cursor.getString(cursor.getColumnIndex(Key.TITLE));
					userName = cursor.getString(cursor.getColumnIndex(Key.USERNAME));
					password = cursor.getString(cursor.getColumnIndex(Key.PASSWORD));
					website = cursor.getString(cursor.getColumnIndex(Key.WEBSITE));
					note = cursor.getString(cursor.getColumnIndex(Key.NOTE));
				}
				//解密
				password = SimpleCrypto.decrypt(KeyUtil.KEY_SEED, password);
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		
		mGroupNameTV = (TextView) findViewById(R.id.tv_group_name);
		if (!group.equals("")) {
			mGroupNameTV.setText(group);
		}
		mGroupNameView = findViewById(R.id.rl_group);
		mGroupNameView.setOnClickListener(this);
		
		mTitleEdit = (EditText) findViewById(R.id.et_title);
		mTitleEdit.setText(title);
		
		mUserNameEdit = (EditText) findViewById(R.id.et_username);
		mUserNameEdit.setText(userName);
		KeyUtil.setEditTextSelection(mUserNameEdit);
		
		mPasswordEdit = (EditText) findViewById(R.id.et_password);
		mPasswordEdit.setText(password);
		mPasswordEdit.addTextChangedListener(passwordWatcher);
		
		//confirm pw edit no need,so set ite gone
		View confirmPwView = findViewById(R.id.rl_confirm_password);
		confirmPwView.setVisibility(View.GONE);
		
		mWebsiteEit = (EditText) findViewById(R.id.et_website);
		mWebsiteEit.setText(website);
		
		mNoteEdit = (EditText) findViewById(R.id.et_note);
		mNoteEdit.setText(note);
		
		mIconView = findViewById(R.id.ll_icon);
		
		mShowPwCB = (CheckBox) findViewById(R.id.cb_showpw);
		mShowPwCB.setText(R.string.show_pw);
		mShowPwCB.setOnCheckedChangeListener(this);
		
		mWakeRadioButton = (RadioButton) findViewById(R.id.rb_wake);
		mMiddleRadioButton = (RadioButton) findViewById(R.id.rb_middle);
		mPowerRadioButton = (RadioButton) findViewById(R.id.rb_power);
		checkPassword(password);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_group:
			showGroupPopupMenu();
			break;
		case R.id.save_menu_item:
			String title = mTitleEdit.getText().toString().trim();
			String username = mUserNameEdit.getText().toString().trim();
			if (username == null | username.equals("")) {
				KeyUtil.showToast(getApplicationContext(), R.string.username_null_tip);
				return;
			}
			
			String password = mPasswordEdit.getText().toString().trim();
			//加密保存
			try {
				password = SimpleCrypto.encrypt(KeyUtil.KEY_SEED, password);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String website = mWebsiteEit.getText().toString().trim();
			String note = mNoteEdit.getText().toString().trim();
			
			ContentValues values = new ContentValues();
			values.put(Key.TITLE, title);
			values.put(Key.USERNAME, username);
			values.put(Key.PASSWORD, password);
			values.put(Key.WEBSITE, website);
			values.put(Key.NOTE, note);
			values.put(Key.GROUP, mGroup);
			
			if (key_id == -1) {
				values.put(Key.DATE_ADDED, System.currentTimeMillis());
				getContentResolver().insert(Key.CONTENT_URI, values);
			}else {
				values.put(Key.DATE_MODIFIED, System.currentTimeMillis());
				Uri uri = Uri.parse(Key.CONTENT_URI + "/" + key_id);
				getContentResolver().update(uri, values, null, null);
			}
			
			setResult(RESULT_OK);
			this.finish();
			break;
		case R.id.cancel_menu_item:
			this.finish();
			break;
		case R.id.ll_icon:
			break;

		default:
			break;
		}
	}
	
	//Edittext edit listener
	TextWatcher passwordWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			//don't care about this
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			//don't care about this
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			String password = mPasswordEdit.getText().toString().trim();
			if (password == null || password.equals("")) {
				mWakeRadioButton.setChecked(false);
				mMiddleRadioButton.setChecked(false);
				mPowerRadioButton.setChecked(false);
			}else {
				checkPassword(password);
			}
		}
	};
	
	/**
	 * show group select popup menu
	 * 
	 */
	private void showGroupPopupMenu(){
		KeyPopupMenu popupMenu = new KeyPopupMenu(this, mGroupList);
		popupMenu.showAsDropDown(mGroupNameView, 0, -2);
		popupMenu.setOnPopupViewClickListener(new PopupViewClickListener() {
			@Override
			public void onClick(PopupWindow popupWindow, int position) {
				if (position == mGroupList.size()) {
					//create new group
					showCreateGroupDialog();
				}else {
					mGroup = mGroupList.get(position);
					mGroupNameTV.setText(mGroup);
					LogUtils.d(TAG, "onClick.mGroup=" + mGroup);
				}
			}
		});
	}
	
	private void showCreateGroupDialog(){
		int i = 1;
		String defaultStr = getString(R.string.group);
		String group = defaultStr + i;
		while (KeyManager.isGroupExist(getContentResolver(), group)) {
			i++;
			group = defaultStr + i;
		}
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.edit, null);
		final EditText editText = (EditText) view.findViewById(R.id.edittext);
		editText.setText(group);
		editText.selectAll();
		AlertDialog.Builder builder = new AlertDialog.Builder(KeyEditorActvity.this);
		builder.setTitle(R.string.group_new);
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String group = editText.getText().toString().trim();
				if (group == null | group.equals("")) {
					//do nothing
				}else {
					if (KeyManager.isGroupExist(getContentResolver(), group)) {
						String msg = getString(R.string.group_exist, group);
						KeyUtil.showToast(getApplicationContext(), msg);
					} else {
						mGroupList.add(group);
						insertToGroup(group);
						
						setResult(RESULT_OK);
					}
					mGroupNameTV.setText(group);
				}
				showGroupPopupMenu();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}
	
	/**
	 * add new group to db
	 * @param group
	 */
	private void insertToGroup(String group){
		ContentValues values = new ContentValues();
		values.put(KeyGroup.NAME, group);
		values.put(KeyGroup.TYPE, KeyUtil.KEYGROUP_CUSTOM);
		
		getContentResolver().insert(KeyGroup.CONTENT_URI, values);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			mPasswordEdit.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		}else {
			mPasswordEdit.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
		}
		
		KeyUtil.setEditTextSelection(mPasswordEdit);
	}
	
	/**
	 * check password power:wake,middle or power
	 * @param password
	 */
	private void checkPassword(String password){
		if (password.length() < 6) {
			mWakeRadioButton.setChecked(true);
			mMiddleRadioButton.setChecked(false);
			mPowerRadioButton.setChecked(false);
		}else {
			int num = KeyUtil.checkPasswodPower(password);
			if (num <= 1) {
				mWakeRadioButton.setChecked(true);
				mMiddleRadioButton.setChecked(false);
				mPowerRadioButton.setChecked(false);
			}else if ( num == 2) {
				mWakeRadioButton.setChecked(false);
				mMiddleRadioButton.setChecked(true);
				mPowerRadioButton.setChecked(false);
			}else {
				mWakeRadioButton.setChecked(false);
				mMiddleRadioButton.setChecked(false);
				mPowerRadioButton.setChecked(true);
			}
		}
	}
}
