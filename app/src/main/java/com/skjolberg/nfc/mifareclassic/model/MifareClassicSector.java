package com.skjolberg.nfc.mifareclassic.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.util.Log;

import com.skjolberg.nfc.clone.desfire.Utils;
import com.skjolberg.nfc.clone.mifareclassic.MifareClassicUtils;

public class MifareClassicSector {
	
	private static final String TAG = MifareClassicSector.class.getName();

    public static String getHexString (byte[] a) {
		StringBuilder sb = new StringBuilder();
		for (byte b : a) {
			sb.append(String.format("%02x ", b & 0xff));
		}
		return sb.toString().trim().toUpperCase();
    }

	private int index = -1;

	private byte[] trailerBlockKeyA;
	private byte[] trailerBlockAccessConditions;
	private byte[] trailerBlockKeyB;

	private List<byte[]> blocks = new ArrayList<byte[]>();

	public byte[] getTrailerBlockKeyA() {
		return trailerBlockKeyA;
	}

	public void setTrailerBlockKeyA(byte[] trailerBlockKeyA) {
		this.trailerBlockKeyA = trailerBlockKeyA;
	}

	public byte[] getTrailerBlockKeyB() {
		return trailerBlockKeyB;
	}

	public void setTrailerBlockKeyB(byte[] trailerBlockKeyB) {
		this.trailerBlockKeyB = trailerBlockKeyB;
	}

	public byte[] getTrailerBlockAccessConditions() {
		return trailerBlockAccessConditions;
	}

	public void setTrailerBlockAccessConditions(byte[] trailerBlockAccessConditions) {
		this.trailerBlockAccessConditions = trailerBlockAccessConditions;
	}

	public List<byte[]> getBlocks() {
		return blocks;
	}

