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


public class MifareClassicSector<A extends Parcelable> extends MifareClassicKeys<A> implements Parcelable {

	private long id = -1L;
	
	private int index = -1;
	
	private int accessBitsDataIndex0 = -1;
	private int accessBitsDataIndex1 = -1;
	private int accessBitsDataIndex2 = -1;
	private int accessBitsTrailerIndex = -1;

	private MifareClassicScheme<A> tag;
	
	public MifareClassicSector() {
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public MifareClassicScheme getScheme() {
		return tag;
	}

	public void setScheme(MifareClassicScheme tag) {
		this.tag = tag;
	}

	public boolean hasAKey() {
		return aKey != null;
	}

	public boolean hasBKey() {
		return bKey != null;
	}

	public boolean hasIndex() {
		return index != -1;
	}
	
	public boolean hasCompleteAccessConditionBits() {
		
		if(accessBitsDataIndex0 == -1) {
			return false;
		}
		if(accessBitsDataIndex1 == -1) {
			return false;
		}
		if(accessBitsDataIndex2 == -1) {
			return false;
		}
		if(accessBitsTrailerIndex == -1) {
			return false;
		}
		
		return true;
	}


	public int getAccessBitsDataIndex0() {
		return accessBitsDataIndex0;
	}

	public void setAccessBitsDataIndex0(int accessBitsDataIndex0) {
		this.accessBitsDataIndex0 = accessBitsDataIndex0;
	}

	public int getAccessBitsDataIndex1() {
		return accessBitsDataIndex1;
	}

	public void setAccessBitsDataIndex1(int accessBitsDataIndex1) {
		this.accessBitsDataIndex1 = accessBitsDataIndex1;
	}

	public int getAccessBitsDataIndex2() {
		return accessBitsDataIndex2;
	}

	public void setAccessBitsDataIndex2(int accessBitsDataIndex2) {
		this.accessBitsDataIndex2 = accessBitsDataIndex2;
	}

	public int getAccessBitsTrailerIndex() {
		return accessBitsTrailerIndex;
	}

	public void setAccessBitsTrailerIndex(int accessBitsTrailerIndex) {
		this.accessBitsTrailerIndex = accessBitsTrailerIndex;
	}
	
	public int getAccessBitsIndex(int type) {

		switch(type) {
		case 0 : return accessBitsDataIndex0;
		case 1 : return accessBitsDataIndex1;
		case 2 : return accessBitsDataIndex2;
		case 3 : return accessBitsTrailerIndex;
		}
		
		throw new IllegalArgumentException();
	}
	
	public String getAccessBitsString(int type) {
		int index = getAccessBitsIndex(type);
		
		if(index == -1) {
			throw new IllegalArgumentException();
		}
		
		return MifareClassicUtils.getAccessBits(index);
	}

	public int describeContents(){
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeInt(index);
		
		if(aKey != null) {
			dest.writeInt(1);
			dest.writeParcelable(aKey, flags);
		} else {
			dest.writeInt(0);
		}
		if(bKey != null) {
			dest.writeInt(1);
			dest.writeParcelable(bKey, flags);
		} else {
			dest.writeInt(0);
		}

		dest.writeInt(accessBitsDataIndex0);
		dest.writeInt(accessBitsDataIndex1);
		dest.writeInt(accessBitsDataIndex2);
		dest.writeInt(accessBitsTrailerIndex);
	}

	private void writeByteArray(Parcel dest, byte[] a) {
		if(a != null) {
			dest.writeInt(a.length);
			dest.writeByteArray(a);
		} else {
			dest.writeInt(0);
		}
	}

	public static final Parcelable.Creator<MifareClassicSector<MifareClassicKey>> CREATOR = new Parcelable.Creator<MifareClassicSector<MifareClassicKey>>() {
		public MifareClassicSector<MifareClassicKey> createFromParcel(Parcel in) {

			MifareClassicSector<MifareClassicKey> sector = new MifareClassicSector<MifareClassicKey>();

			sector.setId(in.readLong());
			sector.setIndex(in.readInt());
			
			if(in.readInt() > 0) {
				sector.setAKey((MifareClassicKey)in.readParcelable(MifareClassicKey.class.getClassLoader()));
			}
			if(in.readInt() > 0) {
				sector.setBKey((MifareClassicKey)in.readParcelable(MifareClassicKey.class.getClassLoader()));
			}
			
			sector.accessBitsDataIndex0 = in.readInt();
			sector.accessBitsDataIndex1 = in.readInt();
			sector.accessBitsDataIndex2 = in.readInt();
			sector.accessBitsTrailerIndex = in.readInt();
			
			return sector;
		}

		private byte[] readByteArray(Parcel in) {
			int a = in.readInt();
			if(a > 0) {
				byte[] key = new byte[a];
				in.readByteArray(key);
				
				return key;
			}
			return null;
		}

		public MifareClassicSector[] newArray(int size) {
			return new MifareClassicSector[size];
		}
	};

	public void setAccessBits(int accessBitTypeIndex, int accessBitIndex) {
		switch(accessBitTypeIndex) {
		case 0: {
			this.accessBitsDataIndex0 = accessBitIndex;
			break;
		}
		case 1: {
			this.accessBitsDataIndex1 = accessBitIndex;
			break;
		}
		case 2: {
			this.accessBitsDataIndex2 = accessBitIndex;
			break;
		}
		case 3: {
			this.accessBitsTrailerIndex = accessBitIndex;
			break;
		}
		default: throw new IllegalArgumentException();
		}
	}

	public byte[] getTrailerBlockAccessConditions() {
		return MifareClassicUtils.toAccessBytes(accessBitsTrailerIndex, accessBitsDataIndex0, accessBitsDataIndex1, accessBitsDataIndex2);
	}

	public boolean hasAccessConditionBits() {
		if(accessBitsDataIndex0 != -1) {
			return true;
		}
		if(accessBitsDataIndex1 != -1) {
			return true;
		}
		if(accessBitsDataIndex2 != -1) {
			return true;
		}
		if(accessBitsTrailerIndex != -1) {
			return true;
		}
		
		return false;
	}

	public boolean hasAccessBitsDataIndex0() {
		return accessBitsDataIndex0 != -1;
	}
	
	public boolean hasAccessBitsDataIndex1() {
		return accessBitsDataIndex1 != -1;
	}
	
	public boolean hasAccessBitsDataIndex2() {
		return accessBitsDataIndex2 != -1;
	}

	public boolean hasAccessBitsTrailerIndex() {
		return accessBitsTrailerIndex != -1;
	}

	public boolean isPermanentDataIndex() {
		return MifareClassicUtils.isPermanentDataIndex(accessBitsDataIndex0) || MifareClassicUtils.isPermanentDataIndex(accessBitsDataIndex1) || MifareClassicUtils.isPermanentDataIndex(accessBitsDataIndex2);
	}

	public boolean isPermanentTrailerIndex() {
		return MifareClassicUtils.isPermanentTrailerIndex(accessBitsTrailerIndex);
	}


}
