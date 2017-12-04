package com.skjolberg.nfc.refactor.mifareclassic;

import java.util.Arrays;

import android.content.Context;
import android.nfc.tech.MifareClassic;
import android.widget.EditText;
import android.widget.TextView;

import com.skjolberg.nfc.mifareclassic.R;

public class MifareClassicUI {

	private Context context;
	
	public MifareClassicUI(Context context) {
		this.context = context;
	}
	
	public boolean setValueErrorMessage(final EditText editText) {
		if(editText.getText().length() > 0) {
			CharSequence wepPasswordErrorMessage = getMifareKeyErrorMessage(editText.getText().toString().replaceAll("\\s",""));

			editText.setError(wepPasswordErrorMessage);
			
			return false;
		} else {
			editText.setError(null);
			
			return true;
		}
	}

	public CharSequence getMifareKeyErrorMessage(String string) {
		if(!MifareClassicKey.isHex(string.toString())) {
			return context.getText(R.string.mifareKeyIllegalCharacters);
		} 
		
		if(string.length() < 12) {
			return context.getText(R.string.mifareKeyIllegalLength);
		} else if(string.length() > 12) {
			return context.getText(R.string.mifareKeyIllegalLength);
		}
		return null;
	}

	public CharSequence getMifareAccessBitsErrorMessage(String string) {
		if(!MifareClassicUtils.isBinary(string.toString())) {
			return context.getText(R.string.accessBitsIllegalCharacters);
		} 
		
		if(string.length() < 3) {
			return context.getText(R.string.accessBitsIllegalLength);
		} else if(string.length() > 3) {
			return context.getText(R.string.accessBitsIllegalLength);
		}
		return null;
	}

	public boolean setAccessBitsErrorMessage(final EditText editText) {
		if(editText.getText().length() > 0) {
			CharSequence errorMessage = getMifareAccessBitsErrorMessage(editText.getText().toString().replaceAll("\\s",""));

			editText.setError(errorMessage);
			
			return false;
		} else {
			editText.setError(null);
			
			return true;
		}
	}	
	
	public static void setKeyText(TextView value, byte[] mifareClassicKey) {
		if(Arrays.equals(mifareClassicKey, MifareClassic.KEY_DEFAULT)) {
			value.setText(R.string.mifareClassicKeyDefault);
		} else if(Arrays.equals(mifareClassicKey, MifareClassic.KEY_NFC_FORUM)) {
			value.setText(R.string.mifareClassicKeyNfcForumDefault);
		} else if(Arrays.equals(mifareClassicKey, MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY)) {
			value.setText(R.string.mifareClassicKeyMAD);
		} else {
			value.setText(Utils.getHexString(mifareClassicKey));
		}
	}
}
