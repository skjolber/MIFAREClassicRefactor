package com.skjolberg.nfc.refactor.mifareclassic.ac;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum MifareClassicDataAccessCondition {

	AccessBits000_READ(0, 0, 0, MifareClassicDataOperation.READ, true, true),
	AccessBits010_READ(0, 1, 0, MifareClassicDataOperation.READ, true, true),
	AccessBits100_READ(1, 0, 0, MifareClassicDataOperation.READ, true, true),
	AccessBits110_READ(1, 1, 0, MifareClassicDataOperation.READ, true, true),
	AccessBits001_READ(0, 0, 1, MifareClassicDataOperation.READ, true, true),
	AccessBits011_READ(0, 1, 1, MifareClassicDataOperation.READ, false, true),
	AccessBits101_READ(1, 0, 1, MifareClassicDataOperation.READ, false, true),
	
	AccessBits000_WRITE(0, 0, 0, MifareClassicDataOperation.WRITE, true, true),
	AccessBits100_WRITE(1, 0, 0, MifareClassicDataOperation.WRITE, false, true),
	AccessBits110_WRITE(1, 1, 0, MifareClassicDataOperation.WRITE, false, true),
	AccessBits011_WRITE(0, 1, 1, MifareClassicDataOperation.WRITE, false, true),
	
	AccessBits000_INCREMENT(0, 0, 0, MifareClassicDataOperation.INCREMENT, true, true),
	AccessBits110_INCREMENT(1, 1, 0, MifareClassicDataOperation.INCREMENT, false, true),
	
	AccessBits000_DECREMENT_TRANSFER_RESTORE(0, 0, 0, MifareClassicDataOperation.DECREMENT_TRANSFER_RESTORE, true, true),
	AccessBits110_DECREMENT_TRANSFER_RESTORE(1, 1, 0, MifareClassicDataOperation.DECREMENT_TRANSFER_RESTORE, true, true),
	AccessBits001_DECREMENT_TRANSFER_RESTORE(0, 0, 1, MifareClassicDataOperation.DECREMENT_TRANSFER_RESTORE, true, true),
	;

	private final int c1;
	private final int c2;
	private final int c3;
	private final MifareClassicDataOperation operation;
	private final boolean keyA;
	private final boolean keyB;

	private MifareClassicDataAccessCondition(int c1, int c2, int c3, MifareClassicDataOperation operation, boolean keyA, boolean keyB) {
		this.c1 = c1;
		this.c2 = c2;
		this.c3 = c3;
		this.operation = operation;
		this.keyA = keyA;
		this.keyB = keyB;
	}
	
	public int getC1() {
		return c1;
	}
	public int getC2() {
		return c2;
	}
	public int getC3() {
		return c3;
	}
	public MifareClassicDataOperation getOperation() {
		return operation;
	}
	public boolean isKeyA() {
		return keyA;
	}
	public boolean isKeyB() {
		return keyB;
	}
	
	public static List<MifareClassicDataAccessCondition> getOperations(int c1, int c2, int c3) {
		List<MifareClassicDataAccessCondition> operations = new ArrayList<MifareClassicDataAccessCondition>();
		
		for(MifareClassicDataAccessCondition value : values()) {
			if(value.is(c1, c2, c3)) {
				operations.add(value);
			}
		}
		
		return operations;
	}
	
	public boolean is(int c1, int c2, int c3) {
		return this.c1 == c1 && this.c2 == c2 && this.c3 == c3;
	}
	
	public static Set<MifareClassicDataOperation> getOperations(boolean keyA, boolean keyB) {
		Set<MifareClassicDataOperation> operations = new HashSet<MifareClassicDataOperation>();
		
		for(MifareClassicDataAccessCondition value : values()) {
			if(value.isKeyA() && keyA || value.isKeyB() && keyB) {
				operations.add(value.getOperation());
			}
		}
		
		return operations;
	}

}
