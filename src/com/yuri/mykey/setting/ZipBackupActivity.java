package com.yuri.mykey.setting;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.yuri.mykey.R;
import com.yuri.mykey.util.ExportToXml;
import com.yuri.mykey.util.KeyUtil;
import com.yuri.mykey.util.XmlUtil;
import com.yuri.mykey.util.ZipUtil;

/**
 * 数据备份
 * @author Yuri
 * */
public class ZipBackupActivity extends Activity implements OnClickListener{
	private static final String TAG = "ZipBackupActivity";
	
	private EditText mEditText;
	private Button mDoButton;
	private CheckBox mCheckBox;
	
	private SharedPreferences sp;
	//设置要发送的Mail收件人
	private String mBackupMail;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.backup);
		
        sp = getSharedPreferences(KeyUtil.SHARED_NAME, MODE_PRIVATE);
        mBackupMail = sp.getString(KeyUtil.MAIL, null);
		
		String name = "MyKey_" + KeyUtil.FILE_FORMAT.format(new Date());
		mEditText = (EditText)findViewById(R.id.backup_name);
		mEditText.setText(name);
		
		mDoButton = (Button)findViewById(R.id.backup_button);
		mDoButton.setOnClickListener(this);
		
		mCheckBox = (CheckBox) findViewById(R.id.cb_backup);
	}
	
	@Override
	public void onClick(View v) {
		// 判断压缩位置是否存在
		File tempFile = new File(XmlUtil.SAVE_PATH);
		if (!tempFile.exists()) {
			boolean ret = tempFile.mkdirs();
			if (!ret) {
				Log.e(TAG, XmlUtil.SAVE_PATH + " make failed");
			}
		}

		ZipBackupTask zipBackupTask = new ZipBackupTask();
		zipBackupTask.execute(XmlUtil.SAVE_PATH + mEditText.getText().toString().trim()
				+ KeyUtil.EXTENSION_ZIP);
	}

	//开始备份
	public void doBackup(File file){
		//只选择邮件客户端发送，但是不能发送附件
//		Uri uri = Uri.parse("mailto:" + mBackupMail);
//		Intent intent = new Intent(android.content.Intent.ACTION_SENDTO, uri);
//		intent.putExtra(Intent.EXTRA_SUBJECT, "MyKey Backup");
//		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
//		intent.putExtra(Intent.EXTRA_TEXT, "Mail body");
//		startActivity(intent);
		
		//发送，可以发送附件
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mBackupMail});//receiver
		intent.putExtra(Intent.EXTRA_SUBJECT, "MyKey Backup");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		intent.putExtra(Intent.EXTRA_TEXT,"Mail Body：\n");
		//
		intent.setType("application/octet-stream");
		intent.setType("message/rfc822");

		// 当无法确认发送类型的时候使用如下语句
		//	intent.setType("*/*");
//		Intent.createChooser(intent, "Choose Email Client");
		startActivity(intent);
	}
	
	private class ZipBackupTask extends AsyncTask<String, File, String>{
		ProgressDialog progressDialog;
		@Override
		protected String doInBackground(String... params) {
			//备份的文件路径
			String desPath = params[0];
			
			File xmlFile = ExportToXml.createXml(getContentResolver());
			
			File forZipFile = new File(desPath);
			
			//password archive
			ZipUtil.zipForPw(forZipFile, xmlFile);
			
			//打包后，删除文件
			if (xmlFile.exists()) {
				xmlFile.delete();
			}
			
			//update ui
			publishProgress(forZipFile);
			return null;
		}
		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(ZipBackupActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
			super.onPreExecute();
		}
		
		@Override
		protected void onProgressUpdate(File... values) {
			if (progressDialog != null) {
				progressDialog.cancel();	
			}
			
			KeyUtil.showLongToast(ZipBackupActivity.this, "Backup to " + values[0].getPath());
			
			if (mCheckBox.isChecked()) {
				doBackup(values[0]);
			}
			
			ZipBackupActivity.this.finish();
			super.onProgressUpdate(values);
		}
	}
}
