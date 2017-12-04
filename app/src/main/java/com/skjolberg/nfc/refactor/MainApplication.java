package com.skjolberg.nfc.refactor;

import com.skjolberg.nfc.refactor.mifareclassic.MifareClassicDataSource;

import android.app.Application;
import android.util.Log;

public class MainApplication extends Application {
	
	public static final String SERVER = "wifi-enterprise.appspot.com";

    private static MainApplication sInstance;
 
    public static MainApplication getInstance() {
    	return sInstance;
    }

	private MifareClassicDataSource mifareClassicDataSource;

    @Override
    public void onCreate() {
      super.onCreate();  
      sInstance = this;
      
      Log.d(getClass().getSimpleName(), "Create application");
    }


	public MifareClassicDataSource spawnMifareClassicDataSource() {
		if(mifareClassicDataSource == null) {
			mifareClassicDataSource = new MifareClassicDataSource(this);
			mifareClassicDataSource.loadAll();
		}
		return mifareClassicDataSource;
	}

	public void clearMifareClassicDataSource() {
		if(mifareClassicDataSource != null) {
			mifareClassicDataSource.close();
			mifareClassicDataSource = null;
		}
	}

}