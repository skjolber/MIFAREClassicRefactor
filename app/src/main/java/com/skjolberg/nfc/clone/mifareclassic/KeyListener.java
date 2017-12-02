package com.skjolberg.nfc.clone.mifareclassic;

public interface KeyListener {

	void addKey(MifareClassicKey key);
	
	void removeKey(MifareClassicKey key);
}
