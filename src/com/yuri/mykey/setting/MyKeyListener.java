package com.yuri.mykey.setting;

import android.os.Bundle;

public interface MyKeyListener {
	
	public static final int MSG_DELETE_GROUP = 1;
	public static final int MSG_MODIFY_GROUP = 2;
	
	public static final String CALLBACK_FLAG = "callback_flag";
	public static final String KEY_ITEM_POSITION = "key_item_position";
	public static final String KEY_ITEM_ID = "key_item_id";
	public static final String KEY_ITEM_OP_TYE = "key_item_op_type";
	public static final String KEY_ITEM_GROUP_NAME = "key_item_group_name";
	
	void onCallBack(Bundle bundle);
}
