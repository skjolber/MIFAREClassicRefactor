package com.skjolberg.nfc.clone.mifareclassic.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;


public class MifareClassicTag {

	private static final String TAG = MifareClassicTag.class.getName();

	private static final int TYPE = 1;
	private static final int VERSION = 1;
	
	private boolean compressed = false;
	
	private List<MifareClassicSectorData> sectors = new ArrayList<MifareClassicSectorData>();

	public List<MifareClassicSectorData> getSectors() {
		return sectors;
	}

	public void setSectors(List<MifareClassicSectorData> sectors) {
		this.sectors = sectors;
	}
	
	public void read(DataInputStream din) throws IOException {
		int type = din.readInt();
		int version = din.readInt();
		if(type == TYPE && version == VERSION) {
			int count = din.readInt();
			Log.d(TAG, "Read " + count + " sectors");
			for(int i = 0; i < count; i++) {
				MifareClassicSectorData sector = new MifareClassicSectorData();
				sector.read(din);
				sectors.add(sector);
				
				Log.d(TAG, "Read sector " + i);
			}
			
			compressed = din.readInt() == 1;
		} else {
			throw new IllegalArgumentException("Unexpected type " + type + " version " + version);
		}
	}
	
	public void write(DataOutputStream dout) throws IOException {
		dout.writeInt(TYPE);
		dout.writeInt(VERSION);
		
		Log.d(TAG, "Write " + sectors.size() + " sectors");

		dout.writeInt(sectors.size());
		for(MifareClassicSectorData sector : sectors) {
			sector.write(dout);
		}
		
		dout.writeInt(compressed ? 1 : 0);
	}
	
	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		DataOutputStream dout = new DataOutputStream(bout);
		try {
			write(dout);
		} finally {
			dout.close();
		}
		
		return bout.toByteArray();
	}
	
	public static MifareClassicTag fromByteArray(byte[] bytes) throws IOException {
		DataInputStream din = new DataInputStream(new ByteArrayInputStream(bytes));
		
		MifareClassicTag mifareClassicDataTag = new MifareClassicTag();
		
		mifareClassicDataTag.read(din);
		
		return mifareClassicDataTag;
	}

	public void add(MifareClassicSectorData mifareClassicSectorData) {
		if(mifareClassicSectorData.getIndex() == -1) throw new RuntimeException();
		
		this.sectors.add(mifareClassicSectorData);
	}
	
	public boolean requiresKeyA() {
		for(MifareClassicSectorData sector : sectors) {
			if(!sector.hasTrailerBlockKeyA()) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean requiresKeyB() {
		for(MifareClassicSectorData sector : sectors) {
			if(!sector.hasTrailerBlockKeyB()) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isComplete() {
		for(MifareClassicSectorData sector : sectors) {
			if(!sector.isComplete()) {
				return false;
			}
		}
		
		return true;
	}

	public boolean isPermanentTrailerAccesBytesTrailer() {
		for(MifareClassicSectorData sector : sectors) {
			if(sector.isPermanentTrailerAccessConditions()) {
				Log.d(TAG, "Data sector " + sector.getIndex() + " has permanent trailer access conditions");
				return true;
			}
		}
		
		return false;
	}

	public List<MifareClassicSectorData> getIncompleteSectors() {
		List<MifareClassicSectorData> incomplete = new ArrayList<MifareClassicSectorData>();
		for(MifareClassicSectorData sector : sectors) {
			if(!sector.hasTrailerBlockKeyA() || !sector.hasTrailerBlockKeyB()) {
				incomplete.add(sector);
			}
		}
		
		return incomplete;
	}
	
	public void printMifare() {
		for(MifareClassicSectorData sector : sectors) {
			sector.printMifare();
		}
	}
	
	/*
	public List<MifareClassicSectorOutput> calcualteMismatch(MifareClassicScheme tag) {
		List<MifareClassicSectorOutput> list = new ArrayList<MifareClassicSectorOutput>();
		
		for(int i = 0; i < sectors.size(); i++) {
			MifareClassicSectorData mifareClassicSectorData = sectors.get(i);

			boolean keyA = false;
			if(!mifareClassicSectorData.hasTrailerBlockKeyA() && tag.getKeyA(i) == null) {
				Log.d(TAG, "Sector " + i + " cannot fill trailer block key A");
				
				keyA = true;
			}
			
			boolean keyB = false;
			if(!mifareClassicSectorData.hasTrailerBlockKeyB() && tag.getKeyB(i) == null) {
				Log.d(TAG, "Sector " + i + " cannot fill trailer block key B");
				
				keyB = false;
			}

			boolean accessConditionBits = false;
			if(!mifareClassicSectorData.hasTrailerBlockAccessConditions() && !tag.hasCompleteAccessConditionBits()) {
				Log.d(TAG, "Sector " + i + " cannot fill access condition bits");
				
				accessConditionBits = true;
			}
			
			if(keyA || keyB || accessConditionBits) {
				list.add(new MifareClassicSectorOutput(i, keyA, keyB, accessConditionBits));
			}
		}
		
		return list;
	}
	*/
	
	public boolean isCompressed() {
		return compressed;
	}

	public void setCompressed(boolean compressed) {
		this.compressed = compressed;
	}

	public MifareClassicSectorData get(int index) {
		return sectors.get(index);
	}
	
	public int getSectorCount() {
		return sectors.size();
	}

	public int getDataSize() {
		int size = 0;
		for(MifareClassicSectorData sector : sectors) {
			size += sector.getDataSize();
		}
		return size;
	}
}

