package com.skjolberg.nfc.clone.mifareclassic;

import android.os.Parcelable;


public abstract class MifareClassicKeys<A extends Parcelable> {

	protected A aKey;
	protected A bKey;
	
	public A getAKey() {
		return aKey;
	}
	
	public void setAKey(A aKey) {
		this.aKey = aKey;
	}
	
	public A getBKey() {
		return bKey;
	}
	
	public void setBKey(A bKey) {
		this.bKey = bKey;
	}
	
	public boolean hasKeyA() {
		return aKey != null;
	}

	public boolean hasKeyB() {
		return bKey != null;
	}

}
