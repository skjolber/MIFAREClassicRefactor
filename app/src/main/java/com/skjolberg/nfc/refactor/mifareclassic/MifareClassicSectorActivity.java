package com.skjolberg.nfc.refactor.mifareclassic;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;

import com.skjolberg.nfc.refactor.MainApplication;
import com.skjolberg.nfc.mifareclassic.R;

public class MifareClassicSectorActivity extends Activity {

    private static final String TAG = MifareClassicSectorActivity.class.getName();
    
	public static final int MIFARE_CLASSIC_SECTOR = 10;
	public static final String MIFARE_CLASSIC_SECTOR_KEY = MifareClassicSector.class.getSimpleName();
	public static final String MIFARE_CLASSIC_SECTOR_INDEX_KEY = "index";

    private MifareClassicUI mifareClassicUI;

	private MifareClassicSector<MifareClassicKey> mifareClassicSector;
	private List<MifareClassicKey> allKeys;
	
	private int index = -1;

	private Spinner aKeyValue;
	private Spinner bKeyValue;
	private AutoCompleteTextView dataAccessBitsValue;
	private AutoCompleteTextView trailerAccessBitsValue;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

    	setContentView(R.layout.mifareclassic_tag_sector);
    	
    	mifareClassicUI = new MifareClassicUI(this);
    	
		Bundle extras = getIntent().getExtras();
		
		if(extras != null && extras.containsKey(MIFARE_CLASSIC_SECTOR_KEY)) {
			mifareClassicSector = extras.getParcelable(MIFARE_CLASSIC_SECTOR_KEY);
			index =  extras.getInt(MIFARE_CLASSIC_SECTOR_INDEX_KEY);
		} else {
			mifareClassicSector = new MifareClassicSector<MifareClassicKey>();
		}
		
    	final EditText sectorValue = (EditText) findViewById(R.id.sectorValue);
    	if(mifareClassicSector.hasIndex()) {
    		sectorValue.setText(Integer.toString(mifareClassicSector.getIndex()));
    		
    		sectorValue.setSelection(sectorValue.getText().length());
    	} else {
    		sectorValue.setText("");
    	}
    	
    	aKeyValue = (Spinner) findViewById(R.id.aKeyValue);
    	bKeyValue = (Spinner) findViewById(R.id.bKeyValue);
    	
		List<String> keys = new ArrayList<String>();
		keys.add(getString(R.string.noKey));

		MainApplication mainApplication = MainApplication.getInstance();
		MifareClassicDataSource mifareClassicDataSource = mainApplication.spawnMifareClassicDataSource();

		allKeys = mifareClassicDataSource.getAllKeys();
		for(MifareClassicKey key : allKeys) {
			keys.add(key.getName() + " (" + Utils.getHexString(key.getValue(), true) + ")");
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		        android.R.layout.simple_spinner_dropdown_item, keys);
    	
		aKeyValue.setAdapter(adapter);
		bKeyValue.setAdapter(adapter);
		
        if(mifareClassicSector.hasAKey()) {
        	
        	int index = allKeys.indexOf(mifareClassicSector.getAKey());
        	aKeyValue.setSelection(index + 1);
        }
        
        if(mifareClassicSector.hasBKey()) {
        	int index = allKeys.indexOf(mifareClassicSector.getBKey());
        	bKeyValue.setSelection(index + 1);
        }
        
    	dataAccessBitsValue = (AutoCompleteTextView) findViewById(R.id.dataAccessBitsValue);
    	trailerAccessBitsValue = (AutoCompleteTextView) findViewById(R.id.trailerAccessBitsValue);
        
