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
package com.skjolberg.nfc.refactor.mifareclassic.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.util.Log;

import com.skjolberg.nfc.refactor.mifareclassic.Utils;
import com.skjolberg.nfc.refactor.mifareclassic.MifareClassicKey;
import com.skjolberg.nfc.refactor.mifareclassic.MifareClassicUtils;

public class MifareClassicSectorData {
	
	private static final String TAG = MifareClassicSectorData.class.getName();

    public static String getHexString (byte[] a) {
		StringBuilder sb = new StringBuilder();
		for (byte b : a) {
			sb.append(String.format("%02x ", b & 0xff));
		}
		return sb.toString().trim().toUpperCase();
    }

	private int index = -1;

	private MifareClassicKey trailerBlockKeyA;
	private byte[] trailerBlockAccessConditions;
	private MifareClassicKey trailerBlockKeyB;

	private List<byte[]> blocks = new ArrayList<byte[]>();

	public MifareClassicKey getTrailerBlockKeyA() {
		return trailerBlockKeyA;
	}

	public void setTrailerBlockKeyA(MifareClassicKey trailerBlockKeyA) {
		this.trailerBlockKeyA = trailerBlockKeyA;
	}

	public MifareClassicKey getTrailerBlockKeyB() {
		return trailerBlockKeyB;
	}

	public void setTrailerBlockKeyB(MifareClassicKey trailerBlockKeyB) {
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
			size += trailerBlockKeyA.getValue().length;
		}

		size += 4;
		if(trailerBlockAccessConditions != null) {
			size += trailerBlockAccessConditions.length;
		}

		size += 4;
		if(trailerBlockKeyB != null) {
			size += trailerBlockKeyB.getValue().length;
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
			byte[] content = new byte[trailerBlockKeyACount];
			din.readFully(content);
			
			trailerBlockKeyA = new MifareClassicKey(content);
		}

		int trailerBlockAccessConditionsCount = din.readInt();
		if(trailerBlockAccessConditionsCount > 0)  {
			trailerBlockAccessConditions = new byte[trailerBlockAccessConditionsCount];
			din.readFully(trailerBlockAccessConditions);
		}

		int trailerBlockKeyBCount = din.readInt();
		if(trailerBlockKeyBCount > 0)  {
			byte[] content = new byte[trailerBlockKeyBCount];
			din.readFully(content);
			
			trailerBlockKeyB = new MifareClassicKey(content);
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
			dout.writeInt(trailerBlockKeyA.getValue().length);
			dout.write(trailerBlockKeyA.getValue());
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
			dout.writeInt(trailerBlockKeyB.getValue().length);
			dout.write(trailerBlockKeyB.getValue());
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
		if(!blocks.isEmpty()) {
			for(byte[] block : blocks) {
				Log.d(TAG, getHexString(block));
			}
		} else {
			Log.d(TAG, "--------------------------------");
			Log.d(TAG, "--------------------------------");
			Log.d(TAG, "--------------------------------");
		}
		
		StringBuffer buffer = new StringBuffer();
		
		if(trailerBlockKeyA != null) {
			buffer.append(getHexString(trailerBlockKeyA.getValue()));
		} else {
			buffer.append("------------");
		}

		buffer.append(" ");

		if(trailerBlockAccessConditions != null) {
			buffer.append(getHexString(trailerBlockAccessConditions));
		} else {
			buffer.append("--------");
		}

		buffer.append(" ");

		if(trailerBlockKeyB != null) {
			buffer.append(getHexString(trailerBlockKeyB.getValue()));
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

	public boolean isEqualTrailer(MifareClassicSectorData data) {
		if(!Arrays.equals(trailerBlockKeyA.getValue(), data.trailerBlockKeyA.getValue())) {
			Log.d(TAG, "Trailer block A do not match");
			return false;
		}
		if(!Arrays.equals(trailerBlockAccessConditions, data.trailerBlockAccessConditions)) {
			Log.d(TAG, "Trailer access conditions do not match");
			return false;
		}
		if(!Arrays.equals(trailerBlockKeyB.getValue(), data.trailerBlockKeyB.getValue())) {
			Log.d(TAG, "Trailer block B do not match");
			return false;
		}
		return true;
	}
	
	public boolean isFill(MifareClassicSectorData data) {
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
				buffer.append(Utils.getHexString(trailerBlockKeyA.getValue()));
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
				buffer.append(Utils.getHexString(trailerBlockKeyB.getValue()));
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

