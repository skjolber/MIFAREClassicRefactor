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

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.skjolberg.nfc.refactor.MainApplication;
import com.skjolberg.nfc.mifareclassic.R;

/**
 *
 */
@SuppressLint("ValidFragment")
public class MifareClassicKeyActivity extends DialogActivity implements ListView.OnItemClickListener {
 
	public static final String KEY_TAG = "tag";

	public static final String KEY_INSTRUCTION = MifareClassicKeyActivity.class.getName() + ":instruction";

    private static final String TAG = MifareClassicKeyActivity.class.getName();
    
	public static final int MIFARE_CLASSIC_KEY = 11;
	public static final String MIFARE_CLASSIC_KEY_CHANGES = MifareClassicSector.class.getName() + ":CHANGES";
    
    private KeysAdapter mAdapter;
    private ListView listView;
    private LayoutInflater mInflater;
    private List<MifareClassicKey> keys;
    
	protected AlertDialog alertDialog;
	protected MifareClassicDataSource dataSource;

	private boolean instruction = false;
	
    public MifareClassicKeyActivity() {
    }
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.mifareclassic_keys);
        
        mInflater = LayoutInflater.from(this);

		MainApplication mainApplication = MainApplication.getInstance();
		
		this.dataSource = mainApplication.spawnMifareClassicDataSource();
		this.keys = this.dataSource.getKeys();
		
        mAdapter = new KeysAdapter(this, keys);

        listView = (ListView)findViewById(R.id.keyListView);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
        
        loadPreferences();
    }

//>- Begin refactoring ---- Commented by V. Bobkov 2017-12-05 ----------------------->
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.activity_mifareclassic_keys, menu);
//
//		return true;
//	}
//<- End refactoring ----------------------------------------------------------------<

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.menu_add: {
			showKeyDialog(new MifareClassicKey());
			return true;
		}
		
		case R.id.menu_keys: {
			showReadKeys();
			return true;
		}
		
		case R.id.menu_help: {
			showHelp();
			return true;
		}

		default:
			return super.onOptionsItemSelected(item);
		}

	}	
	
	private void showReadKeys() {
		Intent intent = new Intent(this, MifareClassicKeyActivity.class);

		startActivity(intent);
	}

	
    private class KeysAdapter extends ArrayAdapter<MifareClassicKey> {
 
        public KeysAdapter(Context context, List<MifareClassicKey> objects) {
            super(context, R.layout.mifareclassic_keys_item, R.id.text, objects);
        }
 
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            Wrapper wrapper;
 
            if (view == null) {
                view = mInflater.inflate(R.layout.mifareclassic_keys_item, null);
                wrapper = new Wrapper(view);
                view.setTag(wrapper);
            } else {
                wrapper = (Wrapper) view.getTag();
            }
 
            MifareClassicKey mifareClassicKey = keys.get(position);
            wrapper.getTextView().setText(mifareClassicKey.getName());
            if(mifareClassicKey.hasValue()) {
            	wrapper.getValue().setText(Utils.getHexString(mifareClassicKey.getValue()));
            } else {
            	wrapper.getValue().setText("");
            }
            return view;
        }
 
    }
 
    // use an wrapper (or view holder) object to limit calling the
    // findViewById() method, which parses the entire structure of your
    // XML in search for the ID of your view
    private class Wrapper {
        private final View mRoot;
        private TextView name;
        private TextView value;
 
        public Wrapper(View root) {
            mRoot = root;
        }
 
        public TextView getTextView() {
            if (name == null) {
                name = (TextView) mRoot.findViewById(R.id.name);
            }
            return name;
        }
 
        public TextView getValue() {
            if (value == null) {
                value = (TextView) mRoot.findViewById(R.id.value);
            }
            return value;
        }
    }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
		Log.d(TAG, "Click on key " + index);
		
		showKeyDialog(keys.get(index));
	}
	
	public int getPx(int dp) {
		float scale = getResources().getDisplayMetrics().density;
		return ((int) (dp * scale + 0.5f));
	}
	
	private void showKeyDialog(final MifareClassicKey key) {
		Log.d(TAG, "Show add key");

		LayoutInflater factory = LayoutInflater.from(this);
		
		View message = factory.inflate(R.layout.mifareclassic_keys_item_dialog, null);
		
		message.setPadding(getPx(10), getPx(10), getPx(10), getPx(10));
		
		final EditText name = (EditText) message.findViewById(R.id.name);
		final EditText value = (EditText) message.findViewById(R.id.value);
		if(key.getId() != -1L) {
			name.setText(key.getName());
			value.setText(Utils.getHexString(key.getValue(), false));
		}
		
		value.addTextChangedListener(new TextWatcher() {
    	    public void afterTextChanged(Editable s) {
    	    	value.setError(null);
    	    }
			
    	    public void beforeTextChanged(CharSequence s, int start, int count, int after){}
    	    public void onTextChanged(CharSequence s, int start, int before, int count){
    	    }
    	});
		
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_NEGATIVE:
					// Yes button clicked
					dialog.dismiss();

					break;
				case DialogInterface.BUTTON_NEUTRAL:
					// Yes button clicked
					dialog.dismiss();

					Log.d(TAG, "Delete key " + name.getText() + " key " + name.getText());

                	dataSource.deleteKey(key);
					keys.remove(key);
					
					// delete
                	mAdapter.notifyDataSetChanged();
                	
                	Intent data = new Intent();
                	data.putExtra(MIFARE_CLASSIC_KEY_CHANGES, true);
                	setResult(Activity.RESULT_OK, data);

					break;
				case DialogInterface.BUTTON_POSITIVE:
					throw new RuntimeException();
				}
			}

		};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(message)
				.setPositiveButton(R.string.ok, dialogClickListener)
				.setNegativeButton(R.string.cancel, dialogClickListener);
		
		
		if(!dataSource.isKeyInUse(key.getId())) {
			builder.setNeutralButton(R.string.delete, dialogClickListener);
		}
		
		final AlertDialog alert = builder.create();
		
	    alert.setOnShowListener(new DialogInterface.OnShowListener() {
	        @Override
	        public void onShow(DialogInterface dialog) {
                Button button = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
    					String keyValueString = value.getText().toString().replaceAll("\\s","");
    					if(getMifareKeyErrorMessage(keyValueString) != null) {
    						setValueErrorMessage(value);
    						return;
    					}
    					alert.dismiss();

    					Log.d(TAG, "Add key " + name.getText() + " key " + name.getText());
    					
                    	key.setName(name.getText().toString());
                    	key.setValue(MifareClassicKey.hexStringToByteArray(value.getText().toString()));
                    	
                    	if(key.getId() == -1L) {
	                    	if(dataSource.createKey(key)) {
	                        	Log.d(TAG, "Add key");
    	                	} else {
    	                    	Log.e(TAG, "Problem creating key");
	                    	}
                    	} else {
                    		if(dataSource.updateKey(key)) {
    	                    	Log.d(TAG, "Updated key " + key.getId());
    	                	} else {
    	                    	Log.e(TAG, "Problem updating key " + key.getId());
    	                	}
                    	}

                    	Intent data = new Intent();
                    	data.putExtra(MIFARE_CLASSIC_KEY_CHANGES, true);
                    	setResult(Activity.RESULT_OK, data);

                    	mAdapter.notifyDataSetChanged();
                    }
                });
                
                InputMethodManager imm = (InputMethodManager) MifareClassicKeyActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(value, InputMethodManager.SHOW_IMPLICIT);
	        }
	    });
	    
		show(alert);
	}

