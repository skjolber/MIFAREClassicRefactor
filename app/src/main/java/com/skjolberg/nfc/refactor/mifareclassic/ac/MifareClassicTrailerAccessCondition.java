package com.skjolberg.nfc.refactor.mifareclassic.ac;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum MifareClassicTrailerAccessCondition {

	AccessBits000_WRITE_KEY_A(0, 0, 0, MifareClassicTrailerOperation.WRITE_KEY_A, true, false),
	AccessBits100_WRITE_KEY_A(1, 0, 0, MifareClassicTrailerOperation.WRITE_KEY_A, false, true),
	AccessBits001_WRITE_KEY_A(0, 0, 1, MifareClassicTrailerOperation.WRITE_KEY_A, true, false),
	AccessBits011_WRITE_KEY_A(0, 1, 1, MifareClassicTrailerOperation.WRITE_KEY_A, false, true),

	AccessBits001_WRITE_ACCESS_BITS(0, 0, 1, MifareClassicTrailerOperation.WRITE_ACCESS_BITS, true, false),
	AccessBits011_WRITE_ACCESS_BITS(0, 1, 1, MifareClassicTrailerOperation.WRITE_ACCESS_BITS, false, true),
	AccessBits101_WRITE_ACCESS_BITS(1, 0, 1, MifareClassicTrailerOperation.WRITE_ACCESS_BITS, false, true),

	AccessBits000_WRITE_KEY_B(0, 0, 0, MifareClassicTrailerOperation.WRITE_KEY_B, true, false),
	AccessBits100_WRITE_KEY_B(1, 0, 0, MifareClassicTrailerOperation.WRITE_KEY_B, false, true),
	AccessBits001_WRITE_KEY_B(0, 0, 1, MifareClassicTrailerOperation.WRITE_KEY_B, true, false),
	AccessBits011_WRITE_KEY_B(0, 1, 1, MifareClassicTrailerOperation.WRITE_KEY_B, false, true),
	
	AccessBits000_READ_ACCESS_BITS(0, 0, 0, MifareClassicTrailerOperation.READ_ACCESS_BITS, true, false),
	AccessBits010_READ_ACCESS_BITS(0, 1, 0, MifareClassicTrailerOperation.READ_ACCESS_BITS, true, false),
	AccessBits100_READ_ACCESS_BITS(1, 0, 0, MifareClassicTrailerOperation.READ_ACCESS_BITS, true, true),
	AccessBits110_READ_ACCESS_BITS(1, 1, 0, MifareClassicTrailerOperation.READ_ACCESS_BITS, true, true),
	AccessBits001_READ_ACCESS_BITS(0, 0, 1, MifareClassicTrailerOperation.READ_ACCESS_BITS, true, false),
	AccessBits011_READ_ACCESS_BITS(0, 1, 1, MifareClassicTrailerOperation.READ_ACCESS_BITS, true, true),
	AccessBits101_READ_ACCESS_BITS(1, 0, 1, MifareClassicTrailerOperation.READ_ACCESS_BITS, true, true),
	AccessBits111_READ_ACCESS_BITS(1, 1, 1, MifareClassicTrailerOperation.READ_ACCESS_BITS, true, true),

	AccessBits000_READ_KEY_B(0, 0, 0, MifareClassicTrailerOperation.READ_KEY_B, true, false),
	AccessBits010_READ_KEY_B(0, 1, 0, MifareClassicTrailerOperation.READ_KEY_B, true, false),
	AccessBits001_READ_KEY_B(0, 0, 1, MifareClassicTrailerOperation.READ_KEY_B, true, false),
	
	;
	
	private MifareClassicTrailerAccessCondition(int c1, int c2, int c3, MifareClassicTrailerOperation operation, boolean keyA, boolean keyB) {
		this.c1 = c1;
		this.c2 = c2;
		this.c3 = c3;
		this.operation = operation;
		this.keyA = keyA;
		this.keyB = keyB;
	}
	private final int c1;
	private final int c2;
	private final int c3;
	
	private final MifareClassicTrailerOperation operation;
	
	private final boolean keyA;
	private final boolean keyB;
	
	public int getC1() {
		return c1;
	}
	public int getC2() {
		return c2;
	}
	public int getC3() {
		return c3;
	}
	public MifareClassicTrailerOperation getOperation() {
		return operation;
	}
	public boolean isKeyA() {
		return keyA;
	}
	public boolean isKeyB() {
		return keyB;
	}
	
	public static List<MifareClassicTrailerAccessCondition> getOperations(int c1, int c2, int c3) {
		List<MifareClassicTrailerAccessCondition> operations = new ArrayList<MifareClassicTrailerAccessCondition>();
		
		for(MifareClassicTrailerAccessCondition value : values()) {
			if(value.is(c1, c2, c3)) {
				operations.add(value);
			}
		}
		
		return operations;
	}

	public static Set<MifareClassicTrailerOperation> getOperations(boolean keyA, boolean keyB) {
		Set<MifareClassicTrailerOperation> operations = new HashSet<MifareClassicTrailerOperation>();
		
		for(MifareClassicTrailerAccessCondition value : values()) {
			if(value.isKeyA() && keyA || value.isKeyB() && keyB) {
				operations.add(value.getOperation());
			}
		}
		
		return operations;
	}

	public boolean is(int c1, int c2, int c3) {
		return this.c1 == c1 && this.c2 == c2 && this.c3 == c3;
	}
}
