package com.skjolberg.nfc.refactor.mifareclassic;

import android.app.Activity;
import android.app.AlertDialog;

public abstract class DialogActivity extends Activity {

	protected AlertDialog alertDialog;
	
	protected void show(AlertDialog altertDialog) {
		synchronized(this) {
			if(alertDialog != null) {
				alertDialog.cancel();
			}
			// create alert dialog
			this.alertDialog = altertDialog;
			
			runOnUiThread(new Runnable() {
				public void run() {
					// show it
					alertDialog.show();
			}});
			
		}
	}
	
	public void hideDialog() {
		synchronized(this) {
			if(alertDialog != null) {
				alertDialog.cancel();
				alertDialog = null;
			}
		}
	}

}
