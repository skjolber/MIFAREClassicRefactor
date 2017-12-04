package com.skjolberg.nfc.refactor.mifareclassic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.nfc.tech.MifareClassic;
import android.util.Log;

import com.skjolberg.nfc.refactor.GalleryTag;
import com.skjolberg.nfc.refactor.MifareClassicGalleryTag;
import com.skjolberg.nfc.refactor.TagCapacityException;
import com.skjolberg.nfc.refactor.UnauthoriedException;
import com.skjolberg.nfc.refactor.mifareclassic.MifareClassicUtils.Operations;
import com.skjolberg.nfc.refactor.mifareclassic.data.MifareClassicSectorData;
import com.skjolberg.nfc.refactor.mifareclassic.data.MifareClassicTag;

public class MifareClassicIO {

	private static final String TAG = MifareClassicIO.class.getName();

	private List<MifareClassicKey> mifareClassicKeys;

	public MifareClassicIO(List<MifareClassicKey> keys) {
		this.mifareClassicKeys = keys;
		
		Log.d(TAG, "Have " + mifareClassicKeys.size() + " mifare classic keys");
	}

	public MifareClassicGalleryTag readMifareClassicGalleryTag(MifareClassic mifareClassic, MifareClassicScheme<MifareClassicKey> scheme, boolean data) throws IOException {

		MifareClassicGalleryTag galleryTag = new MifareClassicGalleryTag();

		byte[] id = mifareClassic.getTag().getId();
		if(id != null) {
			galleryTag.setTagId(Utils.getHexString(id));
		}

		MifareClassicTag tag = new MifareClassicTag();

		int capacity = 0;

		try {
			mifareClassic.connect();

			Log.d(TAG, "Read " + mifareClassic.getSectorCount() + " sectors");

			for(MifareClassicKey mifareClassicKey : mifareClassicKeys) {
				Log.d(TAG, "Key " + Utils.getHexString(mifareClassicKey.getValue(), true));
			}

			MifareClassicKey lastKeyA = null;
			MifareClassicKey lastKeyB = null;
			
			for (int i = 0; i < mifareClassic.getSectorCount(); i++) {

				boolean authenticatedAsA = false;
				boolean authenticatedAsB = false;

				boolean authenticatedKeyA = false;
				boolean authenticatedKeyB = false;

				MifareClassicKey currentKeyA = null;
				MifareClassicKey currentKeyB = null;

				// attempt to authenticate using tag key
				if(scheme != null) {
					MifareClassicKey keyA = scheme.getKeyA(i);

					if(keyA != null) {
						if(mifareClassic.authenticateSectorWithKeyA(i, keyA.getValue())) {
							currentKeyA = keyA;
							authenticatedKeyA = true;
							authenticatedAsA = true;
						}
					}
				}

				// attempt to authenticate using last key
				if(!authenticatedKeyA) {
					if (lastKeyA != null) {
						authenticatedKeyA = mifareClassic.authenticateSectorWithKeyA(i, lastKeyA.getValue());

						if(mifareClassic.authenticateSectorWithKeyA(i, lastKeyA.getValue())) {
							currentKeyA = lastKeyA;
							authenticatedKeyA = true;
							authenticatedAsA = true;
						} else {
							lastKeyA = null;
						}

					}
				}

				if(!authenticatedKeyA) {
					for(MifareClassicKey mifareClassicKey : mifareClassicKeys) {

						if(mifareClassic.authenticateSectorWithKeyA(i, mifareClassicKey.getValue())) {
							currentKeyA = mifareClassicKey;
							authenticatedKeyA = true;
							authenticatedAsA = true;
							break;
						}
					}
				}
				int blockIndex = mifareClassic.sectorToBlock(i);
				int blockCountInSector = mifareClassic.getBlockCountInSector(i);

				byte[] acBlock = null;
				byte[] ac = null;

				boolean mustHaveKeyAForData = false;
				boolean mustHaveKeyBForData = false;

				// determine key B
				if(authenticatedKeyA) {
					Log.d(TAG, "Read access conditions using key A");

					acBlock = mifareClassic.readBlock(blockIndex + blockCountInSector - 1);

					capacity += acBlock.length;

					ac = Arrays.copyOfRange(acBlock, 6, 10);

					if(MifareClassicUtils.isKeyBReadable(ac)) {
						//currentKeyB = new byte[6];
						byte[] value = new byte[6];
						
						System.arraycopy(acBlock, acBlock.length - 6, value, 0, 6);

						currentKeyB = new MifareClassicKey(value);
						Log.d(TAG, "Key B is readable");
					} else {
						Log.d(TAG, "Key B is not readable");
					}

					mustHaveKeyAForData = MifareClassicUtils.isKeyRequired(ac, true, Operations.Read);
					mustHaveKeyBForData = MifareClassicUtils.isKeyRequired(ac, false, Operations.Read);

					if(mustHaveKeyAForData) {
						Log.d(TAG, "We need key A to read the sector data contents");
					} else {
						Log.d(TAG, "We do not need key A to read the sector data contents");
					}

					if(mustHaveKeyBForData) {
						Log.d(TAG, "We need key B to read the sector data contents");
					} else {
						Log.d(TAG, "We do not need key B to read the sector data contents");
					}
				}

				if(currentKeyB == null) {
					Log.d(TAG, "Attempt to authenticate using key B");
					if(scheme != null) {
						MifareClassicKey keyB = scheme.getKeyB(i);

						if(keyB != null) {
							authenticatedAsA = false;
							
							if(mifareClassic.authenticateSectorWithKeyB(i, keyB.getValue())) {
								currentKeyB = keyB;
								authenticatedKeyB = true;
								authenticatedAsB = true;
							}
						}
					}

					if(!authenticatedKeyB) {
						if (lastKeyB != null) {
							authenticatedAsA = false;
							
							if(mifareClassic.authenticateSectorWithKeyB(i, lastKeyB.getValue())) {
								currentKeyB = lastKeyB;
								authenticatedKeyB = true;
								authenticatedAsB = true;
							} else {
								lastKeyB = null;
							}
						}
					}

					if(!authenticatedKeyB) {
						authenticatedAsA = false;

						for(MifareClassicKey mifareClassicKey : mifareClassicKeys) {
							if(mifareClassic.authenticateSectorWithKeyB(i, mifareClassicKey.getValue())) {
								currentKeyB = mifareClassicKey;
								authenticatedKeyB = true;
								authenticatedAsB = true;

								break;
							}
						}
					}

					if(authenticatedKeyB && !authenticatedKeyA) {
						Log.d(TAG, "Read access conditions using key B");

						acBlock = mifareClassic.readBlock(blockIndex + blockCountInSector - 1);

						capacity += acBlock.length;

						ac = Arrays.copyOfRange(acBlock, 6, 10);	

						mustHaveKeyAForData = MifareClassicUtils.isKeyRequired(ac, true, Operations.Read);
						mustHaveKeyBForData = MifareClassicUtils.isKeyRequired(ac, false, Operations.Read);
					}

				}

				if(mustHaveKeyAForData) {
					Log.d(TAG, "We need key A to read the sector data contents");
				} else {
					Log.d(TAG, "We do not need key A to read the sector data contents");
				}

				if(mustHaveKeyBForData) {
					Log.d(TAG, "We need key B to read the sector data contents");
				} else {
					Log.d(TAG, "We do not need key B to read the sector data contents");
				}

				boolean read;
				// Acquire read access for all the sectors
				if (!authenticatedKeyA && !authenticatedKeyB) {
					read = false;
					
					//throw new UnauthoriedException(false, false, "Unable to authenticate using key A or key B");
				} else if(mustHaveKeyBForData && currentKeyB == null) {
					read = false;
					
					//throw new UnauthoriedException(false, true, "Unable to determine key B");
				} else if(mustHaveKeyAForData && currentKeyA == null) {
					read = false;
					
					//throw new UnauthoriedException(true, false, "Unable to determine key A");
				} else {
					read = true;
				}

				// Log.d(TAG, "Sector " + i + " has " + blockCountInSector + " blocks");

				MifareClassicSectorData sectorData = new MifareClassicSectorData();
				sectorData.setIndex(i);
				
				int offset = 0;

				if(data) {
					if(read) {
						byte[][] acMatrix = MifareClassicUtils.acToACMatrix(ac);
						boolean isKeyBReadable = MifareClassicUtils.isKeyBReadable(acMatrix[0][3], acMatrix[1][3], acMatrix[2][3]);

						Log.d(TAG, "We seem to have found the keys we require to clone the tag");
	
						for (int k = offset; k < blockCountInSector - 1; k++) {
							// Log.d(TAG, "Read sector " + i + " block " +
							// (blockIndex + k));
		
							// Data block.
							int acIndex = k;
							// Handle Mifare Classic 4k Tags.
							if (i >= 32) {
								if (k >= 0 && k <= 4) {
									acIndex = 0;
								} else if (k >= 5 && k <= 9) {
									acIndex = 1;
								} else if (k >= 10 && k <= 14) {
									acIndex = 2;
								}
							}
		
							int operationInfoForBlock = MifareClassicUtils.getOperationInfoForBlock(acMatrix[0][acIndex], acMatrix[1][acIndex], acMatrix[2][acIndex], Operations.Read, false, isKeyBReadable);
		
							// do we need to change the key?
		
							if(!authenticatedAsA && operationInfoForBlock == 2) { 
								// currently authenticated as A, but must be B to read
								Log.d(TAG, "Change authentication to B");
		
								if(currentKeyB == null) {
									throw new IllegalArgumentException("Expected key B");
								}
		
								if(!mifareClassic.authenticateSectorWithKeyB(i, currentKeyB.getValue())) {
									throw new IllegalArgumentException("Expected B authentication at sector " + i + " using key " + Utils.getHexString(currentKeyA.getValue()));
								}
		
								authenticatedAsA = false;
								authenticatedAsB = true;
							} else if(!authenticatedAsB && operationInfoForBlock == 1) { // b and only b can read
								// currently authenticated as B, but must be A to read
								Log.d(TAG, "Change authentication to A");
		
								if(currentKeyA == null) {
									throw new IllegalArgumentException("Expected key A");
								}
								if(!mifareClassic.authenticateSectorWithKeyA(i, currentKeyA.getValue())) {
									throw new IllegalArgumentException("Expected A authentication at sector " + i + " using key " + Utils.getHexString(currentKeyA.getValue()));
								}
		
								authenticatedAsA = true;
								authenticatedAsB = false;
							} else if(!authenticatedAsB && !authenticatedAsA && operationInfoForBlock == 3) {
		
								if(authenticatedKeyA) {
									Log.d(TAG, "Change (any) authentication to A");
		
									if(currentKeyA == null) {
										throw new IllegalArgumentException("Expected key A");
									}
									if(!mifareClassic.authenticateSectorWithKeyA(i, currentKeyA.getValue())) {
										throw new IllegalArgumentException("Expected A authentication at sector " + i + " using key " + Utils.getHexString(currentKeyA.getValue()));
									}
		
									authenticatedAsA = true;
									authenticatedAsB = false;
								} else if(authenticatedKeyB) {
									// currently authenticated as A, but must be B to read
									Log.d(TAG, "Change (any) authentication to B");
		
									if(currentKeyB == null) {
										throw new IllegalArgumentException("Expected key B");
									}
		
									if(!mifareClassic.authenticateSectorWithKeyB(i, currentKeyB.getValue())) {
										throw new IllegalArgumentException("Expected B authentication at sector " + i + " using key " + Utils.getHexString(currentKeyA.getValue()));
									}
		
									authenticatedAsA = false;
									authenticatedAsB = true;
								} else {
									throw new IllegalArgumentException("Expected to have either key A or key B at this point");
								}
								
							} else {
								Log.d(TAG, "Already correctly authenticated with " + (authenticatedAsA ? "A" : ""));
							}
		
							byte[] readBlock = mifareClassic.readBlock(blockIndex + k);
		
							Log.d(TAG, "Read sector " + i + " block " + k + " (" + (blockIndex + k) + ")" );
		
							capacity += readBlock.length;
		
							sectorData.addBlock(readBlock);
						}
					}
				} else {
					Log.d(TAG, "No need to read data");
				}

				// now append the key-value 
				if(currentKeyA != null) {
					sectorData.setTrailerBlockKeyA(currentKeyA);
				}
				sectorData.setTrailerBlockAccessConditions(ac);

				if(currentKeyB != null) {
					sectorData.setTrailerBlockKeyB(currentKeyB);
				}

				tag.add(sectorData);

				// save value for next iteration
				if(currentKeyA != null) {
					lastKeyA = currentKeyA;
				}
				
				if(currentKeyB != null) {
					lastKeyB = currentKeyB;
				}
			}
		} finally {
			mifareClassic.close();
		}

		List<MifareClassicSectorData> sectors = tag.getSectors();

		//sectors = compress(tag, sectors);

		byte[] bytes = tag.toByteArray();

		Log.d(TAG, "Read tag capacity " + capacity + " and stored the result in " + bytes.length + " bytes.");

		if(!tag.isComplete()) {
			List<MifareClassicSectorData> incompleteSectors = tag.getIncompleteSectors();

			Log.d(TAG, "Tag had " + incompleteSectors.size() + "/" + sectors.size() + " incomplete sectors");
		} else {
			Log.d(TAG, "Tag had no incomplete sectors");
		}

		//mifareClassicDataTag.printMifare();

		galleryTag.setMifareClassicDataTag(tag);
		galleryTag.setBytes(bytes);
		galleryTag.setSize(bytes.length);
		galleryTag.setCapacity(capacity);
		galleryTag.setType(GalleryTag.TYPE_MIFARE_CLASSIC);
		galleryTag.setTech(MifareClassic.class.getName());

		return galleryTag;
	}

