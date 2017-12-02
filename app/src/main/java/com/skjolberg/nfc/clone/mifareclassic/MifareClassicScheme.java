package com.skjolberg.nfc.clone.mifareclassic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class MifareClassicScheme<A extends Parcelable> extends MifareClassicKeys<A> implements Comparable<MifareClassicScheme<A>>{

	private static final String TAG = MifareClassicScheme.class.getName();
	
	private List<MifareClassicSector<A>> sectors = new ArrayList<MifareClassicSector<A>>();

	private int accessBitsDataIndex0 = -1;
	private int accessBitsDataIndex1 = -1;
	private int accessBitsDataIndex2 = -1;
	private int accessBitsTrailerIndex = -1;
	
	private String name;

	private long id;

	private Date time;

	public boolean hasKeyA(int sector) {
		if(aKey != null) {
			return true;
		}
		for(MifareClassicSector<A> key : sectors) {
			if(key.getIndex() == sector) {
				return key.hasAKey();
			}
		}
		return false;
	}

	public boolean hasKeyB(int sector) {
		if(bKey != null) {
			return true;
		}
		for(MifareClassicSector<A> key : sectors) {
			if(key.getIndex() == sector) {
				return key.hasBKey();
			}
		}
		return false;
	}

	public A getKeyA(int sector) {
		for(MifareClassicSector<A> key : sectors) {
			if(key.getIndex() == sector) {
				if(key.hasKeyA()) {
					return key.getAKey();
				}
			}
		}
		return aKey;
	}

	public A getKeyB(int sector) {
		for(MifareClassicSector<A> key : sectors) {
			if(key.getIndex() == sector) {
				if(key.hasKeyB()) {
					return key.getBKey();
				}
			}
		}
		return bKey;
	}

	public boolean hasName() {
		return name != null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<MifareClassicSector<A>> getSectors() {
		return sectors;
	}

	public void setSectors(List<MifareClassicSector<A>> sectors) {
		this.sectors = sectors;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	@Override
	public int compareTo(MifareClassicScheme another) {
		return -time.compareTo(another.getTime());
	}


	public int describeContents(){
		return 0;
	}

	public boolean isEmpty() {
		if(hasKeyA()) {
			return false;
		}
		if(hasKeyB()) {
			return false;
		}
		
		for(MifareClassicSector<A> key : sectors) {
			if(key.hasKeyA()) {
				return false;
			}
			if(key.hasKeyB()) {
				return false;
			}
			if(key.hasAccessConditionBits()) {
				return false;
			}
		}
		
		return true;
	}
	
	/*
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeLong(time.getTime());

		if(hasKeyA()) {
			dest.writeLong(aKey.getId());
		} else {
			dest.writeLong(-1L);
		}
		if(hasKeyB()) {
			dest.writeLong(bKey.getId());
		} else {
			dest.writeLong(-1L);
		}

		dest.writeInt(accessBitsDataIndex0);
		dest.writeInt(accessBitsDataIndex1);
		dest.writeInt(accessBitsDataIndex2);
		dest.writeInt(accessBitsTrailerIndex);
		
		dest.writeInt(sectors.size());
		for(MifareClassicSector key : sectors) {
			dest.writeLong(key.getId());
			dest.writeInt(key.getIndex());
			
			if(key.hasKeyA()) {
				dest.writeLong(key.getAKey().getId());
			} else {
				dest.writeLong(-1L);
			}
			if(key.hasKeyB()) {
				dest.writeLong(key.getBKey().getId());
			} else {
				dest.writeLong(-1L);
			}

			dest.writeInt(key.getAccessBitsDataIndex0());
			dest.writeInt(key.getAccessBitsDataIndex1());
			dest.writeInt(key.getAccessBitsDataIndex2());
			dest.writeInt(key.getAccessBitsTrailerIndex());
		}
	}
	*/

	public boolean isPermanentDataIndex() {
		if(MifareClassicUtils.isPermanentDataIndex(accessBitsDataIndex0) || MifareClassicUtils.isPermanentDataIndex(accessBitsDataIndex1) || MifareClassicUtils.isPermanentDataIndex(accessBitsDataIndex2)) {
			Log.d(TAG, "Default data access bits is permanent");
			return true;
		}

		for(MifareClassicSector key : sectors) {
			if(key.isPermanentDataIndex()) {
				Log.d(TAG, "Sector " + key.getIndex() + " has permanent data access bits");
				return true;
			}
		}

		return false;
	}
	
	public boolean isPermanentTrailerIndex() {
		if(MifareClassicUtils.isPermanentTrailerIndex(accessBitsTrailerIndex)) {
			Log.d(TAG, "Default trailer access bits is permanent");

			return true;
		}

		for(MifareClassicSector key : sectors) {
			if(key.isPermanentTrailerIndex()) {
				Log.d(TAG, "Sector " + key.getIndex() + " has permanent trailer access bits");

				return true;
			}
		}

		return false;
	}

	/*
	private void writeByteArray(Parcel dest, byte[] a) {
		if(a != null) {
			dest.writeInt(a.length);
			dest.writeByteArray(a);
		} else {
			dest.writeInt(0);
		}
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public MifareClassicScheme createFromParcel(Parcel in) {

			MifareClassicScheme tag = new MifareClassicScheme();

			tag.setId(in.readLong());
			tag.setName(in.readString());
			tag.setTime(new Date(in.readLong()));

			tag.setAKey(readByteArray(in));
			tag.setBKey(readByteArray(in));
			
			tag.accessBitsDataIndex0 = in.readInt();
			tag.accessBitsDataIndex1 = in.readInt();
			tag.accessBitsDataIndex2 = in.readInt();
			tag.accessBitsTrailerIndex = in.readInt();
			
			int count = in.readInt();
			for(int i = 0; i < count; i++) {
				MifareClassicSector mifareClassicSector = new MifareClassicSector();
				mifareClassicSector.setId(in.readLong());
				mifareClassicSector.setIndex(in.readInt());
				
				mifareClassicSector.setAKey(readByteArray(in));
				mifareClassicSector.setBKey(readByteArray(in));

				tag.setAccessBitsDataIndex0(in.readInt());
				tag.setAccessBitsDataIndex1(in.readInt());
				tag.setAccessBitsDataIndex2(in.readInt());
				tag.setAccessBitsTrailerIndex(in.readInt());

				mifareClassicSector.setScheme(tag);
				
				tag.add(mifareClassicSector);
			}
			
			return tag;
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

		public MifareClassicScheme[] newArray(int size) {
			return new MifareClassicScheme[size];
		}
	};
*/
	
	public MifareClassicSector<A> getSector(int index) {
		return sectors.get(index);
	}

	public void add(MifareClassicSector<A> sector) {
		sectors.add(sector);
	}

	public void remove(MifareClassicSector<A> sector) {
		sectors.remove(sector);
	}

	public void clearSectors() {
		sectors.clear();
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

	public boolean hasAccessConditions(int sector) {
		for(MifareClassicSector key : sectors) {
			if(key.getIndex() == sector) {
				if(key.hasAccessConditionBits()) {
					return true;
				} else {
					break;
				}
			}
		}
		return hasAccessConditionBits();
	}

	private boolean hasAccessConditionBits() {
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

	public byte[] getTrailerBlockAccessConditions(int sector) {
		
		int accessBitsDataIndex0 = -1;
		int accessBitsDataIndex1 = -1;
		int accessBitsDataIndex2  = -1;
		int accessBitsTrailerIndex = -1;

		if(hasAccessBitsDataIndex0()) {
			accessBitsDataIndex0 = getAccessBitsDataIndex0();
			
			Log.d(TAG, "Default data access bits for 0 for sector " + sector);
		} else if(hasAccessBitsDataIndex1()) {
			accessBitsDataIndex1 = getAccessBitsDataIndex1();
			
			Log.d(TAG, "Default data access bits for 1 for sector " + sector);
		} else if(hasAccessBitsDataIndex2()) {
			accessBitsDataIndex2 = getAccessBitsDataIndex2();
			
			Log.d(TAG, "Default data access bits for 2 for sector " + sector);
		} else if(hasAccessBitsTrailerIndex()) {
			accessBitsTrailerIndex = getAccessBitsTrailerIndex();
			
			Log.d(TAG, "Default trailer access bits for sector " + sector);
		}

		for(MifareClassicSector key : sectors) {
			if(key.getIndex() == sector) {
				if(key.hasAccessBitsDataIndex0()) {
					accessBitsDataIndex0 = key.getAccessBitsDataIndex0();
					
					Log.d(TAG, "Specific data access bits for 0 for sector " + sector);
				} else if(key.hasAccessBitsDataIndex1()) {
					accessBitsDataIndex1 = key.getAccessBitsDataIndex1();
					
					Log.d(TAG, "Specific data access bits for 1 for sector " + sector);
				} else if(key.hasAccessBitsDataIndex2()) {
					accessBitsDataIndex2 = key.getAccessBitsDataIndex2();
					
					Log.d(TAG, "Specific data access bits for 2 for sector " + sector);
				} else if(key.hasAccessBitsTrailerIndex()) {
					accessBitsTrailerIndex = key.getAccessBitsTrailerIndex();
					
					Log.d(TAG, "Specific trailer access bits for sector " + sector);
				}
				break;
			}
		}
		return MifareClassicUtils.fromAccessBytesIndexesToAccessConditionBytes(accessBitsTrailerIndex, accessBitsDataIndex0, accessBitsDataIndex1, accessBitsDataIndex2);
	}

	public byte[] getTrailerBlockAccessConditions(int sector, byte[] accessConditions) {
		
		byte[][] acBits = MifareClassicUtils.acToACMatrix(accessConditions);
		
		int accessBitsDataIndex0 = MifareClassicUtils.getIndex(acBits, 0);
		int accessBitsDataIndex1 = MifareClassicUtils.getIndex(acBits, 1);
		int accessBitsDataIndex2  = MifareClassicUtils.getIndex(acBits, 2);
		int accessBitsTrailerIndex = MifareClassicUtils.getIndex(acBits, 3);

		if(hasAccessBitsDataIndex0()) {
			int next = getAccessBitsDataIndex0();
			if(next != accessBitsDataIndex0) {
				Log.d(TAG, "Default data access bits for 0 for sector " + sector);
				accessBitsDataIndex0 = next;
			}
		} else if(hasAccessBitsDataIndex1()) {
			int next = getAccessBitsDataIndex1();
			if(accessBitsDataIndex1 != next) {
				Log.d(TAG, "Default data access bits for 1 for sector " + sector);
				accessBitsDataIndex1 = next;
			}
		} else if(hasAccessBitsDataIndex2()) {
			int next = getAccessBitsDataIndex2();
			
			if(accessBitsDataIndex2 != next) {
				Log.d(TAG, "Default data access bits for 2 for sector " + sector);
				accessBitsDataIndex2 = next;
			}
		} else if(hasAccessBitsTrailerIndex()) {
			int next = getAccessBitsTrailerIndex();
			if(next != accessBitsTrailerIndex) {
				accessBitsTrailerIndex = next;
				Log.d(TAG, "Default trailer access bits for sector " + sector);
			}
		}

		for(MifareClassicSector key : sectors) {
			if(key.getIndex() == sector) {
				if(key.hasAccessBitsDataIndex0()) {
					int next = key.getAccessBitsDataIndex0();
					if(accessBitsDataIndex0 != next) {
						Log.d(TAG, "Specific data access bits for 0 for sector " + sector);
					
						accessBitsDataIndex0 = next;
					}
				} else if(key.hasAccessBitsDataIndex1()) {
					accessBitsDataIndex1 = key.getAccessBitsDataIndex1();
					
					Log.d(TAG, "Specific data access bits for 1 for sector " + sector);
				} else if(key.hasAccessBitsDataIndex2()) {
					accessBitsDataIndex2 = key.getAccessBitsDataIndex2();
					
					Log.d(TAG, "Specific data access bits for 2 for sector " + sector);
				} else if(key.hasAccessBitsTrailerIndex()) {
					accessBitsTrailerIndex = key.getAccessBitsTrailerIndex();
					
					Log.d(TAG, "Specific trailer access bits for sector " + sector);
				}
				break;
			}
		}
		return MifareClassicUtils.fromAccessBytesIndexesToAccessConditionBytes(accessBitsTrailerIndex, accessBitsDataIndex0, accessBitsDataIndex1, accessBitsDataIndex2);
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

	public boolean hasCompleteAccessConditionBits(int sector) {
		int accessBitsDataIndex0 = -1;
		int accessBitsDataIndex1 = -1;
		int accessBitsDataIndex2  = -1;
		int accessBitsTrailerIndex = -1;

		if(hasAccessBitsDataIndex0()) {
			accessBitsDataIndex0 = getAccessBitsDataIndex0();
		} else if(hasAccessBitsDataIndex1()) {
			accessBitsDataIndex1 = getAccessBitsDataIndex1();
		} else if(hasAccessBitsDataIndex2()) {
			accessBitsDataIndex2 = getAccessBitsDataIndex2();
		} else if(hasAccessBitsTrailerIndex()) {
			accessBitsTrailerIndex = getAccessBitsTrailerIndex();
		}

		for(MifareClassicSector key : sectors) {
			if(key.getIndex() == sector) {
				if(key.hasAccessBitsDataIndex0()) {
					accessBitsDataIndex0 = key.getAccessBitsDataIndex0();
				} else if(key.hasAccessBitsDataIndex1()) {
					accessBitsDataIndex1 = key.getAccessBitsDataIndex1();
				} else if(key.hasAccessBitsDataIndex2()) {
					accessBitsDataIndex2 = key.getAccessBitsDataIndex2();
				} else if(key.hasAccessBitsTrailerIndex()) {
					accessBitsTrailerIndex = key.getAccessBitsTrailerIndex();
				}
				break;
			}
		}

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

	public boolean isPermanentAccessConditionTrailer() {
		return isPermanentTrailerIndex();
	}

}

