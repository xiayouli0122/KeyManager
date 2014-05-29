package com.yuri.mykey.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import com.yuri.mykey.R;

public class KeyPopupMenu implements OnItemClickListener {
	private static final String TAG = "KeyPopupMenu";
    private PopupWindow popupWindow ;
    private ListView listView;
    private LayoutInflater inflater;
    private PopupViewClickListener mListener;
    
    private List<String> mMenuItemList = new ArrayList<String>();
    
    public interface PopupViewClickListener{
    	void onClick(PopupWindow popupWindow, int position);
    }
    
	public KeyPopupMenu(Context context, List<String> groupList){
		inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.group_menu, null);
		listView = (ListView) view.findViewById(R.id.lv_group_menu);
		listView.setOnItemClickListener(this);
		mMenuItemList.clear();
		for(String name : groupList){
			mMenuItemList.add(name);
		}
		mMenuItemList.add(context.getString(R.string.group_new));
		
		listView.setAdapter(new PopupMenuAdapter());
		
		int width = context.getResources().getDisplayMetrics().widthPixels;
		int height = LayoutParams.WRAP_CONTENT;
		popupWindow = new PopupWindow(view, width, height);
		popupWindow.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.dialog_full_holo_light));
		popupWindow.setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					popupWindow.dismiss();
					return true;
				}
				return false;
			}
		});
		
	}
	
	public void setOnPopupViewClickListener(PopupViewClickListener listener){
		mListener = listener;
	}
	
	
	//下拉式 弹出 pop菜单 parent 
	public void showAsDropDown(View parent, int xOff, int yOff) {
		popupWindow.showAsDropDown(parent, xOff, yOff);
		//focus
		popupWindow.setFocusable(true);
		//allow touchable outside
		popupWindow.setOutsideTouchable(true);
		//refresh state
		popupWindow.update();
	}
	
	/**
	 * @param parent anchor
	 * @param gravity loaction
	 * @param x offset
	 * @param y offset
	 */
	public void showAsLoaction(View parent, int gravity, int x, int y){
		popupWindow.showAtLocation(parent, gravity, x , y);
		
		//focus
		popupWindow.setFocusable(true);
		//allow touchable outside
		popupWindow.setOutsideTouchable(true);
		//refresh state
		popupWindow.update();
	}
    
	public void dismiss() {
		popupWindow.dismiss();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mListener.onClick(popupWindow, position);
		popupWindow.dismiss();
	}
	
	class PopupMenuAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mMenuItemList.size();
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
			View view = inflater.inflate(R.layout.group_menu_item, null);
			TextView textView = (TextView) view.findViewById(R.id.tv_group_menu_name);
			RadioButton radioButton = (RadioButton) view.findViewById(R.id.rb_group_menu);
			radioButton.setVisibility(View.INVISIBLE);
			textView.setText(mMenuItemList.get(position));
			return view;
		}
	}
}