	private List<MifareClassicSectorData> compress(MifareClassicTag tag, List<MifareClassicSectorData> sectors) {
		// compress content by 
		int lastIndex = sectors.size() - 1;
		while(lastIndex > 0) {
			if(sectors.get(lastIndex - 1).isFill(sectors.get(lastIndex))) {
				lastIndex--;
			} else {
				break;
			}
		}

		if(lastIndex < sectors.size() - 1) {
			Log.d(TAG, "Compress from " + sectors.size() + " to " + (lastIndex + 1) + " sectors");

			sectors = sectors.subList(0, lastIndex + 1);

			tag.setSectors(sectors);

			tag.setCompressed(true);
		}
		return sectors;
	}

	public List<MifareClassicSectorDiff> writeMifareClassic(MifareClassic mifareClassic, MifareClassicScheme<MifareClassicKey> scheme, MifareClassicTag tag, boolean data) throws IOException, TagCapacityException {

		List<MifareClassicSectorDiff> result = new ArrayList<MifareClassicSectorDiff>();

		try {
			mifareClassic.connect();

			// check capacity
			if(tag.getSectorCount() >= mifareClassic.getSectorCount()) {
				int capacity = getCapacity(mifareClassic);
				
				int size = tag.getDataSize();
				
				Log.d(TAG, "Tag capacity is " + capacity + ", required size is " + size);

				if (size > capacity) {
					throw new TagCapacityException(capacity, size);
				}
			} else {
				Log.d(TAG, "Tag capacity is sufficient");
			}
			
			Log.d(TAG, "Write " + tag.getSectorCount() + " sectors");

			for(MifareClassicKey mifareClassicKey : mifareClassicKeys) {
				Log.d(TAG, "Key " + Utils.getHexString(mifareClassicKey.getValue(), true));
			}

			MifareClassicKey lastKeyA = null;
			MifareClassicKey lastKeyB = null;
			byte[] lastTrailer = null;
			
			for (int i = 0; i < mifareClassic.getSectorCount(); i++) {

				boolean authenticatedKeyA = false;
				boolean authenticatedKeyB = false;

				MifareClassicKey currentKeyA = null;
				MifareClassicKey currentKeyB = null;

				// attempt to authenticate using tag key
				if(scheme != null) {
					MifareClassicKey keyA = scheme.getKeyA(i);

					if(keyA != null) {
						if(mifareClassic.authenticateSectorWithKeyA(i, keyA.getValue())) {
							currentKeyA = keyA;
							authenticatedKeyA = true;
						}
					}
				}

				// attempt to authenticate using last key
				if(!authenticatedKeyA) {
					if (lastKeyA != null) {
						if(mifareClassic.authenticateSectorWithKeyA(i, lastKeyA.getValue())) {
							currentKeyA = lastKeyA;
							authenticatedKeyA = true;
						} else {
							lastKeyA = null;
						}

					}
				}

				if(!authenticatedKeyA) {
					for(MifareClassicKey mifareClassicKey : mifareClassicKeys) {

						if(mifareClassic.authenticateSectorWithKeyA(i, mifareClassicKey.getValue())) {
							currentKeyA = mifareClassicKey;
							authenticatedKeyA = true;
							break;
						}
					}
				}
				int blockIndex = mifareClassic.sectorToBlock(i);
				int blockCountInSector = mifareClassic.getBlockCountInSector(i);

				byte[] acBlock = null;
				byte[] ac = null;
				byte[][] acMatrix = null;
				
				int writeACRequirement = -1;
				int writeKeyARequirement = -1;
				int writeKeyBRequirement = -1;
				
				boolean isKeyBReadable = false;
				
				int[] writeDataRequirement = new int[]{-1, -1, -1};
				
				// determine key B
				if(authenticatedKeyA) {
					Log.d(TAG, "Read access conditions using key A");

					acBlock = mifareClassic.readBlock(blockIndex + blockCountInSector - 1);

					ac = Arrays.copyOfRange(acBlock, 6, 10);
					acMatrix = MifareClassicUtils.acToACMatrix(ac);
			        isKeyBReadable = MifareClassicUtils.isKeyBReadable(acMatrix[0][3], acMatrix[1][3], acMatrix[2][3]);
			        
			        for(int k = 0; k < 4; k++) {
			        	Log.d(TAG, "Access conditions " + k + ": " + acMatrix[0][k] + acMatrix[1][k] + acMatrix[2][k]);
			        }
			        
			        // check that we keys corresponding to access conditions
			        for(int acIndex = 0; acIndex < 3; acIndex++) {
			        	writeDataRequirement[acIndex] = MifareClassicUtils.getOperationInfoForBlock(acMatrix[0][acIndex], acMatrix[1][acIndex], acMatrix[2][acIndex], Operations.Write, false, isKeyBReadable);
			        }

			        writeACRequirement = MifareClassicUtils.getOperationInfoForBlock(acMatrix[0][3], acMatrix[1][3], acMatrix[2][3], Operations.WriteAC, true, isKeyBReadable);
			        writeKeyARequirement = MifareClassicUtils.getOperationInfoForBlock(acMatrix[0][3], acMatrix[1][3], acMatrix[2][3], Operations.WriteKeyA, true, isKeyBReadable);
			        writeKeyBRequirement = MifareClassicUtils.getOperationInfoForBlock(acMatrix[0][3], acMatrix[1][3], acMatrix[2][3], Operations.WriteKeyB, true, isKeyBReadable);
				}

				if(!authenticatedKeyA || writeACRequirement == 2 || writeKeyARequirement == 2 || writeKeyBRequirement == 2 || writeDataRequirement[0] == 2 || writeDataRequirement[1] == 2 || writeDataRequirement[2] == 2) {
					Log.d(TAG, "Attempt to authenticate using key B");
					if(scheme != null) {
						MifareClassicKey keyB = scheme.getKeyB(i);

						if(keyB != null) {
							if(mifareClassic.authenticateSectorWithKeyB(i, keyB.getValue())) {
								currentKeyB = keyB;
								authenticatedKeyB = true;
							}
						}
					}

					if(!authenticatedKeyB) {
						if (lastKeyB != null) {
							if(mifareClassic.authenticateSectorWithKeyB(i, lastKeyB.getValue())) {
								currentKeyB = lastKeyB;
								authenticatedKeyB = true;
							} else {
								lastKeyB = null;
							}
						}
					}

					if(!authenticatedKeyB) {
						for(MifareClassicKey mifareClassicKey : mifareClassicKeys) {
							if(mifareClassic.authenticateSectorWithKeyB(i, mifareClassicKey.getValue())) {
								currentKeyB = mifareClassicKey;
								authenticatedKeyB = true;

								break;
							}
						}
					}

					if(authenticatedKeyB) {
						Log.d(TAG, "Authenticated using key B");

						if(!authenticatedKeyA) {
							Log.d(TAG, "Read access conditions using key B");
	
							acBlock = mifareClassic.readBlock(blockIndex + blockCountInSector - 1);
	
							ac = Arrays.copyOfRange(acBlock, 6, 10);
	
							acMatrix = MifareClassicUtils.acToACMatrix(ac);
					        isKeyBReadable = MifareClassicUtils.isKeyBReadable(acMatrix[0][3], acMatrix[1][3], acMatrix[2][3]);
					        
					        for(int k = 0; k < 4; k++) {
					        	Log.d(TAG, "Access conditions " + k + ": " + acMatrix[0][k] + acMatrix[1][k] + acMatrix[2][k]);
					        }

					        // check that we keys corresponding to access conditions
					        for(int acIndex = 0; acIndex < 3; acIndex++) {
					        	writeDataRequirement[acIndex] = MifareClassicUtils.getOperationInfoForBlock(acMatrix[0][acIndex], acMatrix[1][acIndex], acMatrix[2][acIndex], Operations.Write, false, isKeyBReadable);
					        }
	
					        writeACRequirement = MifareClassicUtils.getOperationInfoForBlock(acMatrix[0][3], acMatrix[1][3], acMatrix[2][3], Operations.WriteAC, true, isKeyBReadable);
					        writeKeyARequirement = MifareClassicUtils.getOperationInfoForBlock(acMatrix[0][3], acMatrix[1][3], acMatrix[2][3], Operations.WriteKeyA, true, isKeyBReadable);
					        writeKeyBRequirement = MifareClassicUtils.getOperationInfoForBlock(acMatrix[0][3], acMatrix[1][3], acMatrix[2][3], Operations.WriteKeyB, true, isKeyBReadable);
						}
					}

				}
				
				if (!authenticatedKeyA && !authenticatedKeyB) {
					throw new UnauthoriedException(false, false, "Unable to authenticate using key A or key B");
				}
				
				if(writeDataRequirement[0] == 0 || writeDataRequirement[1] == 0 || writeDataRequirement[2] == 0) {
					throw new IllegalArgumentException("Write data is blocked according to access conditions");
				}
				
				MifareClassicSectorData mifareClassicSectorData;
				if(i >= tag.getSectorCount()) {
					Log.d(TAG, "Write blank sector");
					
					mifareClassicSectorData = new MifareClassicSectorData();
					mifareClassicSectorData.setTrailerBlockAccessConditions(Arrays.copyOfRange(lastTrailer, 6, 10));
					mifareClassicSectorData.setTrailerBlockKeyA(new MifareClassicKey(Arrays.copyOfRange(lastTrailer, 0, 6)));
					mifareClassicSectorData.setTrailerBlockKeyB(new MifareClassicKey(Arrays.copyOfRange(lastTrailer, 10, 16)));
					
					byte[] empty = new byte[MifareClassic.BLOCK_SIZE];
					for(int k = 0; k < empty.length; k++) {
						empty[k] = 0x00;
					}
					
					for(int k = 0; k < mifareClassic.getBlockCount() - 1; k++) {
						mifareClassicSectorData.addBlock(empty);
					}
				} else {
					mifareClassicSectorData = tag.get(i);
				}
				
				MifareClassicKey writeKeyA = null;
				if(mifareClassicSectorData.hasTrailerBlockKeyA()) {
					writeKeyA = mifareClassicSectorData.getTrailerBlockKeyA();
				}
				if(scheme != null && scheme.hasKeyA(i)) {
					writeKeyA = scheme.getKeyA(i);
					
					Log.d(TAG, "Override scheme in sector " + i + " trailer key A: " + Utils.getHexString(writeKeyA.getValue()));
				}

				MifareClassicKey writeKeyB = null;
				if(isKeyBReadable) {
					writeKeyB = new MifareClassicKey(Arrays.copyOfRange(acBlock, 10, 16));
				}
				
				if(mifareClassicSectorData.hasTrailerBlockKeyB()) {
					writeKeyB = mifareClassicSectorData.getTrailerBlockKeyB();
				}
				if(scheme != null && scheme.hasKeyB(i)) {
					writeKeyB = scheme.getKeyB(i);
					
					Log.d(TAG, "Override scheme in sector " + i + " trailer key B: " + Utils.getHexString(writeKeyB.getValue()));
				}
				
				byte[] writeAC = Arrays.copyOfRange(acBlock, 6, 10);
				if(mifareClassicSectorData.hasTrailerBlockAccessConditions()) {
					writeAC = mifareClassicSectorData.getTrailerBlockAccessConditions();
				}
				if(scheme != null && scheme.hasAccessConditions(i)) {
					if(writeAC != null) {
						writeAC = scheme.getTrailerBlockAccessConditions(i, writeAC);
					} else if(scheme.hasCompleteAccessConditionBits(i)){
						writeAC = scheme.getTrailerBlockAccessConditions(i);
					} else {
						// no access conditions (whooops)
					}
				}

				if(writeKeyA == null) {
					throw new IllegalArgumentException("Unable to determine output target key A");
				}

				if(writeKeyB == null) {
					throw new IllegalArgumentException("Unable to determine output target key B");
				}

				if(writeAC == null) {
					throw new IllegalArgumentException("Unable to determine output target access conditions");
				}

				boolean mustWriteKeyA = true;
				boolean mustWriteKeyB = true;
				boolean mustWriteAC = true;

				if(currentKeyA != null) {
					if(Arrays.equals(currentKeyA.getValue(), writeKeyA.getValue())) {
						mustWriteKeyA = false;
					}
				}

				if(currentKeyB != null) {
					if(Arrays.equals(currentKeyB.getValue(), writeKeyB.getValue())) {
						mustWriteKeyB = false;
					}
				}
				
				if(Arrays.equals(ac, writeAC)) {
					mustWriteAC = false;
				}

				if(currentKeyA == null && ((mustWriteAC && writeACRequirement == 1) || (mustWriteKeyA && writeKeyARequirement == 1) || (mustWriteKeyB && writeKeyBRequirement == 1) || writeDataRequirement[0] == 1 || writeDataRequirement[1] == 1 || writeDataRequirement[2] == 1)) {
					throw new UnauthoriedException(true, false, "Unable to determine key A");
				}

				if(currentKeyB == null && ((mustWriteAC && writeACRequirement == 2) || (mustWriteKeyA && writeKeyARequirement == 2) || (mustWriteKeyB && writeKeyBRequirement == 2) || writeDataRequirement[0] == 2 || writeDataRequirement[1] == 2 || writeDataRequirement[2] == 2)) {
					throw new UnauthoriedException(false, true, "Unable to determine key B");
				}

				Log.d(TAG, "We seem to have found the keys we require to write sector " + i);
				// Log.d(TAG, "Sector " + i + " has " + blockCountInSector + " blocks");

				boolean authenticatedAsA = !authenticatedKeyB;

				if(data) {
					int offset;
					if (i == 0) {
						offset = 1;
					} else {
						offset = 0;
					}
					
					for (int k = offset; k < blockCountInSector - 1; k++) {
						// Log.d(TAG, "Read sector " + i + " block " +
						// (blockIndex + k));
	
						// Data block.
						int acIndex = k;
						// Handle Mifare Classic 4k Tags.
						if (i >= 32) {
							if (k >= 0 && k <= 4) {
								acIndex = 0;
							} else if (k >= 5 && k <= 9) {
								acIndex = 1;
							} else if (k >= 10 && k <= 14) {
								acIndex = 2;
							}
						}
	
						int operationInfoForBlock = MifareClassicUtils.getOperationInfoForBlock(acMatrix[0][acIndex], acMatrix[1][acIndex], acMatrix[2][acIndex], Operations.Write, false, isKeyBReadable);
	
						// do we need to change the key?
	
						if(authenticatedAsA && operationInfoForBlock == 2) { 
							// currently authenticated as A, but must be B to read
							Log.d(TAG, "Change authentication to B");
	
							if(currentKeyB == null) {
								throw new IllegalArgumentException("Expected key B");
							}
	
							if(!mifareClassic.authenticateSectorWithKeyB(i, currentKeyB.getValue())) {
								throw new IllegalArgumentException("Expected B authentication at sector " + i + " using key " + Utils.getHexString(currentKeyA.getValue()));
							}
	
							authenticatedAsA = false;
						} else if(!authenticatedAsA && operationInfoForBlock == 1) { // b and only b can read
							// currently authenticated as B, but must be A to read
							Log.d(TAG, "Change authentication to A");
	
							if(currentKeyA == null) {
								throw new IllegalArgumentException("Expected key A");
							}
							if(!mifareClassic.authenticateSectorWithKeyA(i, currentKeyA.getValue())) {
								throw new IllegalArgumentException("Expected A authentication at sector " + i + " using key " + Utils.getHexString(currentKeyA.getValue()));
							}
	
							authenticatedAsA = true;
						} else {
							Log.d(TAG, "Already correctly authenticated with " + (authenticatedAsA ? "A" : "B"));
						}
	
						mifareClassic.writeBlock(blockIndex + k, mifareClassicSectorData.getDataBlock(k));
						
						Log.d(TAG, "Write sector " + i + " block " + k + " (" + (blockIndex + k) + ")" );
					}
				}

				byte[] trailer = new byte[MifareClassic.BLOCK_SIZE];
				System.arraycopy(writeKeyA.getValue(), 0, trailer, 0, writeKeyA.getValue().length);
				System.arraycopy(writeAC, 0, trailer, writeKeyA.getValue().length, writeAC.length);
				System.arraycopy(writeKeyB.getValue(), 0, trailer, writeKeyA.getValue().length + writeAC.length, writeKeyB.getValue().length);

				if(mustWriteKeyA || mustWriteKeyB || mustWriteAC) {

					if(mustWriteKeyA) {
						Log.d(TAG, "Change key A; " + writeKeyARequirement);
					}
					
					if(mustWriteAC) {
						Log.d(TAG, "Change access conditions; " + writeACRequirement);
					}
					
					if(mustWriteKeyB) {
						Log.d(TAG, "Change key B; " + writeKeyBRequirement);
					}
					
					MifareClassicSectorDiff diff = new MifareClassicSectorDiff();
					diff.setIndex(i);
					diff.setKeyA(mustWriteKeyA);
					diff.setKeyB(mustWriteKeyB);
					diff.setAccessConditions(mustWriteAC);
					
					result.add(diff);
					
					// it is not possible that both keys are required to write the trailer (i.e. with two write operations)
					if(authenticatedKeyA && (!mustWriteAC || (writeACRequirement == 1 || writeACRequirement == 3)) && (!mustWriteKeyA || (writeKeyARequirement == 1 || writeKeyARequirement == 3)) && (!mustWriteKeyB || (writeKeyBRequirement == 1 || writeKeyBRequirement == 3))) {
						
						if(!authenticatedAsA) {
							// currently authenticated as B, but must be A to read
							Log.d(TAG, "Change authentication to A for trailer");

							if(currentKeyA == null) {
								throw new IllegalArgumentException("Expected key A");
							}
							if(!mifareClassic.authenticateSectorWithKeyA(i, currentKeyA.getValue())) {
								throw new IllegalArgumentException("Expected A authentication at sector " + i + " using key " + Utils.getHexString(currentKeyA.getValue()));
							}

							authenticatedAsA = true;
						}
						
						Log.d(TAG, "Write trailer bytes using key A");						
					} else if(authenticatedKeyB && (!mustWriteAC || (writeACRequirement == 2 || writeACRequirement == 3)) && (!mustWriteKeyA || (writeKeyARequirement == 2 || writeKeyARequirement == 3)) && (!mustWriteKeyB || (writeKeyBRequirement == 2 || writeKeyBRequirement == 3))) {

						if(authenticatedAsA) {
							// currently authenticated as A, but must be B to read
							Log.d(TAG, "Change authentication to B for trailer");
	
							if(currentKeyB == null) {
								throw new IllegalArgumentException("Expected key B");
							}
	
							if(!mifareClassic.authenticateSectorWithKeyB(i, currentKeyB.getValue())) {
								throw new IllegalArgumentException("Expected B authentication at sector " + i + " using key " + Utils.getHexString(currentKeyA.getValue()));
							}
	
							authenticatedAsA = false;
						}
						
						Log.d(TAG, "Write trailer bytes using key B");
					} else {
						throw new IllegalArgumentException("Cannot determine whether to use key A or key B for writing trailer with access bits " + acMatrix[0][3] + acMatrix[1][3] + acMatrix[2][3]);
					}
					
					Log.d(TAG, "Change trailer from " + Utils.getHexString(acBlock) + " to " + Utils.getHexString(trailer) + " for access bits " + acMatrix[0][3] + acMatrix[1][3] + acMatrix[2][3]);
					
					mifareClassic.writeBlock(blockIndex + blockCountInSector - 1, trailer);
				} else {
					Log.d(TAG, "Skip writing trailer block; identical bytes");
				}
				
				// save value for next iteration
				lastKeyA = currentKeyA;
				lastKeyB = currentKeyB;
				lastTrailer = trailer;
			}
			
			if(tag.isCompressed()) {
				Log.d(TAG, "Wrote " + (mifareClassic.getSectorCount() - tag.getSectorCount()) + " empty sectors");
			}
			
			
		} finally {
			mifareClassic.close();
		}

		Log.d(TAG, "Wrote tag type " + android.nfc.tech.MifareClassic.class.getName());
		
		return result;
	}

