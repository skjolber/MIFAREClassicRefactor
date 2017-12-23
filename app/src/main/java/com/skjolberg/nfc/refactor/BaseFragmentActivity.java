/***************************************************************************
 *
 * This file is part of the 'NDEF Tools for Android' project at
 * http://code.google.com/p/ndef-tools-for-android/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ****************************************************************************/
package com.skjolberg.nfc.refactor;

import com.skjolberg.nfc.mifareclassic.R;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;
public class BaseFragmentActivity extends AppCompatActivity {

	private static final String TAG = BaseFragmentActivity.class.getName();
	
	protected AlertDialog alertDialog;

	public int getPx(int dp) {
		float scale = getResources().getDisplayMetrics().density;
		return ((int) (dp * scale + 0.5f));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_mifareclassic_tags, menu);

		return true;
	}
	
	public void toast(String message) {
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();
	}
	
	public void show(AlertDialog altertDialog) {
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
	
	protected void setPreference(String key, boolean enable) {
		Log.d(TAG, "Set preference " + key + ": " + enable);
		
		// store in preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = prefs.edit();

		edit.putBoolean(key, enable);
		edit.commit();
	}

}
