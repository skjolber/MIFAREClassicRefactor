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

import android.os.Parcel;
import android.os.Parcelable;



public class MifareClassicKeyId implements Parcelable{

	protected long id = -1L;
	
	public MifareClassicKeyId(long id) {
		this.id = id;
	}

	public MifareClassicKeyId() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

    /**
     * Compares two {@code long} values.
     * @return 0 if lhs = rhs, less than 0 if lhs &lt; rhs, and greater than 0 if lhs &gt; rhs.
     * @since 1.7
     */
    public static int compare(long lhs, long rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		MifareClassicKeyId other = (MifareClassicKeyId) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public int describeContents(){
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
	}

	public static final Parcelable.Creator<MifareClassicKeyId> CREATOR = new Parcelable.Creator<MifareClassicKeyId>() {
		public MifareClassicKeyId createFromParcel(Parcel in) {

			MifareClassicKeyId item = new MifareClassicKeyId();

			item.readFromParcel(in);
			
			return item;
		}

		public MifareClassicKeyId[] newArray(int size) {
			return new MifareClassicKeyId[size];
		}
	};

	protected void readFromParcel(Parcel in) {
		this.id = in.readLong();
	}

    
}