	private int getCapacity(MifareClassic mifareClassic) {
		int capacity = 0;
		for (int i = 0; i < mifareClassic.getSectorCount(); i++) {
			int blockCountInSector = mifareClassic.getBlockCountInSector(i);
			
			capacity += blockCountInSector * MifareClassic.BLOCK_SIZE;
		}
		return capacity;
	}

	public List<MifareClassicSectorDiff> verifyMifareClassic(MifareClassicScheme<MifareClassicKey> scheme, MifareClassicTag tag) {

		List<MifareClassicSectorDiff> result = new ArrayList<MifareClassicSectorDiff>();
		
		for (int i = 0; i < tag.getSectorCount(); i++) {
			MifareClassicSectorData mifareClassicSectorData = tag.get(i);
			
			MifareClassicKey writeKeyA = null;
			if(mifareClassicSectorData.hasTrailerBlockKeyA()) {
				writeKeyA = mifareClassicSectorData.getTrailerBlockKeyA();
			}
			if(scheme != null && scheme.hasKeyA(i)) {
				writeKeyA = scheme.getKeyA(i);
			}

			MifareClassicKey writeKeyB = null;
			if(mifareClassicSectorData.hasTrailerBlockKeyB()) {
				writeKeyB = mifareClassicSectorData.getTrailerBlockKeyB();
			}
			if(scheme != null && scheme.hasKeyB(i)) {
				writeKeyB = scheme.getKeyB(i);
			}
			
			byte[] writeAC = null;
			if(mifareClassicSectorData.hasTrailerBlockAccessConditions()) {
				writeAC = mifareClassicSectorData.getTrailerBlockAccessConditions();
			}
			
			if(scheme != null && scheme.hasAccessConditions(i)) {
				if(writeAC != null) {
					writeAC = scheme.getTrailerBlockAccessConditions(i, writeAC);
				} else if(scheme.hasCompleteAccessConditionBits(i)){
					writeAC = scheme.getTrailerBlockAccessConditions(i);
				} else {
					// no access conditions
				}
			}
			
			if(writeKeyA == null || writeKeyB == null || writeAC == null) {
				MifareClassicSectorDiff incompatibility = new MifareClassicSectorDiff();
				incompatibility.setIndex(i);
				
				if(writeKeyA == null) {
					Log.d(TAG, "Unable to determine output target key A for sector " + mifareClassicSectorData.getIndex());
					
					incompatibility.setKeyA(true);
				}
	
				if(writeKeyB == null) {
					Log.d(TAG, "Unable to determine output target key B for sector " + mifareClassicSectorData.getIndex());
					
					incompatibility.setKeyB(true);
				}
	
				if(writeAC == null) {
					Log.d(TAG, "Unable to determine output target access conditions for sector " + mifareClassicSectorData.getIndex());
					
					incompatibility.setAccessConditions(true);
				}

				result.add(incompatibility);
			}
			
		}

		return result;
	}
	
	
	
}
