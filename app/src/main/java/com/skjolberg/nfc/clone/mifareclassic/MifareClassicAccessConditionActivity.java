package com.skjolberg.nfc.clone.mifareclassic;

import com.skjolberg.nfc.mifareclassic.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
 
public class MifareClassicAccessConditionActivity extends Activity {

    private static final String TAG = MifareClassicAccessConditionActivity.class.getName();
    
	public static final int MIFARE_CLASSIC_ACCESS_CONDITION = 2;
	public static final String MIFARE_CLASSIC_ACCESS_CONDITION_RESULT = "result";

    public static final String KEY_ACCESS_BITS_INDEX = "accesBitsIndex";
    public static final String KEY_ACCESS_BITS_TYPE_INDEX = "accesBitsTypeIndex";
    
    private static final int[] resources = new int[]{R.id.ac000, R.id.ac010, R.id.ac100, R.id.ac110, R.id.ac001,R.id.ac011, R.id.ac101, R.id.ac111, R.id.acxxx};
    private static final int[] checkboxes = new int[]{R.id.checkbox000, R.id.checkbox010, R.id.checkbox100, R.id.checkbox110, R.id.checkbox001,R.id.checkbox011, R.id.checkbox101, R.id.checkbox111, R.id.checkboxxxx};

    private int type = -1;
    private int index;
    
    public MifareClassicAccessConditionActivity() {
    }
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		
		index = extras.getInt(KEY_ACCESS_BITS_INDEX);
	    type = extras.getInt(KEY_ACCESS_BITS_TYPE_INDEX, -1);
	    
	    if(type == 3) {
	    	setContentView(R.layout.mifareclassic_ac_trailer);
	    	setTitle(R.string.accessBitsTrailer);
	    } else {
	    	setContentView(R.layout.mifareclassic_ac_data);
	    	setTitle(R.string.accessBitsData);
	    }

	    CheckBox checkbox;
	    if(index != -1) {
	    	checkbox = (CheckBox)findViewById(checkboxes[index]);
	    } else {
	    	checkbox = (CheckBox)findViewById(R.id.checkboxxxx);
	    }
	    checkbox.setChecked(true);
	    
	    for(int i = 0; i < resources.length; i++) {
	    	View view  = findViewById(resources[i]);
	    	final OnClickListener listener = new OnClickListener() {

	            @Override
	            public void onClick(View v) {
	            	for(int k = 0; k < resources.length; k++) {
	            		CheckBox box = (CheckBox)findViewById(checkboxes[k]);
		            	if(resources[k] == v.getId()) {
		            		box.setChecked(true);
		            		
		            		index = k;
		            		
		            		Log.d(TAG, "Selected index " + index);
		            		
		            	} else {
		            		box.setChecked(false);
		            	}
	            	}
	            }
	        };
	        
	        CheckBox box = (CheckBox)findViewById(checkboxes[i]);
	        
	        OnClickListener delegate = new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	            	View parent = (View) v.getParent();
	            	
	            	listener.onClick(parent);
	            }
	        };
	        
	    	view.setOnClickListener(listener);
	    	box.setOnClickListener(delegate);
	    }
    }
	
	public void returnResult() {
		 Intent returnIntent = new Intent();
		 returnIntent.putExtra(KEY_ACCESS_BITS_INDEX, index == checkboxes.length - 1 ? -1 : index);
		 returnIntent.putExtra(KEY_ACCESS_BITS_TYPE_INDEX, type);
		 setResult(RESULT_OK,returnIntent);     
		 finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_mifareclassic_access_bits, menu);

		return true;
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.menu_save: {
			returnResult();
			return true;
		}
		
		default:
			return super.onOptionsItemSelected(item);
		}
	}	

	
	
}