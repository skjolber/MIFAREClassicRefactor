package com.skjolberg.nfc.refactor.mifareclassic;

import java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;



public class MifareClassicKey extends MifareClassicKeyId implements Comparable<MifareClassicKey>{

	public static boolean isHex(String key) {
    	for (int i = key.length() - 1; i >= 0; i--) {
    		final char c = key.charAt(i);
    		if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f')) {
    			return false;
    		}
    	}

    	return true;
    }
	
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	private String name;
	
	protected byte[] value;
	
	protected long time;
	
	public MifareClassicKey(byte[] value) {
		this.value = value;
	}

	public MifareClassicKey() {
	}

	public byte[] getValue() {
		return value;
	}
	
	public boolean hasValue() {
		return value != null;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	@Override
	public int compareTo(MifareClassicKey another) {
		return name.compareTo(another.getName());
	}

	public boolean hasName() {
		return name != null;
	}

	public int describeContents(){
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		
		dest.writeString(name);
		
		if(value != null) {
			dest.writeInt(value.length);
			dest.writeByteArray(value);
		} else {
			dest.writeInt(0);
		}
		dest.writeLong(time);
	}

	public static final Parcelable.Creator<MifareClassicKey> CREATOR = new Parcelable.Creator<MifareClassicKey>() {
		public MifareClassicKey createFromParcel(Parcel in) {

			MifareClassicKey item = new MifareClassicKey();

			item.readFromParcel(in);
			
			return item;
		}

		public MifareClassicKey[] newArray(int size) {
			return new MifareClassicKey[size];
		}
	};

	protected void readFromParcel(Parcel in) {
		super.readFromParcel(in);
		
		this.name = in.readString();
		
		int length = in.readInt();
		if(length > 0) {
			value = new byte[length];
			in.readByteArray(value);
		}
		time = in.readLong();
		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (int) (time ^ (time >>> 32));
		result = prime * result + Arrays.hashCode(value);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MifareClassicKey other = (MifareClassicKey) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (time != other.time)
			return false;
		if (!Arrays.equals(value, other.value))
			return false;
		return true;
	}
	
	
}
