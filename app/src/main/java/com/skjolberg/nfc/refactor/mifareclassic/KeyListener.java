package com.skjolberg.nfc.refactor.mifareclassic;

public interface KeyListener {

	void addKey(MifareClassicKey key);
	
	void removeKey(MifareClassicKey key);
}
