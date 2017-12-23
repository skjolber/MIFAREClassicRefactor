/***************************************************************************
 *
 * This file is part of the 'NDEF Tools for Android' project at
 * http://code.google.com/p/ndef-tools-for-android/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ****************************************************************************/
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
