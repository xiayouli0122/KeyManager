package com.yuri.mykey;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * SlideMenu Adapter
 * @author Yuri
 *
 */
public class DrawerAdapter extends BaseAdapter {
	
	private LayoutInflater mInflater = null;
	private List<String> mData = new ArrayList<String>();

	public DrawerAdapter(Context context, List<String> data) {
		mInflater = LayoutInflater.from(context);
		mData = data;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = mInflater.inflate(R.layout.loader_drawer_item, null);
		TextView textView = (TextView) view.findViewById(R.id.tv_drawer_item);
		textView.setText(mData.get(position));
		return view;
	}

}
