package com.yuri.mykey.setting;

import com.yuri.mykey.R;
import com.yuri.mykey.db.KeyData.KeyGroup;
import com.yuri.mykey.util.KeyManager;
import com.yuri.mykey.util.KeyUtil;
import com.yuri.mykey.util.LogUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;

/**
 * 群组管理界面
 * @author Yuri
 *
 */
public class GroupManagerActivity extends Activity implements OnClickListener,
				LoaderCallbacks<Cursor>{
	
	private static final String[] COLUMNS = {KeyGroup._ID, KeyGroup.NAME, KeyGroup.TYPE};
	private static final String TAG = "GroupManagerActivity";
	private GroupManagerAdapter mAdapter = null;
	
	private ListView mListView;
	private Button mCreateBtn;
	
	private static final int EDIT = 0;
	private static final int NEW = 1;
	
	private MyKeyListener myKeyListener = new MyKeyListener() {
		
		@Override
		public void onCallBack(Bundle bundle) {
			int flag = bundle.getInt(MyKeyListener.CALLBACK_FLAG);
			LogUtils.d(TAG, "onCallBack flag:" + flag);
			
			Message message = mHandler.obtainMessage(flag);
			message.setData(bundle);
			mHandler.removeMessages(flag);
			mHandler.sendMessage(message);
		}
	};
	
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_manager_main);
		
		KeyUtil.setShowTitleBackButton(GroupManagerActivity.this);
		
		mCreateBtn = (Button) findViewById(R.id.btn_create_group);
		mCreateBtn.setOnClickListener(this);
		
		mListView = (ListView) findViewById(R.id.lv_group_manager);
		mAdapter = new GroupManagerAdapter(this, null,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		mListView.setAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);
		
		mAdapter.registerKeyListener(myKeyListener);
		
		setResult(RESULT_CANCELED);
	}

	@Override
	public void onClick(View v) {
		showEditorDialog(NEW, null, -1);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		//loader group data from db
		return new CursorLoader(this, KeyGroup.CONTENT_URI, COLUMNS, null, null,null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		//load finished,set data to adapter
		if (null == data) {
			LogUtils.d(TAG, "onLoadFinished cursor is null");
			return;
		}
		
		Log.d(TAG, "onLoadFinished. count = " + data.getCount());
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	};
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			Bundle bundle = null;
			bundle = msg.getData();
			final long id = bundle.getLong(MyKeyListener.KEY_ITEM_ID);
			String name = bundle.getString(MyKeyListener.KEY_ITEM_GROUP_NAME);
			switch (msg.what) {
			case MyKeyListener.MSG_MODIFY_GROUP:
				showEditorDialog(EDIT, name, id);
				break;
			case MyKeyListener.MSG_DELETE_GROUP:
				AlertDialog.Builder dialog = new AlertDialog.Builder(GroupManagerActivity.this);
				dialog.setTitle(R.string.group_delete);
				dialog.setMessage(getString(R.string.group_delete_confirm_tip, name));
				dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Uri uri = Uri.parse(KeyGroup.CONTENT_URI + "/" + id);
						getContentResolver().delete(uri, null, null);
						getLoaderManager().restartLoader(0, null, GroupManagerActivity.this);
						
						setResult(RESULT_OK);
					}
				});
				dialog.setNegativeButton(android.R.string.cancel, null);
				dialog.create().show();
				break;
			default:
				break;
			}
		};
	};
	
	/**
	 * show group editor dialog
	 * @param type new or edit
	 * @param groupName the group name
	 * @param id id from db
	 */
	private void showEditorDialog(final int type, String groupName, final long id){
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.edit, null);
		final EditText editText = (EditText) view
				.findViewById(R.id.edittext);
		String title = "";
		if (NEW == type) {
			int i = 1;
			String defaultStr = getString(R.string.group);
			groupName = defaultStr + i;
			while (KeyManager.isGroupExist(getContentResolver(), groupName)) {
				i++;
				groupName = defaultStr + i;
			}
			title = getString(R.string.group_new);
		} else {
			title = getString(R.string.group_edit);
			KeyUtil.onFocusChange(editText, true);
		}
		editText.setText(groupName);
		editText.selectAll();
		AlertDialog.Builder builder = new AlertDialog.Builder(
				GroupManagerActivity.this);
		builder.setTitle(title);
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						String group = editText.getText().toString()
								.trim();
						if (group == null | group.equals("")) {
							//do nothing
						} else {
							ContentValues values = new ContentValues();
							values.put(KeyGroup.NAME, group);

							if (NEW == type) {
								values.put(KeyGroup.TYPE,
										KeyUtil.KEYGROUP_CUSTOM);
								getContentResolver().insert(
										KeyGroup.CONTENT_URI, values);
							} else {
								Uri uri = Uri.parse(KeyGroup.CONTENT_URI + "/" + id);
								getContentResolver().update(uri, values, null, null);
							}

							getLoaderManager().restartLoader(0, null,
									GroupManagerActivity.this);
							
							setResult(RESULT_OK);
						}
					}
				});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (android.R.id.home == item.getItemId()) {
			doFinish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		doFinish();
	}
	
	private void doFinish(){
		this.finish();
	}
	
	@Override
	protected void onDestroy() {
		Cursor cursor = mAdapter.getCursor();
		if (null != mAdapter && cursor != null) {
			cursor.close();
			mAdapter.changeCursor(null);
		}
		
		mAdapter.unregisterMyKeyListener(myKeyListener);
		super.onDestroy();
	}
}