//>- Begin refactoring ---- Commented by V. Bobkov 2017-12-05 ----------------------->
//	protected void show(AlertDialog altertDialog) {
//		synchronized(this) {
//			if(alertDialog != null) {
//				alertDialog.cancel();
//			}
//			// create alert dialog
//			this.alertDialog = altertDialog;
//
//			runOnUiThread(new Runnable() {
//				public void run() {
//					// show it
//					alertDialog.show();
//			}});
//
//		}
//	}
//
//	public void hideDialog() {
//		synchronized(this) {
//			if(alertDialog != null) {
//				alertDialog.cancel();
//				alertDialog = null;
//			}
//		}
//	}
//<- End refactoring ----------------------------------------------------------------<

	private boolean setValueErrorMessage(final EditText editText) {
		if(editText.getText().length() > 0) {
			CharSequence wepPasswordErrorMessage = getMifareKeyErrorMessage(editText.getText().toString());

			editText.setError(wepPasswordErrorMessage);
			
			return false;
		} else {
			editText.setError(null);
			
			return true;
		}
	}

	private CharSequence getMifareKeyErrorMessage(String string) {
		if(!MifareClassicKey.isHex(string.toString())) {
			return getText(R.string.mifareKeyIllegalCharacters);
		} 
		
		if(string.length() < 12) {
			return getText(R.string.mifareKeyIllegalLength);
		} else if(string.length() > 12) {
			return getText(R.string.mifareKeyIllegalLength);
		}
		return null;
	}
	
	private void showHelp() {
		Log.d(TAG, "Show help");

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// No button clicked
					dialog.dismiss();

					break;
				}
			}

		};
		
		final TextView message = new TextView(this);
		message.setPadding(getPx(15), getPx(15), getPx(15), getPx(15));
		message.setTextSize(getPx(10));
		final SpannableString s = new SpannableString(
				getText(R.string.mifareClassicKeysMessage));
		Linkify.addLinks(s, Linkify.WEB_URLS);
		message.setText(s);
		message.setMovementMethod(LinkMovementMethod.getInstance());

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(message).setTitle(R.string.mifareClassicKeysTitle)
				.setPositiveButton(R.string.ok, dialogClickListener);
		
		show(builder.create());

	}

	private void loadPreferences() {
		Log.d(getClass().getName(), "Load preferences");
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		this.instruction = prefs.getBoolean(KEY_INSTRUCTION, true);
		
		Log.d(getClass().getName(), "Load preference " + KEY_INSTRUCTION + ": " + instruction);
	}
	
	private void setPreference(String key, boolean enable) {
		Log.d(getClass().getName(), "Set preference " + key + ": " + enable);
		
		// store in preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = prefs.edit();

		edit.putBoolean(key, enable);
		edit.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(this.instruction) {
			this.instruction = false;
			
			setPreference(KEY_INSTRUCTION, false);
			
			showHelp();
		}
	}

}