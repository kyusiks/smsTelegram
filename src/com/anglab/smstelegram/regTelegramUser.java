package com.anglab.smstelegram;

import android.app.Activity;
import android.os.Bundle;

public class regTelegramUser extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.view_mode);
	}

	@Override
	public void finish() {
		super.finish();

		overridePendingTransition(R.anim.hold,R.anim.slide_out_left);
	}

}
