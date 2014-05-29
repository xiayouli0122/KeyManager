package com.yuri.mykey.setting;

import com.yuri.mykey.util.KeyUtil;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class SettingActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		KeyUtil.setShowTitleBackButton(SettingActivity.this);
		
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (android.R.id.home == item.getItemId()) {
			this.finish();
		}
		return super.onOptionsItemSelected(item);
	}
}
