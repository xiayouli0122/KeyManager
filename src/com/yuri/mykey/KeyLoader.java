package com.yuri.mykey;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.yuri.mykey.db.KeyData;
import com.yuri.mykey.db.KeyData.Key;
import com.yuri.mykey.setting.SettingActivity;
import com.yuri.mykey.util.KeyUtil;
import com.yuri.mykey.util.LogUtils;

/**
 * created by yuri for mykey main ui
 * @author Yuri
 */
public class KeyLoader extends ListActivity implements OnItemClickListener,
		OnItemLongClickListener, OnQueryTextListener,
		LoaderCallbacks<Cursor>, OnAttachStateChangeListener, OnNavigationListener {
	private static final String TAG = "KeyLoader";
	/// 用户显示数据
	private ListView mListView;
	private TextView mTipsView;
	private TextView mSearchResultEmptyTextView;

	private KeyAdapter mAdapter;

	// 记录退出的时间点
	private long mExitTime = 0;

	private SearchView mSearchView;
	private String mSearchString;
	private boolean mIsSearchMode = false;
	
	private boolean isNeedRefresh = false;
	
	private static final int ADD_KEY_REQUEST = 0x01;
	private static final int EDIT_KEY_REQUEST = 0x02;
	
	private List<String> mGroupList = new ArrayList<String>();
	
	private String GROUP_ALL;
	private String mGroup;
	
	private ActionBar mActionBar;
	
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private DrawerAdapter mDrawerAdapter;
    
    private TextView mRecordCountView;
    
    private ActionBarDrawerToggle mDrawerToggle;
    
    private ModeCallBack mCallBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.loader_main);
		
		GROUP_ALL = getString(R.string.group_all);
		mGroup = GROUP_ALL;
		getActionBar().setTitle(mGroup);
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		
		//侧滑菜单主要布局
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setOnItemClickListener(this);
	
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, 
				R.string.about, R.string.about_author){
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				invalidateOptionsMenu();
			}
			
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				invalidateOptionsMenu();
			}
		};
		
		// Set up the action bar to show a dropdown list.
		mActionBar = getActionBar();
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		
		ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.homepage_actionbar, null);
		mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
		mActionBar.setCustomView(viewGroup, new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, 
				ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.RIGHT));
		
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setHomeButtonEnabled(true);
		
		mRecordCountView = (TextView) viewGroup.findViewById(R.id.record_count);
		
		//group init
		initGroup();

		mListView = getListView();
		mListView.setOnItemClickListener(this);
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		mCallBack = new ModeCallBack();
		mListView.setMultiChoiceModeListener(mCallBack);

		mAdapter = new KeyAdapter(this, null,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		mListView.setAdapter(mAdapter);

		mTipsView = (TextView) findViewById(R.id.tips);
		mSearchResultEmptyTextView = (TextView) findViewById(R.id.tv_homepage_search_result_empty);

		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void onResume() {
		if (isNeedRefresh) {
			getLoaderManager().restartLoader(0, null, this);
		}
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.key_loader, menu);

		mSearchView = (SearchView) menu.findItem(R.id.action_search)
				.getActionView();
		mSearchView.setOnQueryTextListener(this);
		mSearchView.addOnAttachStateChangeListener(this);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_search).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.action_new_add:
			intent = new Intent();
			intent.setClass(getApplicationContext(), KeyEditorActvity.class);
			startActivityForResult(intent, ADD_KEY_REQUEST);
			break;
		case R.id.action_settings:
			intent = new Intent();
			intent.setClass(KeyLoader.this, SettingActivity.class);
			startActivityForResult(intent, 0);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (parent.getId()) {
		case R.id.left_drawer:
			if (mAdapter.isMode(KeyUtil.MODE_MENU)) {
				mCallBack.actionMode.finish();
			}
			mGroup = mGroupList.get(position);
			getLoaderManager().restartLoader(0, null, this);
			getActionBar().setTitle(mGroup);
			
			mDrawerLayout.closeDrawer(mDrawerList);
			break;
		default:
			Cursor cursor = mAdapter.getCursor();
			cursor.moveToPosition(position);
			
			long key_id = cursor.getLong(cursor.getColumnIndex(Key._ID));
			
			Intent intent = new Intent(this, KeyEditorActvity.class);
			intent.putExtra("key_id", key_id);
			startActivityForResult(intent, EDIT_KEY_REQUEST);
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 再按一次back退出
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if ((System.currentTimeMillis() - mExitTime > 2000)) {
				KeyUtil.showLongToast(getApplicationContext(), R.string.twice_back);
				mExitTime = System.currentTimeMillis();
			} else {
				mExitTime = 0;
				this.finish();
			}
		}
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
			return false;
		}
		mSearchString = !TextUtils.isEmpty(newText) ? newText : null;
		mAdapter.setSearchString(newText);
		getLoaderManager().restartLoader(0, null, this);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		// Don't care about this.
		LogUtils.d(TAG, "onQueryTextSubmit");
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = null;
		String selection = null;
		String[] selectionArgs = null;
		if (mSearchString != null) {
			LogUtils.d(TAG, "onCreateLoader.mSearchString:" + mSearchString);
			uri = Uri.withAppendedPath(
					KeyData.Key.CONTENT_FILTER_URI,
					Uri.encode(mSearchString));
		}else {
			uri = KeyData.Key.CONTENT_URI;
			if (!GROUP_ALL.equals(mGroup)) {
				selection = Key.GROUP + "=?";
				selectionArgs = new String[]{mGroup};
			}
		}
		
		return new CursorLoader(this, uri, KeyUtil.COLUMNS_LOADER, selection, selectionArgs,
				KeyData.Key.SORT_ORDER_TITLE);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		if (null == cursor) {
			LogUtils.d(TAG, "onLoadFinished cursor is null");
			return;
		}
		
		Log.d(TAG, "onLoadFinished. count = " + cursor.getCount());
		mAdapter.swapCursor(cursor);
		updateTipsView(mAdapter.getCount());
		
		mRecordCountView.setText(cursor.getCount() + "");
	}

	private void updateTipsView(int resultCount) {
		if (resultCount <= 0) {
			if (mIsSearchMode && mTipsView.getVisibility() != View.VISIBLE) {
				mSearchResultEmptyTextView.setVisibility(View.VISIBLE);
			} else {
				mTipsView.setVisibility(View.VISIBLE);
				mSearchResultEmptyTextView.setVisibility(View.GONE);
			}
		} else {
			if (mIsSearchMode) {
				mSearchResultEmptyTextView.setVisibility(View.GONE);
			} else {
				mTipsView.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);

	}

	@Override
	public void onViewAttachedToWindow(View v) {
		mIsSearchMode = true;
		updateTipsView(mAdapter.getCount());
	}

	@Override
	public void onViewDetachedFromWindow(View v) {
		mIsSearchMode = false;
		if (!TextUtils.isEmpty(mSearchString)) {
			mSearchString = null;
			mAdapter.setSearchString(null);
			getLoaderManager().restartLoader(0, null, this);
		}

		updateTipsView(mAdapter.getCount());
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			isNeedRefresh = true;
			initGroup();
		}
	}

	@Override
	protected void onDestroy() {
		Cursor cursor = mAdapter.getCursor();
		if (null != mAdapter && cursor != null) {
			cursor.close();
			mAdapter.changeCursor(null);
		}
		
		super.onDestroy();
	}

	/**
	 * Listview Selection Action Mode
	 */
	private class ModeCallBack implements MultiChoiceModeListener, OnMenuItemClickListener{
		/**
		 * 用户弹出全选和取消全选的菜单的
		 */
		private PopupMenu mSelectPopupMenu = null;
		private boolean mSelectedAll = true;
		/**
		 * 显示选中多少个，以及全选和取消全选
		 */
	    private Button mSelectBtn = null;
	    
	    private ActionMode actionMode;
	    
		@Override
		public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
			Cursor cursor = mAdapter.getCursor();
			switch (item.getItemId()) {
			case R.id.action_delete:
				final ArrayList<Long> selectedList = new ArrayList<Long>();
				for (int pos = getListView().getCount() - 1; pos >= 0; pos--) {
					if (mAdapter.isSelected(pos)) {
						cursor.moveToPosition(pos);
						long id = cursor.getLong(cursor.getColumnIndex(KeyData.Key._ID));
						selectedList.add(id);
					}
				}
				
				new AlertDialog.Builder(KeyLoader.this)
						.setTitle(R.string.menu_delete)
						.setMessage(getString(R.string.delete_msg_multi, selectedList.size()))
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										//add this code,fix a bug when delete the last item
										mode.finish();
										for (int i = 0; i < selectedList.size(); i++) {
											Uri uri = Uri
													.parse(KeyData.Key.CONTENT_URI
															+ "/" + selectedList.get(i));
											getContentResolver()
											.delete(uri, null, null);
										}
//										// bug: When search string is not empty, the list can not update. Why?
										if (mIsSearchMode && !TextUtils.isEmpty(mSearchString)) {
											getLoaderManager().restartLoader(0, null, KeyLoader.this);
										}
									}
								}).setNegativeButton(android.R.string.cancel, null)
						.create().show();
				break;

			default:
				Toast.makeText(KeyLoader.this, "Clicked " + item.getTitle(),
                        Toast.LENGTH_SHORT).show();
				break;
			}
			return true;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// 最先调用的
			LayoutInflater inflater = (LayoutInflater) KeyLoader.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// 自定义ActionBar菜单
			View customView = inflater.inflate(R.layout.listview_actionbar_edit,
					null);
			mode.setCustomView(customView);
			mSelectBtn = (Button) customView.findViewById(R.id.select_button);
			mSelectBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (null == mSelectPopupMenu) {
						// 创建"全选/取消全选"的弹出菜单
						 mSelectPopupMenu = createSelectPopupMenu(mSelectBtn);
						 updateSelectPopupMenu();
						 mSelectPopupMenu.show();
					} else {
						// Update
						updateSelectPopupMenu();
						mSelectPopupMenu.show();
					}
				}
			});
			MenuInflater menuInflater = mode.getMenuInflater();
			menuInflater.inflate(R.menu.multichoice_menu, menu);
			setSubtitle(mode);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			LogUtils.i(TAG, "onDestroyActionMode");
			mAdapter.unSelectedAll();
			mAdapter.setMode(KeyUtil.MODE_NORMAL);
			
			if (actionMode != null) {
				actionMode = null;
			}
			
			if (mSelectPopupMenu != null) {
				mSelectPopupMenu.dismiss();
				mSelectPopupMenu = null;
			}
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			mAdapter.setMode(KeyUtil.MODE_MENU);
			actionMode = mode;
			int selectedCount = mAdapter.getCheckedItemCount();
			switch (selectedCount) {
			case 0:
				menu.findItem(R.id.action_delete).setEnabled(false);
				break;
			case 1:
				menu.findItem(R.id.action_delete).setEnabled(true);
				break;

			default:
				
				break;
			}
			return true;
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				return;
			}
			Log.d(TAG, "position=" + position);
			mAdapter.setChecked(position);
			setSubtitle(mode);
		}
		
		private void setSubtitle(ActionMode mode) {
			final int checkedCount = mAdapter.getCheckedItemCount();
			mSelectBtn.setText(getString(R.string.selected_msg, checkedCount));
			Menu menu = mode.getMenu();
			if (checkedCount == 0) {
				menu.findItem(R.id.action_delete).setEnabled(false);
			}else if (checkedCount == 1) {
				menu.findItem(R.id.action_delete).setEnabled(true);
			}else {
				menu.findItem(R.id.action_delete).setEnabled(true);
			}
		}
		
		private PopupMenu createSelectPopupMenu(View anchorView) {
	        final PopupMenu popupMenu = new PopupMenu(KeyLoader.this, anchorView);
	        popupMenu.inflate(R.menu.select_popup_menu);
	        popupMenu.setOnMenuItemClickListener(this);
	        return popupMenu;
	    }
		
		private void updateSelectPopupMenu(){
			if (mSelectPopupMenu == null) {
	            mSelectPopupMenu = createSelectPopupMenu(mSelectBtn);
	            return;
	        }
			final Menu menu = mSelectPopupMenu.getMenu();
			int selectedCount = mAdapter.getCheckedItemCount();
			if (getListView().getCount() == 0) {
				menu.findItem(R.id.select).setEnabled(false);
			}else {
				if (getListView().getCount() != selectedCount) {
					menu.findItem(R.id.select).setTitle(R.string.seleteall);
					mSelectedAll = true;
				}else {
					menu.findItem(R.id.select).setTitle(R.string.cancelall);
					mSelectedAll = false;
				}
			}
		}

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			switch (item.getItemId()) {
			case R.id.select:
				if (mSelectedAll) {
					mAdapter.selectAll();
				}else {
					mAdapter.unSelectedAll();
				}
				
				setSubtitle(actionMode);
	           
				updateSelectPopupMenu();
				
				if (actionMode != null) {
					actionMode.invalidate();
				}
				
				break;

			default:
				break;
			}
			return true;
		}
		
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		mGroup = mGroupList.get(itemPosition);
		getLoaderManager().restartLoader(0, null, this);
		return true;
	}
	
	/**
	 * init group data
	 */
	private void initGroup(){
		mGroupList.clear();
		
		mGroupList.add(GROUP_ALL);
		
		KeyUtil.getGroupList(getApplicationContext(), mGroupList);
		
		mDrawerAdapter = new DrawerAdapter(getApplicationContext(), mGroupList);
		mDrawerList.setAdapter(mDrawerAdapter);
	}
	
}
