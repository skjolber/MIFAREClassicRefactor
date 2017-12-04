package com.skjolberg.nfc.refactor.mifareclassic;

public class MifareClassicSectorOutput {

	private int index;
	
	private boolean keyA;
	private boolean keyB;
	private boolean accessConditionBits;
	
	public MifareClassicSectorOutput(int index, boolean keyA, boolean keyB, boolean accessConditionBits) {
		super();
		this.index = index;
		this.keyA = keyA;
		this.keyB = keyB;
		this.accessConditionBits = accessConditionBits;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public boolean isKeyA() {
		return keyA;
	}
	public void setKeyA(boolean keyA) {
		this.keyA = keyA;
	}
	public boolean isKeyB() {
		return keyB;
	}
	public void setKeyB(boolean keyB) {
		this.keyB = keyB;
	}
	public boolean isAccessConditionBits() {
		return accessConditionBits;
	}
	public void setAccessConditionBits(boolean accessConditionBits) {
		this.accessConditionBits = accessConditionBits;
	}

	
	
}
