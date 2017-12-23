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
package com.skjolberg.nfc.refactor;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class GalleryTag implements Parcelable, Comparable<GalleryTag>{

	public static final String TYPE_MIFARE_CLASSIC = "Mifare Classic";
	public static final String TYPE_MIFARE_ULTRALIGHT = "Mifare Ultralight";
	
	private long id;
	private String tagId;
	private String name;
	private Date date;
	private String type;
	private byte[] bytes;
	private int capacity;
	private int size;
	private boolean ndef;
	private int resource;
	private String tech;
	
	private long crc;
	
	public boolean isNdef() {
		return ndef;
	}
	public void setNdef(boolean ndef) {
		this.ndef = ndef;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTagId() {
		return tagId;
	}
	public void setTagId(String tagId) {
		this.tagId = tagId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public byte[] getBytes() {
		return bytes;
	}
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	
     public int getResource() {
		return resource;
	}
	public void setResource(int resource) {
		this.resource = resource;
	}
	public int describeContents(){
         return 0;
     }
	
     public long getCrc() {
		return crc;
	}
	public void setCrc(long crc) {
		this.crc = crc;
	}
	
	public String getTech() {
		return tech;
	}
	
	public void setTech(String tech) {
		this.tech = tech;
	}
	
	@Override
     public void writeToParcel(Parcel dest, int flags) {
    	 dest.writeLong(id);
    	 dest.writeString(tagId);
    	 dest.writeString(name);
    	 dest.writeLong(date.getTime());
    	 dest.writeString(type);
    	 dest.writeInt(bytes.length);
    	 dest.writeByteArray(bytes);
    	 dest.writeInt(capacity);
    	 dest.writeInt(size);
    	 dest.writeInt(ndef ? 1 : 0);
    	 dest.writeInt(resource);
    	 dest.writeLong(crc);
    	 
    	 if(tech != null) {
    		 dest.writeInt(1);
        	 dest.writeString(tech);
    	 } else {
    		 dest.writeInt(0);
    	 }
     }
     
     public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
         public GalleryTag createFromParcel(Parcel in) {
        	 
        	 GalleryTag galleryTag = new GalleryTag();
        	 
        	 galleryTag.setId(in.readLong());
        	 galleryTag.setTagId(in.readString());
        	 galleryTag.setName(in.readString());
        	 galleryTag.setDate(new Date(in.readLong()));
        	 galleryTag.setType(in.readString());
        	 
        	 byte[] bytes = new byte[in.readInt()];
        	 in.readByteArray(bytes);
        	 galleryTag.setBytes(bytes);
        	 
        	 galleryTag.setCapacity(in.readInt());
        	 galleryTag.setSize(in.readInt());
        	 
        	 galleryTag.setNdef(in.readInt() == 1);

        	 galleryTag.setResource(in.readInt());

        	 galleryTag.setCrc(in.readLong());

        	 if(in.dataAvail() > 0) {
        		 if(in.readInt() == 1) {
        			 galleryTag.setTech(in.readString());
        		 }
        	 }
        	 
             return galleryTag;
         }

         public GalleryTag[] newArray(int size) {
             return new GalleryTag[size];
         }
     };

	@Override
	public int compareTo(GalleryTag another) {
		return -date.compareTo(another.getDate());
	}
	
	public boolean isMifareClassic() {
		return type.equals(TYPE_MIFARE_CLASSIC);
	}
	
	public boolean isMifareUltralight() {
		return type.equals(TYPE_MIFARE_ULTRALIGHT);
	}
	
}
