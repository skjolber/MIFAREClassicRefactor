package com.skjolberg.nfc.mifareclassic.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;


public class MifareClassic {

	private static final String TAG = MifareClassic.class.getName();

	private static final int TYPE = 1;
	private static final int VERSION = 1;
	
	private boolean compressed = false;
	
	private List<MifareClassicSector> sectors = new ArrayList<MifareClassicSector>();

	public List<MifareClassicSector> getSectors() {
		return sectors;
	}

	public void setSectors(List<MifareClassicSector> sectors) {
		this.sectors = sectors;
	}
	
	public void read(DataInputStream din) throws IOException {
		int type = din.readInt();
		int version = din.readInt();
		if(type == TYPE && version == VERSION) {
			int count = din.readInt();
			Log.d(TAG, "Read " + count + " sectors");
			for(int i = 0; i < count; i++) {
				MifareClassicSector sector = new MifareClassicSector();
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
		for(MifareClassicSector sector : sectors) {
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
	
	public static MifareClassic fromByteArray(byte[] bytes) throws IOException {
		DataInputStream din = new DataInputStream(new ByteArrayInputStream(bytes));
		
		MifareClassic mifareClassicDataTag = new MifareClassic();
		
		mifareClassicDataTag.read(din);
		
		return mifareClassicDataTag;
	}

	public void add(MifareClassicSector mifareClassicSectorData) {
		if(mifareClassicSectorData.getIndex() == -1) throw new RuntimeException();
		
		this.sectors.add(mifareClassicSectorData);
	}
	
	public boolean requiresKeyA() {
		for(MifareClassicSector sector : sectors) {
			if(!sector.hasTrailerBlockKeyA()) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean requiresKeyB() {
		for(MifareClassicSector sector : sectors) {
			if(!sector.hasTrailerBlockKeyB()) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isComplete() {
		for(MifareClassicSector sector : sectors) {
			if(!sector.isComplete()) {
				return false;
			}
		}
		
		return true;
	}

	public boolean isPermanentTrailerAccesBytesTrailer() {
		for(MifareClassicSector sector : sectors) {
			if(sector.isPermanentTrailerAccessConditions()) {
				Log.d(TAG, "Data sector " + sector.getIndex() + " has permanent trailer access conditions");
				return true;
			}
		}
		
		return false;
	}

	public List<MifareClassicSector> getIncompleteSectors() {
		List<MifareClassicSector> incomplete = new ArrayList<MifareClassicSector>();
		for(MifareClassicSector sector : sectors) {
			if(!sector.hasTrailerBlockKeyA() || !sector.hasTrailerBlockKeyB()) {
				incomplete.add(sector);
			}
		}
		
		return incomplete;
	}
	
	public void printMifare() {
		for(MifareClassicSector sector : sectors) {
			sector.printMifare();
		}
	}
	
	public boolean isCompressed() {
		return compressed;
	}

	public void setCompressed(boolean compressed) {
		this.compressed = compressed;
	}

	public MifareClassicSector get(int index) {
		return sectors.get(index);
	}
	
	public int getSectorCount() {
		return sectors.size();
	}

	public int getDataSize() {
		int size = 0;
		for(MifareClassicSector sector : sectors) {
			size += sector.getDataSize();
		}
		return size;
	}
}

