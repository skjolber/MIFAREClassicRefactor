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

public class MifareClassicSectorDiff {

	private boolean keyA;
	
	private boolean accessConditions;
	
	private boolean keyB;
	
	private int index;

	public boolean isKeyA() {
		return keyA;
	}

	public void setKeyA(boolean keyA) {
		this.keyA = keyA;
	}

	public boolean isAccessConditions() {
		return accessConditions;
	}

	public void setAccessConditions(boolean accessConditions) {
		this.accessConditions = accessConditions;
	}

	public boolean isKeyB() {
		return keyB;
	}

	public void setKeyB(boolean keyB) {
		this.keyB = keyB;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (accessConditions ? 1231 : 1237);
		result = prime * result + index;
		result = prime * result + (keyA ? 1231 : 1237);
		result = prime * result + (keyB ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MifareClassicSectorDiff other = (MifareClassicSectorDiff) obj;
		if (accessConditions != other.accessConditions)
			return false;
		if (index != other.index)
			return false;
		if (keyA != other.keyA)
			return false;
		if (keyB != other.keyB)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MifareClassicIncompatibility [keyA=" + keyA + ", accessConditions=" + accessConditions + ", keyB=" + keyB + ", index=" + index + "]";
	}
	
	
}
