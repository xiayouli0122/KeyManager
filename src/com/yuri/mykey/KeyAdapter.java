package com.yuri.mykey;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuri.mykey.db.KeyData.Key;
import com.yuri.mykey.util.KeyUtil;
import com.yuri.mykey.util.LogUtils;

public class KeyAdapter extends CursorAdapter {
	private static final String TAG = "KeyAdapter";

	private final LayoutInflater mInflater;
	private String mSearchString;
	private Pattern mPattern;
	
	private static final int THEME_COLOR_DEFAULT = 0x7F33b5e5;
	
	private int mCount = 0;
	
	//default is normal mode
	private int mMode = 0;
	
	// 定义一个数组，保存每一个item是否被选中
	private boolean mCheckedArray[] = null;

	/** 被选中的数量 */
	private int mCheckedCount = 0;

	private static final int SHOW_LENGTH = 20;
	
	//for menu
	public void setMode(int mode){
		this.mMode = mode;
	}
	
	public boolean isMode(int mode){
		return mMode == mode;
	}
	
	public void setChecked(int position){
		mCheckedArray[position] = !mCheckedArray[position];
		LogUtils.d(TAG, "mCheckedArray[" + position + "]=" + mCheckedArray[position]);
		notifyDataSetChanged();
	}
	
	public int getCheckedItemCount(){
		int selectedCount = 0;
		for (int i = 0; i < mCount; i++) {
			if (mCheckedArray[i]) {
				selectedCount ++;
			}
		}
		LogUtils.d(TAG, "getCheckedItemCount = " + selectedCount);
		return selectedCount;
	}
	
	public void unSelectedAll(){
		for (int i = 0; i < mCount; i++) {
			mCheckedArray[i] = false;
		}
		notifyDataSetChanged();
	}
	
	public void selectAll(){
		for (int i = 0; i < mCount; i++) {
			mCheckedArray[i] = true;
		}
		notifyDataSetChanged();
	}
	
	public boolean isSelected(int position){
		return mCheckedArray[position];
	}
	//for menu
	public String getSearchString() {
		return mSearchString;
	}

	public void setSearchString(String searchString) {
		mSearchString = searchString;
		if (!TextUtils.isEmpty(searchString)) {
			mPattern = Pattern.compile(searchString.toLowerCase());
		}
	}

	public KeyAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
	protected void onContentChanged() {
		super.onContentChanged();
	}
	
	@Override
	public Cursor swapCursor(Cursor newCursor) {
		//当cursor改变后，重新new mCheckArray
		if (null == newCursor) {
			return newCursor;
		}
		
		Cursor oldCursor = super.swapCursor(newCursor);
		mCount = newCursor.getCount();
		LogUtils.d(TAG, "new COunt = " + mCount);
		mCheckedArray = new boolean[mCount];
		for (int i = 0; i < mCount; i++) {
			mCheckedArray[i] = false;
		}
		return oldCursor;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView snTextView = (TextView) view.findViewById(R.id.id_text);
		TextView titleTextView = (TextView) view.findViewById(R.id.tv_key_title);
		TextView contentTextView = (TextView) view.findViewById(R.id.tv_key_username);
		snTextView.setText(String.valueOf(cursor.getPosition() + 1));
		String title = cursor.getString(cursor.getColumnIndex(Key.TITLE));
		String username = cursor.getString(cursor.getColumnIndex(Key.USERNAME));
		
		if (!TextUtils.isEmpty(mSearchString)) {
			//title search
			hightLightMatcherText(titleTextView, title);
			
			//username search
			hightLightMatcherTextCut(contentTextView, username);
		} else {
			titleTextView.setText(title);
			
			int enter = username.indexOf(KeyUtil.ENTER);
			if (enter != -1) {//有回车
				username = username.substring(0, enter);
			}
			contentTextView.setText(username);
		}
		
		if (mMode == KeyUtil.MODE_MENU) {
			updateBackout(cursor.getPosition(), view);
		}else {
			// do nothing
			view.setBackgroundColor(Color.TRANSPARENT);
		}
	}
	
	private void updateBackout(int position, View view){
		if (mCheckedArray[position]) {
			view.setBackgroundColor(THEME_COLOR_DEFAULT);
		}else {
			view.setBackgroundColor(Color.TRANSPARENT);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return mInflater.inflate(R.layout.homepage_list_item, parent, false);
	}
	
	/**
	 * 高亮显示指定字符串中的字符
	 * @param view 显示字符串的textview
	 * @param text 初始字符串
	 */
	public void hightLightMatcherText(TextView view, String text){
		SpannableString spannableString = new SpannableString(text);
		Matcher matcher = mPattern.matcher(text.toLowerCase());
		while (matcher.find()) {
			spannableString.setSpan(new ForegroundColorSpan(Color.RED),
					matcher.start(), matcher.end(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		view.setText(spannableString);
	}
	
	/**
	 * 截取指定字符串的一部分，并高亮显示其中的指定字符串
	 * @param view  显示字符串的textview
	 * @param text  初始字符串
	 */
	public void hightLightMatcherTextCut(TextView view, String text){
		SpannableString spannableString = null;
		Matcher matcher = mPattern.matcher(text.toLowerCase());
		
		if (matcher.find()) {
			int start, end;
			int span_start = 0;
			if (matcher.start() - SHOW_LENGTH < 0) {
				start = 0;
				span_start = matcher.start();
			} else {
				start = matcher.start() - SHOW_LENGTH;
				span_start = SHOW_LENGTH;
			}

			if (matcher.end() + SHOW_LENGTH > text.length()) {
				end = text.length();
			} else {
				end = matcher.end() + SHOW_LENGTH;
			}

			text = text.substring(start, end);

			spannableString = new SpannableString(text);
			spannableString.setSpan(new ForegroundColorSpan(Color.RED),
					span_start, span_start + mSearchString.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			view.setText(spannableString);
		} else {
			view.setText(text);
		}
	}

}