	public void setBlocks(List<byte[]> blocks) {
		this.blocks = blocks;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getSize() {
		int size = 4 + 4;
		
		size += 4;
		if(trailerBlockKeyA != null) {
			size += trailerBlockKeyA.length;
		}

		size += 4;
		if(trailerBlockAccessConditions != null) {
			size += trailerBlockAccessConditions.length;
		}

		size += 4;
		if(trailerBlockKeyB != null) {
			size += trailerBlockKeyB.length;
		}

		size += 4;
		for(byte[] block : blocks) {
			size += 4;
			
			size += block.length;
		}
		
		return size;
	}
	
	public void read(DataInputStream din) throws IOException {
		int size = din.readInt();
		
		din.mark(size - 4);

		index = din.readInt();
		
		int trailerBlockKeyACount = din.readInt();
		if(trailerBlockKeyACount > 0)  {
			trailerBlockKeyA = new byte[trailerBlockKeyACount];
			din.readFully(trailerBlockKeyA);
		}

		int trailerBlockAccessConditionsCount = din.readInt();
		if(trailerBlockAccessConditionsCount > 0)  {
			trailerBlockAccessConditions = new byte[trailerBlockAccessConditionsCount];
			din.readFully(trailerBlockAccessConditions);
		}

		int trailerBlockKeyBCount = din.readInt();
		if(trailerBlockKeyBCount > 0)  {
			trailerBlockKeyB = new byte[trailerBlockKeyBCount];
			din.readFully(trailerBlockKeyB);
		}

		int count = din.readInt();
		
		for(int i = 0; i < count; i++) {
			int bufferSize = din.readInt();
			byte[] buffer = new byte[bufferSize];
			din.readFully(buffer);
			
			blocks.add(buffer);
		}
		
		din.reset();
		din.skip(size - 4);
	}
	
	public void write(DataOutputStream dout) throws IOException {
		int size = getSize();
		
		dout.writeInt(size);
		dout.writeInt(index);

		if(trailerBlockKeyA != null) {
			dout.writeInt(trailerBlockKeyA.length);
			dout.write(trailerBlockKeyA);
		} else {
			dout.writeInt(0);
		}

		if(trailerBlockAccessConditions != null) {
			dout.writeInt(trailerBlockAccessConditions.length);
			dout.write(trailerBlockAccessConditions);
		} else {
			dout.writeInt(0);
		}

		if(trailerBlockKeyB != null) {
			dout.writeInt(trailerBlockKeyB.length);
			dout.write(trailerBlockKeyB);
		} else {
			dout.writeInt(0);
		}
		
		dout.writeInt(blocks.size());
		for(byte[] block : blocks) {
			dout.writeInt(block.length);
			dout.write(block);
		}

	}

	public void addBlock(byte[] block) {
		blocks.add(block);
	}

	public boolean hasTrailerBlockKeyA() {
		return trailerBlockKeyA != null;
	}

	public boolean hasTrailerBlockKeyB() {
		return trailerBlockKeyB != null;
	}

	public void printMifare() {
		for(byte[] block : blocks) {
			Log.d(TAG, getHexString(block));
		}
		
		StringBuffer buffer = new StringBuffer();
		
		if(trailerBlockKeyA != null) {
			buffer.append(getHexString(trailerBlockKeyA));
		} else {
			buffer.append("------------");
		}

		if(trailerBlockAccessConditions != null) {
			buffer.append(getHexString(trailerBlockAccessConditions));
		} else {
			buffer.append("--------");
		}

		if(trailerBlockKeyB != null) {
			buffer.append(getHexString(trailerBlockKeyB));
		} else {
			buffer.append("------------");
		}

		Log.d(TAG, buffer.toString());
	}
	

	public boolean hasTrailerBlockAccessConditions() {
		return trailerBlockAccessConditions != null;
	}
	
	public boolean isPermanentTrailerAccessConditions() {
		if(trailerBlockAccessConditions != null) {
			return MifareClassicUtils.isPermanentTrailerAccesBytes(trailerBlockAccessConditions);
		}
		
		return false;
	}

	public boolean isBlankData() {
		for(byte[] block : blocks) {
			for(int i = 0; i < block.length; i++) {
				if(block[i] != 0) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isEqualTrailer(MifareClassicSector data) {
		if(!Arrays.equals(trailerBlockKeyA, data.trailerBlockKeyA)) {
			Log.d(TAG, "Trailer block A do not match");
			return false;
		}
		if(!Arrays.equals(trailerBlockAccessConditions, data.trailerBlockAccessConditions)) {
			Log.d(TAG, "Trailer access conditions do not match");
			return false;
		}
		if(!Arrays.equals(trailerBlockKeyB, data.trailerBlockKeyB)) {
			Log.d(TAG, "Trailer block B do not match");
			return false;
		}
		return true;
	}
	
	public boolean isFill(MifareClassicSector data) {
		return isEqualTrailer(data) && data.isBlankData();
	}

	public boolean isComplete() {
		if(!hasTrailerBlockKeyA()) {
			return false;
		}
		if(!hasTrailerBlockKeyB()) {
			return false;
		}
		
		if(!hasTrailerAccessConditions()) {
			return false;
		}
		
		return true;
	}

	private boolean hasTrailerAccessConditions() {
		return trailerBlockAccessConditions != null;
	}

	public int blockCount() {
		return blocks.size() + 1;
	}
	
	public String getBlockString(int index) {
		if(index < blocks.size()) {
			return Utils.getHexString(blocks.get(index));
		}
		if(index == blocks.size()) {
			StringBuffer buffer = new StringBuffer();
			
			if(trailerBlockKeyA != null) {
				buffer.append(Utils.getHexString(trailerBlockKeyA));
				buffer.append(" ");
			} else {
				buffer.append("-- -- -- -- -- --");
				buffer.append(" ");
			}

			if(trailerBlockAccessConditions != null) {
				buffer.append(Utils.getHexString(trailerBlockAccessConditions));
				buffer.append(" ");
			} else {
				buffer.append("-- -- -- --");
				buffer.append(" ");
			}

			if(trailerBlockKeyB != null) {
				buffer.append(Utils.getHexString(trailerBlockKeyB));
				buffer.append(" ");
			} else {
				buffer.append("-- -- -- -- -- --");
				buffer.append(" ");
			}

			if(buffer.length() > 0) {
				buffer.setLength(buffer.length() - 1);
			}
			
			return buffer.toString();
		}
		throw new IllegalArgumentException();
	}

	public byte[] getDataBlock(int index) {
		if(index < blocks.size()) {
			return blocks.get(index);
		}
		throw new IllegalArgumentException();
	}

	public int getDataSize() {
		int size = 16;
		for(byte[] block : blocks) {
			size += block.length;
		}
		return size;
	}
}

