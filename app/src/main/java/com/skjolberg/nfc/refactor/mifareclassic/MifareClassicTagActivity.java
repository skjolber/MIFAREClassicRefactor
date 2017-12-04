package com.skjolberg.nfc.refactor.mifareclassic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.skjolberg.nfc.refactor.MainApplication;
import com.skjolberg.nfc.refactor.MifareClassicGalleryTag;
import com.skjolberg.nfc.refactor.mifareclassic.data.MifareClassicSectorData;
import com.skjolberg.nfc.refactor.mifareclassic.data.MifareClassicTag;
import com.skjolberg.nfc.mifareclassic.R;

public class MifareClassicTagActivity extends NfcDetectorFragmentActivity {

	private static final String FIRST_RUN_KEY = "FIRST_RUN";
	
	private static final int TRAILER_REQUEST_CODE = 1;
	private static final int DATA_REQUEST_CODE = 2;
	
	private static final String TAG = MifareClassicTagActivity.class.getName();
	
	public static final String TAGS_INSTRUCTION = "tags:instruction";

	private class MyPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
	    @Override
	    public void onPageSelected(int position) {
	        focusedPage = position;
	        
	        Log.d(TAG, "Focused page is now " + focusedPage);
	    }
	}

	private int focusedPage = 0;

	protected AlertDialog alertDialog;
	protected MifareClassicDataSource dataSource;
	
    private MifareClassicTagFragmentAdapter mAdapter;
    private ViewPager mPager;
    
    private boolean instruction = false;
    private boolean swipe = false;
    
	private boolean nxpMifare;

	private boolean firstRun = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.mifareclassic);
		
		MainApplication mainApplication = MainApplication.getInstance();
		
		this.dataSource = mainApplication.spawnMifareClassicDataSource();
		
        mAdapter = new MifareClassicTagFragmentAdapter(getSupportFragmentManager(), dataSource);

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setOnPageChangeListener(new MyPageChangeListener()); 
        
        loadPreferences();
        
		nxpMifare = hasMifare();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		firstRun = prefs.getBoolean(FIRST_RUN_KEY, true);
		
		setDetecting(true);
	}
	
	private boolean hasMifare() {
		return getPackageManager().hasSystemFeature("com.nxp.mifare");
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == TRAILER_REQUEST_CODE) {
			if(resultCode == RESULT_OK){      
				int index = data.getIntExtra("result", -1);          

				Log.d(TAG, "Select trailer index " + index);
			}
		} else if (requestCode == DATA_REQUEST_CODE) {
			if(resultCode == RESULT_OK){      
				int index = data.getIntExtra("result", -1);          

				Log.d(TAG, "Select data index " + index);
			}
		} else if (requestCode == MifareClassicKeyActivity.MIFARE_CLASSIC_KEY) {
			if(resultCode == RESULT_OK) {      
				Log.d(TAG, "Changes to keys");
				
				boolean changes = data.getBooleanExtra(MifareClassicKeyActivity.MIFARE_CLASSIC_KEY_CHANGES, false);          

				if(changes) {
					mAdapter.notifyDataSetChanged();
				}
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(this.instruction) {
			this.instruction = false;
			
			setPreference(TAGS_INSTRUCTION, false);
			
			showHelp();
		} else if(this.swipe) {
			this.swipe = false;
			
			toast(getString(R.string.swipeAuthorizationSchemes));
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		MenuItem delete = menu.findItem(R.id.menu_delete);
		if(mAdapter.getCount() > 0) {
			delete.setVisible(true);
		} else {
			delete.setVisible(false);
		}
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.menu_add: {
			showAddTagDialog();
			return true;
		}
		case R.id.menu_delete: {
			deleteTag();
			return true;
		}
		case R.id.menu_keys: {
			showMifareClassicKeyActivity();
			return true;
		}
		case R.id.menu_delete_all: {
			deleteAll();
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

	private void showMifareClassicKeyActivity() {
		Intent intent = new Intent(this, MifareClassicKeyActivity.class);

		startActivityForResult(intent, MifareClassicKeyActivity.MIFARE_CLASSIC_KEY);
	}

	private void deleteAll() {
		dataSource.deleteAll();
		
		Intent returnIntent = new Intent();
		setResult(RESULT_CANCELED, returnIntent);        
		finish();
	}

	private void deleteTag() {
		int page = focusedPage;

		dataSource.deleteTag(page);

		List<MifareClassicScheme<MifareClassicKey>> tags = dataSource.getTags();
		Log.d(TAG, "Still have " + tags.size() + " tags");

		mAdapter = new MifareClassicTagFragmentAdapter(getSupportFragmentManager(), dataSource);
		mPager.setAdapter(mAdapter);

		if(dataSource.hasTags()) {
			if(focusedPage >= tags.size()) {
				focusedPage = tags.size() - 1;
			}
			mPager.setCurrentItem(focusedPage, false);
		} else {
			focusedPage = 0;
		}
    	invalidateOptionsMenu();
		
	}

	/*
	private void setUse() {
		Log.d(TAG, "Set use");

		Intent returnIntent = new Intent();
		
		MifareClassicScheme galleryTag = dataSource.getTag(focusedPage);
		returnIntent.putExtra(DefaultNfcTagWriterActivity.MIFARE_CLASSIC_TAG, galleryTag);

		setResult(RESULT_OK, returnIntent);        
		finish();
	}
	*/

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

	private void showAddTagDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.mifareclassic_tag_dialog, null);
        
    	final EditText editText = (EditText)textEntryView.findViewById(R.id.tagname_edit);
    	StringBuffer buffer = new StringBuffer();

    	editText.setText(buffer.toString());
    	editText.setHint(R.string.nameHint);
        
    	AlertDialog.Builder builder = new AlertDialog.Builder(this)
        /*
            .setIcon(R.drawable.alert_dialog_icon)
            */
            .setTitle(R.string.mifareKeyScheme)
            .setView(textEntryView)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked OK so do some stuff */
                	
                	EditText editText = (EditText)textEntryView.findViewById(R.id.tagname_edit);
                	
                	MifareClassicScheme<MifareClassicKey> tag = new MifareClassicScheme<>();
                	tag.setTime(new Date());
                	tag.setName(editText.getText().toString());
                	
                	if(dataSource.createTag(tag)) {
                    	Log.d(TAG, "Add tag");
                    	
                    	mAdapter = new MifareClassicTagFragmentAdapter(getSupportFragmentManager(), dataSource);
                        mPager.setAdapter(mAdapter);
                        
                    	mAdapter.notifyDataSetChanged();
                    	mPager.setCurrentItem(dataSource.getTags().indexOf(tag), false);
                	}
               		invalidateOptionsMenu();
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked cancel so do some stuff */
                }
            })
            ;

		final AlertDialog alert = builder.create();
		
	    alert.setOnShowListener(new DialogInterface.OnShowListener() {
	        @Override
	        public void onShow(DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager) MifareClassicTagActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
	        }
	    });
	    
		show(alert);		
		
    	
	}

	public void showEditTagDialog() {
		LayoutInflater mInflater = LayoutInflater.from(this);
		
        final View textEntryView = mInflater.inflate(R.layout.mifareclassic_tag_dialog, null);
        
    	final EditText editText = (EditText)textEntryView.findViewById(R.id.tagname_edit);

    	final MifareClassicScheme tag = dataSource.getTag(focusedPage);
    	
    	editText.setText(tag.getName());
    	editText.setHint(R.string.nameHint);
    	editText.setSelection(editText.getText().length());
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setTitle(R.string.mifareKeyScheme)
            .setView(textEntryView)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                	EditText editText = (EditText)textEntryView.findViewById(R.id.tagname_edit);
                	
                	tag.setName(editText.getText().toString());
                	
                	if(dataSource.updateTag(tag)) {
                    	Log.d(TAG, "Update tag");

                    	mAdapter.notifyDataSetChanged();
                	}
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            })
            ;
        
		final AlertDialog alert = builder.create();
		
	    alert.setOnShowListener(new DialogInterface.OnShowListener() {
	        @Override
	        public void onShow(DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager) MifareClassicTagActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
	        }
	    });
	    
		show(alert);
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
		message.setTextSize(getPx(10));
		message.setPadding(getPx(15), getPx(15), getPx(15), getPx(15));
		final SpannableString s = new SpannableString(getText(R.string.mifareClassicAuthorizationSchemeMessage));
		Linkify.addLinks(s, Linkify.WEB_URLS);
		message.setText(s);
		message.setMovementMethod(LinkMovementMethod.getInstance());

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(message).setTitle(R.string.mifareClassicAuthorizationSchemeTitle)
				.setPositiveButton(R.string.ok, dialogClickListener);
		
		show(builder.create());
		
	}

	private void loadPreferences() {
		Log.d(getClass().getName(), "Load preferences");
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		this.instruction = prefs.getBoolean(TAGS_INSTRUCTION, true);
		
		Log.d(getClass().getName(), "Load preference " + TAGS_INSTRUCTION + ": " + instruction);
	}
	
	@Override
	protected void nfcIntentDetected(Intent intent, String action) {
		
		if(dataSource.hasTags()) {
			MifareClassicScheme<MifareClassicKey> scheme = dataSource.getTag(mPager.getCurrentItem());
		
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			
			String[] techList = tag.getTechList();

			android.nfc.tech.MifareClassic mifareClassic = null;
			for (String tech : techList) {
				Log.d(TAG, "Tech " + tech);

				if (tech.equals(android.nfc.tech.MifareClassic.class.getName())) {
					mifareClassic = android.nfc.tech.MifareClassic.get(tag);
				}

			}

			if(mifareClassic != null) {
				MifareClassicIO mifareClassicIO = new MifareClassicIO(dataSource.getAllKeys());

				try {
					MifareClassicGalleryTag mifareClassicGalleryTag = mifareClassicIO.readMifareClassicGalleryTag(mifareClassic, scheme, false);

					MifareClassicTag mifareClassicDataTag = mifareClassicGalleryTag.getMifareClassicDataTag();

					mifareClassicDataTag.printMifare();
					
					List<MifareClassicSectorData> incompleteSectors = mifareClassicDataTag.getIncompleteSectors();
					
					if(!incompleteSectors.isEmpty()) {
						
						List<MifareClassicSectorDiff> verifyMifareClassic = mifareClassicIO.verifyMifareClassic(scheme, mifareClassicDataTag);
						
						List<MifareClassicSectorData> ac = new ArrayList<>();
						List<MifareClassicSectorData> keyA = new ArrayList<>();
						List<MifareClassicSectorData> keyB = new ArrayList<>();
						
						Log.d(TAG, "Cannot write mifare classic:");
						for(int i = 0; i < incompleteSectors.size(); i++) {
							MifareClassicSectorData item = incompleteSectors.get(i);
							Log.d(TAG, item.getIndex() + ": " + item.toString());
							
							if(!item.hasTrailerBlockKeyA()) {
								keyA.add(item);
							} 
							if(!item.hasTrailerBlockKeyB()) {
								keyB.add(item);
							}
							if(!item.hasTrailerBlockAccessConditions()) {
								ac.add(item);
							}
						}
						
						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
								MifareClassicTagActivity. this);
				 
							// set title
							alertDialogBuilder.setTitle(R.string.unrefactorableTitle);
				 
							// set dialog message
							alertDialogBuilder
								.setMessage(getString(R.string.unrefactorableMessage, keyA.size(), keyB.size(), ac.size()))
								.setCancelable(true)
								.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,int id) {
										// if this button is clicked, just close
										// the dialog box and do nothing
										dialog.cancel();
									}
								});
				 
							// create alert dialog
							AlertDialog alertDialog = alertDialogBuilder.create();
			 
							// show it
							alertDialog.show();
						
					} else {
						try {
							List<MifareClassicSectorDiff> diffs = mifareClassicIO.writeMifareClassic(mifareClassic, scheme, mifareClassicDataTag, false);
							
							List<MifareClassicSectorDiff> ac = new ArrayList<>();
							List<MifareClassicSectorDiff> keyA = new ArrayList<>();
							List<MifareClassicSectorDiff> keyB = new ArrayList<>();
							
							Log.d(TAG, "Wrote mifare classic:");
							for(int i = 0; i < diffs.size(); i++) {
								MifareClassicSectorDiff item = diffs.get(i);
								Log.d(TAG, item.getIndex() + ": " + item.toString());
								
								if(item.isKeyA()) {
									keyA.add(item);
								} 
								if(item.isKeyB()) {
									keyB.add(item);
								}
								if(item.isAccessConditions()) {
									ac.add(item);
								}
							}

							Log.d(TAG, "Wrote mifare classic");
							
							if(ac.isEmpty() && keyA.isEmpty() && keyB.isEmpty()) {
								toast(getString(R.string.refactorableNotNecessary));
							} else {
								toast(getString(R.string.refactoredTag, keyA.size(), keyB.size(), ac.size()));
							}
						} catch (IOException e) {
							Log.d(TAG, "Unable to write mifare classic", e);
							toast(getString(R.string.refactorWriteFail));
						}
					}

				} catch (IOException e) {
					toast(getString(R.string.refactorFail));
					
					Log.d(TAG, "Unable to read mifare classic", e);
				}
				
			} else {
				Log.d(TAG, "Not mifare classic tag");
				
				toast(getString(R.string.refactorNotPossible));
			}
		} else {
			Log.d(TAG, "No scheme exists yet");
		}
		
	}

	/**
	 * 
	 * NFC feature was found and is currently enabled
	 * 
	 */

	@Override
	protected void onNfcStateEnabled() {
		// toast(getString(R.string.nfcAvailableEnabled));
	}

	/**
	 * 
	 * NFC feature was found but is currently disabled
	 * 
	 */

	@Override
	protected void onNfcStateDisabled() {
		toast(getString(R.string.nfcAvailableDisabled));
	}

	/**
	 * 
	 * NFC setting changed since last check. For example, the user enabled NFC
	 * in the wireless settings.
	 * 
	 */

	@Override
	protected void onNfcStateChange(boolean enabled) {
		if (enabled) {
			toast(getString(R.string.nfcSettingEnabled));
		} else {
			toast(getString(R.string.nfcSettingDisabled));
		}
	}

	/**
	 * 
	 * This device does not have NFC hardware
	 * 
	 */

	@Override
	protected void onNfcFeatureNotFound() {
		toast(getString(R.string.noNfcMessage));
	}
	
	public boolean isMifareClassicIncompatible(Tag tag) throws Exception {
		
		if(!nxpMifare) {
			String[] techList = tag.getTechList();

			boolean nfcA = false;
			boolean ndefFormatable = false;
			for (String tech : techList) {
				if (tech.equals(android.nfc.tech.NfcA.class.getName())) {
					nfcA = true;
				} else if (tech.equals(android.nfc.tech.NdefFormatable.class.getName())) {
					ndefFormatable = true;
				} else {
					return false;
				}

			}
			
			return nfcA && ndefFormatable;
		}
		return false;
	}
	
	public void toast(String message) {
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();
	}
}
