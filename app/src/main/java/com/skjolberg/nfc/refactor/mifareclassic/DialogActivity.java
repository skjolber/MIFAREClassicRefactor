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
package com.skjolberg.nfc.refactor.mifareclassic;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;

import com.skjolberg.nfc.mifareclassic.R;

//>- Begin refactoring ---- Commented by V. Bobkov 2017-12-05 ----------------------->

/**
 * This class defines the basic field and methods required in the activity to create
 * and display an AlertDialog.
 */
public abstract class DialogActivity extends AppCompatActivity {
//<- End refactoring ----------------------------------------------------------------<

	protected AlertDialog alertDialog;

//>- Begin refactoring ---- Commented by V. Bobkov 2017-12-05 ----------------------->
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_mifareclassic_keys, menu);

		return true;
	}

	/**
	 * The method shows a new AlertDialog.
	 *
	 * @param altertDialog - instance of the AlertDialog class.
	 */
//<- End refactoring ----------------------------------------------------------------<
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

//>- Begin refactoring ---- Commented by V. Bobkov 2017-12-05 ----------------------->
	/**
	 * The method hides a showed AlertDialog.
	 */
//<- End refactoring ----------------------------------------------------------------<
	public void hideDialog() {
		synchronized(this) {
			if(alertDialog != null) {
				alertDialog.cancel();
				alertDialog = null;
			}
		}
	}

}
