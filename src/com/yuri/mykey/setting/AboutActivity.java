package com.yuri.mykey.setting;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.yuri.mykey.R;
import com.yuri.mykey.util.KeyUtil;

public class AboutActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.about);
		KeyUtil.setShowTitleBackButton(AboutActivity.this);
		
		TextView mAuthorText = (TextView) findViewById(R.id.author);
		
		SharedPreferences sp = getSharedPreferences(KeyUtil.SHARED_NAME, MODE_PRIVATE);
		final Editor editor = sp.edit();
		mAuthorText.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				editor.putBoolean(KeyUtil.FIRST_START, true);
				editor.commit();
				return true;
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.about, menu);
		MenuItem item = menu.findItem(R.id.action_share);
		
		ShareActionProvider actionProvider = (ShareActionProvider) item.getActionProvider();
		actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
		actionProvider.setShareIntent(createShareIntent());
		return super.onCreateOptionsMenu(menu);
	}
	
	private Intent createShareIntent(){
		String sourceDir = getApplicationInfo().sourceDir;
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("*/*");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(sourceDir)));
		intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_tip));
		return intent;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (android.R.id.home == item.getItemId()) {
			this.finish();
		}
		return super.onOptionsItemSelected(item);
	}
}
