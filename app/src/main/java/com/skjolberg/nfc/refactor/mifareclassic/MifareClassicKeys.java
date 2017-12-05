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
