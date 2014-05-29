package com.yuri.mykey.setting;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yuri.mykey.R;
import com.yuri.mykey.db.KeyData.KeyGroup;
import com.yuri.mykey.util.KeyUtil;
import com.yuri.mykey.util.LogUtils;

public class GroupManagerAdapter extends CursorAdapter {
	private static final String TAG = "GroupManagerAdapter";
	private LayoutInflater mInflater;
	
	private BtnOnClickListener mModifyListener = new BtnOnClickListener(MODIFY);
	private BtnOnClickListener mDeleteListener = new BtnOnClickListener(DELETE);
	public static final int MODIFY = 0;
	public static final int DELETE = 1;

	public GroupManagerAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return mInflater.inflate(R.layout.group_manager_item, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView textView = (TextView) view.findViewById(R.id.tv_groupmanager_name);
		ImageButton modifyBtn = (ImageButton) view.findViewById(R.id.btn_modify);
		ImageButton deleteBtn = (ImageButton) view.findViewById(R.id.btn_delete);
		
		String group = cursor.getString(cursor.getColumnIndex(KeyGroup.NAME));
		textView.setText(group);
		
		int type = cursor.getInt(cursor.getColumnIndex(KeyGroup.TYPE));
		if (KeyUtil.KEYGROUP_DEFAULT == type) {
			deleteBtn.setVisibility(View.INVISIBLE);
		} else {
			deleteBtn.setVisibility(View.VISIBLE);
		}
		
		modifyBtn.setOnClickListener(mModifyListener);
		deleteBtn.setOnClickListener(mDeleteListener);
		
		MsgData msgData = new MsgData(cursor.getPosition());
		modifyBtn.setTag(msgData);
		deleteBtn.setTag(msgData);
		
	}
	
	class MsgData {
		int position;

		public MsgData(int position) {
			this.position = position;
		}
	}
	
	class BtnOnClickListener implements OnClickListener{

		int type = -1;
		
		BtnOnClickListener(int type){
			this.type = type;
		}
		
		@Override
		public void onClick(View v) {
			MsgData msgData = (MsgData) v.getTag();
			int position = msgData.position;
			
			Cursor cursor = getCursor();
			cursor.moveToPosition(position);
			long key_id = cursor.getLong(cursor.getColumnIndex(KeyGroup._ID));
			String name = cursor.getString(cursor.getColumnIndex(KeyGroup.NAME));
			
			Bundle bundle = new Bundle(3);
			if (MODIFY == type) {
				bundle.putInt(MyKeyListener.CALLBACK_FLAG, MyKeyListener.MSG_MODIFY_GROUP);
			} else if (DELETE == type) {
				bundle.putInt(MyKeyListener.CALLBACK_FLAG, MyKeyListener.MSG_DELETE_GROUP);
			} else {
				LogUtils.e(TAG, "onClick error.type:" + type);
				return;
			}
			
			bundle.putLong(MyKeyListener.KEY_ITEM_ID, key_id);
			bundle.putString(MyKeyListener.KEY_ITEM_GROUP_NAME, name);
			notifyActivityStateChanged(bundle);
		}
		
	}
	
	private static class Record{
		int mHashCode;
		MyKeyListener mCallBack;
	}
	
	private ArrayList<Record> mRecords = new ArrayList<GroupManagerAdapter.Record>();
	public void registerKeyListener(MyKeyListener callBack){
		synchronized (mRecords) {
			//register callback in adapter,if the callback is exist,just replace the event
			Record record = null;
			int hashCode = callBack.hashCode();
			final int n = mRecords.size();
			for(int i = 0; i < n ; i++){
				record = mRecords.get(i);
				if (hashCode == record.mHashCode) {
					return;
				}
			}
			
			record = new Record();
			record.mHashCode = hashCode;
			record.mCallBack = callBack;
			mRecords.add(record);
		}
	}
	
	private void notifyActivityStateChanged(Bundle bundle){
		if (!mRecords.isEmpty()) {
			LogUtils.d(TAG, "notifyActivityStateChanged.clients = " + mRecords.size());
			synchronized (mRecords) {
				Iterator<Record> iterator = mRecords.iterator();
				while (iterator.hasNext()) {
					Record record = iterator.next();
					
					MyKeyListener listener = record.mCallBack;
					if (listener == null) {
						iterator.remove();
						return;
					}
					
					listener.onCallBack(bundle);
				}
			}
		}
	}
	
	public void unregisterMyKeyListener(MyKeyListener callBack){
		remove(callBack.hashCode());
	}
	
	private void remove(int hashCode){
		synchronized (mRecords) {
			Iterator<Record> iterator =mRecords.iterator();
			while(iterator.hasNext()){
				Record record = iterator.next();
				if (record.mHashCode == hashCode) {
					iterator.remove();
				}
			}
		}
	}

}