        List<String> accessBitsList = MifareClassicUtils.getAccessBitsList();
        dataAccessBitsValue.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, accessBitsList));
        trailerAccessBitsValue.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, accessBitsList));
        
        if(mifareClassicSector.getAccessBitsDataIndex0() != -1) {
        	dataAccessBitsValue.setText(MifareClassicUtils.getAccessBits(mifareClassicSector.getAccessBitsDataIndex0()));
        }
        if(mifareClassicSector.getAccessBitsTrailerIndex() != -1) {
        	trailerAccessBitsValue.setText(MifareClassicUtils.getAccessBits(mifareClassicSector.getAccessBitsTrailerIndex()));
        }
        
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(sectorValue, InputMethodManager.SHOW_IMPLICIT);
    }
	
	public void returnResult(boolean save) {
		 Intent returnIntent = new Intent();
		 if(save) {
			 returnIntent.putExtra(MIFARE_CLASSIC_SECTOR_KEY, mifareClassicSector);
		 }
		 returnIntent.putExtra(MIFARE_CLASSIC_SECTOR_INDEX_KEY, index);
		 setResult(RESULT_OK,returnIntent);     
		 finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_mifareclassic_sector, menu);

		if(index == -1) {
			for(int i = 0; i < menu.size(); i++) {
				MenuItem item = menu.getItem(i);
				
				if(item.getItemId() == R.id.menu_delete) {
					item.setVisible(false);
				}
			}
		}
		
		return true;
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.menu_save: {
			save();
			return true;
		}

		case R.id.menu_delete: {
			returnResult(false);
			return true;
		}

		default:
			return super.onOptionsItemSelected(item);
		}

	}	
	
	public void save() {
    	final EditText sectorValue = (EditText) findViewById(R.id.sectorValue);

    	final AutoCompleteTextView dataAccessBitsValue = (AutoCompleteTextView) findViewById(R.id.dataAccessBitsValue);
    	final AutoCompleteTextView trailerAccessBitsValue = (AutoCompleteTextView) findViewById(R.id.trailerAccessBitsValue);
    	
    	// validate
		String string = sectorValue.getText().toString();
		if(string.length() == 0) {
			return;
		}

    	String dataAccessBits = dataAccessBitsValue.getText().toString().replaceAll("\\s","");
		if(dataAccessBits.length() != 0) {
			if(mifareClassicUI.getMifareAccessBitsErrorMessage(dataAccessBits) != null) {
				mifareClassicUI.setAccessBitsErrorMessage(dataAccessBitsValue);
				
				return;
			} else {
				Log.d(TAG, "Valid data access bit " + dataAccessBits);
			}
		} else {
			dataAccessBits = null;
		}
    	
    	String trailerAccessBits = trailerAccessBitsValue.getText().toString().replaceAll("\\s","");
		if(trailerAccessBits.length() != 0) {
			if(mifareClassicUI.getMifareAccessBitsErrorMessage(trailerAccessBits) != null) {
				mifareClassicUI.setAccessBitsErrorMessage(trailerAccessBitsValue);
				
				return;
			} else {
				Log.d(TAG, "Valid trailer access bit " + trailerAccessBits);
			}
		} else {
			trailerAccessBits = null;
		}
    	
		int trailerAccessBitsIndex = -1;
		if(trailerAccessBits != null) {
			trailerAccessBitsIndex = MifareClassicUtils.getAccessBitsIndex(trailerAccessBits);
		}
		
		int dataAccessBitsIndex = -1;
		if(dataAccessBits != null) {
			dataAccessBitsIndex = MifareClassicUtils.getAccessBitsIndex(dataAccessBits);
		}
		
		mifareClassicSector.setIndex(Integer.parseInt(string));
		mifareClassicSector.setAKey(getSelectedKey(aKeyValue));
		mifareClassicSector.setBKey(getSelectedKey(bKeyValue));
		mifareClassicSector.setAccessBitsTrailerIndex(trailerAccessBitsIndex);
		mifareClassicSector.setAccessBitsDataIndex0(dataAccessBitsIndex);
		mifareClassicSector.setAccessBitsDataIndex1(dataAccessBitsIndex);
		mifareClassicSector.setAccessBitsDataIndex2(dataAccessBitsIndex);

		returnResult(true);
	}
	
	private MifareClassicKey getSelectedKey(Spinner spinner) {
		int selectedItemPosition = spinner.getSelectedItemPosition();
		if(selectedItemPosition == 0) {
			return null;
		}
		return allKeys.get(selectedItemPosition - 1);
	}

	public void selectDataAccessBits(View v) {
		showAccessConditionsActivity(0, mifareClassicSector.getAccessBitsDataIndex0());
	}

	public void selectTrailerAccessBits(View v) {
		showAccessConditionsActivity(3, mifareClassicSector.getAccessBitsTrailerIndex());
	}
	
	public void showAccessConditionsActivity(int type, int index) {
		Intent i = new Intent(this, MifareClassicAccessConditionActivity.class);
		
		i.putExtra(MifareClassicAccessConditionActivity.KEY_ACCESS_BITS_TYPE_INDEX, type);
		i.putExtra(MifareClassicAccessConditionActivity.KEY_ACCESS_BITS_INDEX, index);
		
		startActivityForResult(i, MifareClassicAccessConditionActivity.MIFARE_CLASSIC_ACCESS_CONDITION);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult");
		
		if(requestCode == MifareClassicAccessConditionActivity.MIFARE_CLASSIC_ACCESS_CONDITION) {
			if(resultCode == Activity.RESULT_OK) {
				Bundle extras = data.getExtras();
				
				int accessBitTypeIndex = extras.getInt(MifareClassicAccessConditionActivity.KEY_ACCESS_BITS_TYPE_INDEX);
				int accessBitIndex = extras.getInt(MifareClassicAccessConditionActivity.KEY_ACCESS_BITS_INDEX);

				Log.d(TAG, "Access condition " + accessBitTypeIndex + " index " + accessBitIndex);

				if(accessBitTypeIndex == 3) {
					mifareClassicSector.setAccessBits(accessBitTypeIndex, accessBitIndex);
					
			        if(accessBitIndex != -1) {
			        	trailerAccessBitsValue.setText(MifareClassicUtils.getAccessBits(accessBitIndex));
			        } else {
			        	trailerAccessBitsValue.setText("");
			        }
				} else {
					mifareClassicSector.setAccessBits(0, accessBitIndex);
					mifareClassicSector.setAccessBits(1, accessBitIndex);
					mifareClassicSector.setAccessBits(2, accessBitIndex);

			        if(accessBitIndex != -1) {
			        	dataAccessBitsValue.setText(MifareClassicUtils.getAccessBits(accessBitIndex));
			        } else {
			        	dataAccessBitsValue.setText("");
			        }

				}
				
			}
			
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}	
}