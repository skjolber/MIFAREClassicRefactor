/*
 * Copyright 2013 Gerhard Klostermeier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.skjolberg.nfc.clone.mifareclassic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.nfc.tech.MifareClassic;
import android.util.SparseArray;
public class MifareClassicUtils {

    private static final String LOG = MifareClassicUtils.class.getName();

	public static boolean isPermanentTrailerIndex(int index) {
		if(index != -1) {
			return index <= 3 || index == 7;
		}
		return false;
	}

	protected static boolean isPermanentDataIndex(int index) {
		return index <= 3 || index == 4 || index == 6 || index == 7;
	}

    /**
     * Some classical Mifare keys retrieved by a quick google search
     * ("mifare standard keys").
     */
    public static final String[] SOME_CLASSICAL_KNOWN_KEYS = {   "000000000000",
            "A0B0C0D0E0F0",
            "A1B1C1D1E1F1",
            "B0B1B2B3B4B5",
            "4D3A99C351DD",
            "1A982C7E459A",
            "AABBCCDDEEFF"
     };

    /**
     * Possible operations the on a Mifare Classic Tag.
     */
    public enum Operations {
        Read, Write, Increment, DecTransRest, ReadKeyA, ReadKeyB, ReadAC,
        WriteKeyA, WriteKeyB, WriteAC
    }

    public static final int[] accessBits = new int[]{0x000, 0x010, 0x100, 0x110, 0x001, 0x011, 0x101, 0x111};
    
    public static MifareClassicKeyType getOperationInfoForBlock2(byte c1, byte c2, byte c3,
            Operations op, boolean isSectorTrailer, boolean isKeyBReadable) {
        // Is Sector Trailer?
        if (isSectorTrailer) {
            // Sector Trailer.
            if (op != Operations.ReadKeyA && op != Operations.ReadKeyB
                    && op != Operations.ReadAC
                    && op != Operations.WriteKeyA
                    && op != Operations.WriteKeyB
                    && op != Operations.WriteAC) {
                // Error. Sector Trailer but no Sector Trailer permissions.
                throw new IllegalArgumentException();
            }
            if          (c1 == 0 && c2 == 0 && c3 == 0) {
                if (op == Operations.WriteKeyA
                        || op == Operations.WriteKeyB
                        || op == Operations.ReadKeyB
                        || op == Operations.ReadAC) {
                    return MifareClassicKeyType.A;
                }
                return null;
            } else if   (c1 == 0 && c2 == 1 && c3 == 0) {
                if (op == Operations.ReadKeyB
                        || op == Operations.ReadAC) {
                    return MifareClassicKeyType.A;
                }
                return null;
            } else if   (c1 == 1 && c2 == 0 && c3 == 0) {
                if (op == Operations.WriteKeyA
                        || op == Operations.WriteKeyB) {
                	return MifareClassicKeyType.B;
                }
                if (op == Operations.ReadAC) {
                	return MifareClassicKeyType.AorB;
                }
                return null;
            } else if   (c1 == 1 && c2 == 1 && c3 == 0) {
                if (op == Operations.ReadAC) {
                	return MifareClassicKeyType.AorB;
                }
                return null;
            } else if   (c1 == 0 && c2 == 0 && c3 == 1) {
                if (op == Operations.ReadKeyA) {
                    return null;
                }
                return MifareClassicKeyType.A;
            } else if   (c1 == 0 && c2 == 1 && c3 == 1) {
                if (op == Operations.ReadAC) {
                	return MifareClassicKeyType.AorB;
                }
                if (op == Operations.ReadKeyA
                        || op == Operations.ReadKeyB) {
                    return null;
                }
                return MifareClassicKeyType.B;
            } else if   (c1 == 1 && c2 == 0 && c3 == 1) {
                if (op == Operations.ReadAC) {
                	return MifareClassicKeyType.AorB;
                }
                if (op == Operations.WriteAC) {
                	return MifareClassicKeyType.B;
                }
                return null;
            } else if   (c1 == 1 && c2 == 1 && c3 == 1) {
                if (op == Operations.ReadAC) {
                	return MifareClassicKeyType.AorB;
                }
                return null;
            } else {
            	throw new IllegalArgumentException();
            }
        } else {
            // Data Block.
            if (op != Operations.Read && op != Operations.Write
                    && op != Operations.Increment
                    && op != Operations.DecTransRest) {
                // Error. Data block but no data block permissions.
            	throw new IllegalArgumentException();
            }
            if          (c1 == 0 && c2 == 0 && c3 == 0) {
                return (isKeyBReadable) ? MifareClassicKeyType.A : MifareClassicKeyType.AorB;
            } else if   (c1 == 0 && c2 == 1 && c3 == 0) {
                if (op == Operations.Read) {
                    return (isKeyBReadable) ? MifareClassicKeyType.A : MifareClassicKeyType.AorB;
                }
                return null;
            } else if   (c1 == 1 && c2 == 0 && c3 == 0) {
                if (op == Operations.Read) {
                    return (isKeyBReadable) ? MifareClassicKeyType.A : MifareClassicKeyType.AorB;
                }
                if (op == Operations.Write) {
                	return MifareClassicKeyType.B;
                }
                return null;
            } else if   (c1 == 1 && c2 == 1 && c3 == 0) {
                if (op == Operations.Read
                        || op == Operations.DecTransRest) {
                    return (isKeyBReadable) ? MifareClassicKeyType.A : MifareClassicKeyType.AorB;
                }
                return MifareClassicKeyType.B;
            } else if   (c1 == 0 && c2 == 0 && c3 == 1) {
                if (op == Operations.Read
                        || op == Operations.DecTransRest) {
                    return (isKeyBReadable) ? MifareClassicKeyType.A : MifareClassicKeyType.AorB;
                }
                return null;
            } else if   (c1 == 0 && c2 == 1 && c3 == 1) {
                if (op == Operations.Read || op == Operations.Write) {
                	return MifareClassicKeyType.B;
                }
                return null;
            } else if   (c1 == 1 && c2 == 0 && c3 == 1) {
                if (op == Operations.Read) {
                	return MifareClassicKeyType.B;
                }
                return null;
            } else if   (c1 == 1 && c2 == 1 && c3 == 1) {
                return null;
            } else {
                // Error.
                throw new IllegalArgumentException();
            }
        }
    }
    
    /**
     * Depending on the provided Access Conditions this method will return
     * with which key you can achieve the operation ({@link Operations})
     * you asked for.<br />
     * This method contains the table from the NXP Mifare Classic Datasheet.
     * @param c1 Access Condition byte "C1".
     * @param c2 Access Condition byte "C2".
     * @param c3 Access Condition byte "C3".
     * @param op The operation you want to do.
     * @param isSectorTrailer True if it is a Sector Trailer, False otherwise.
     * @param isKeyBReadable True if key B is readable, False otherwise.
     * @return The operation "op" is possible with:<br />
     * <ul>
     * <li>0 - Never.</li>
     * <li>1 - Key A.</li>
     * <li>2 - Key B.</li>
     * <li>3 - Key A or B.</li>
     * <li>-1 - Error.</li>
     * </ul>
     */

    public static int getOperationInfoForBlock(byte c1, byte c2, byte c3,
            Operations op, boolean isSectorTrailer, boolean isKeyBReadable) {
        // Is Sector Trailer?
        if (isSectorTrailer) {
            // Sector Trailer.
            if (op != Operations.ReadKeyA && op != Operations.ReadKeyB
                    && op != Operations.ReadAC
                    && op != Operations.WriteKeyA
                    && op != Operations.WriteKeyB
                    && op != Operations.WriteAC) {
                // Error. Sector Trailer but no Sector Trailer permissions.
                return 4;
            }
            if          (c1 == 0 && c2 == 0 && c3 == 0) {
                if (op == Operations.WriteKeyA
                        || op == Operations.WriteKeyB
                        || op == Operations.ReadKeyB
                        || op == Operations.ReadAC) {
                    return 1;
                }
                return 0;
            } else if   (c1 == 0 && c2 == 1 && c3 == 0) {
                if (op == Operations.ReadKeyB
                        || op == Operations.ReadAC) {
                    return 1;
                }
                return 0;
            } else if   (c1 == 1 && c2 == 0 && c3 == 0) {
                if (op == Operations.WriteKeyA
                        || op == Operations.WriteKeyB) {
                    return 2;
                }
                if (op == Operations.ReadAC) {
                    return 3;
                }
                return 0;
            } else if   (c1 == 1 && c2 == 1 && c3 == 0) {
                if (op == Operations.ReadAC) {
                    return 3;
                }
                return 0;
            } else if   (c1 == 0 && c2 == 0 && c3 == 1) {
                if (op == Operations.ReadKeyA) {
                    return 0;
                }
                return 1;
            } else if   (c1 == 0 && c2 == 1 && c3 == 1) {
                if (op == Operations.ReadAC) {
                    return 3;
                }
                if (op == Operations.ReadKeyA
                        || op == Operations.ReadKeyB) {
                    return 0;
                }
                return 2;
            } else if   (c1 == 1 && c2 == 0 && c3 == 1) {
                if (op == Operations.ReadAC) {
                    return 3;
                }
                if (op == Operations.WriteAC) {
                    return 2;
                }
                return 0;
            } else if   (c1 == 1 && c2 == 1 && c3 == 1) {
                if (op == Operations.ReadAC) {
                    return 3;
                }
                return 0;
            } else {
                return -1;
            }
        } else {
            // Data Block.
            if (op != Operations.Read && op != Operations.Write
                    && op != Operations.Increment
                    && op != Operations.DecTransRest) {
                // Error. Data block but no data block permissions.
                return -1;
            }
            if          (c1 == 0 && c2 == 0 && c3 == 0) {
                return (isKeyBReadable) ? 1 : 3;
            } else if   (c1 == 0 && c2 == 1 && c3 == 0) {
                if (op == Operations.Read) {
                    return (isKeyBReadable) ? 1 : 3;
                }
                return 0;
            } else if   (c1 == 1 && c2 == 0 && c3 == 0) {
                if (op == Operations.Read) {
                    return (isKeyBReadable) ? 1 : 3;
                }
                if (op == Operations.Write) {
                    return 2;
                }
                return 0;
            } else if   (c1 == 1 && c2 == 1 && c3 == 0) {
                if (op == Operations.Read
                        || op == Operations.DecTransRest) {
                    return (isKeyBReadable) ? 1 : 3;
                }
                return 2;
            } else if   (c1 == 0 && c2 == 0 && c3 == 1) {
                if (op == Operations.Read
                        || op == Operations.DecTransRest) {
                    return (isKeyBReadable) ? 1 : 3;
                }
                return 0;
            } else if   (c1 == 0 && c2 == 1 && c3 == 1) {
                if (op == Operations.Read || op == Operations.Write) {
                    return 2;
                }
                return 0;
            } else if   (c1 == 1 && c2 == 0 && c3 == 1) {
                if (op == Operations.Read) {
                    return 2;
                }
                return 0;
            } else if   (c1 == 1 && c2 == 1 && c3 == 1) {
                return 0;
            } else {
                // Error.
                return -1;
            }
        }
    }

    /**
     * Check if key B is readable.
     * Key B is readable for the following configurations:
     * <ul>
     * <li>C1 = 0, C2 = 0, C3 = 0</li>
     * <li>C1 = 0, C2 = 0, C3 = 1</li>
     * <li>C1 = 0, C2 = 1, C3 = 0</li>
     * </ul>
     * @param c1 Access Condition byte "C1"
     * @param c2 Access Condition byte "C2"
     * @param c3 Access Condition byte "C3"
     * @return True if key B is readable. False otherwise.
     */
    public static boolean isKeyBReadable(byte c1, byte c2, byte c3) {
        if (c1 == 0
                && (c2 == 0 && c3 == 0)
                || (c2 == 1 && c3 == 0)
                || (c2 == 0 && c3 == 1)) {
            return true;
        }
        return false;
    }
    
    /**
     * Check if key B is readable.
     * Key B is readable for the following configurations:
     * <ul>
     * <li>C1 = 0, C2 = 0, C3 = 0</li>
     * <li>C1 = 0, C2 = 0, C3 = 1</li>
     * <li>C1 = 0, C2 = 1, C3 = 0</li>
     * </ul>
     * @param ac The access conditions (4 bytes).
     * @return True if key B is readable. False otherwise.
     */
    public static boolean isKeyBReadable(byte[] ac) {
        byte c1 = (byte) ((ac[1] & 0x80) >>> 7);
        byte c2 = (byte) ((ac[2] & 0x08) >>> 3);
        byte c3 = (byte) ((ac[2] & 0x80) >>> 7);
        if (c1 == 0
                && (c2 == 0 && c3 == 0)
                || (c2 == 1 && c3 == 0)
                || (c2 == 0 && c3 == 1)) {
            return true;
        }
        return false;
    }

    /**
     * Convert the Access Condition bytes to a matrix containing the
     * resolved C1, C2 and C3 for each block.
     * @param ac The Access Conditions.
     * @return Matrix of access conditions bits (C1-C3) where the first
     * dimension is the "C" parameter (C1-C3, Index 0-2) and the second
     * dimension is the block number (Index 0-3).
     */
    public static byte[][] acToACMatrix(byte ac[]) {
        // ACs correct?
        // C1 (Byte 7, 4-7) == ~C1 (Byte 6, 0-3) and
        // C2 (Byte 8, 0-3) == ~C2 (Byte 6, 4-7) and
        // C3 (Byte 8, 4-7) == ~C3 (Byte 7, 0-3)
        byte[][] acMatrix = new byte[3][4];
        if ((byte)((ac[1]>>>4)&0x0F)  == (byte)((ac[0]^0xFF)&0x0F) &&
            (byte)(ac[2]&0x0F) == (byte)(((ac[0]^0xFF)>>>4)&0x0F) &&
            (byte)((ac[2]>>>4)&0x0F)  == (byte)((ac[1]^0xFF)&0x0F)) {
            // C1, Block 0-3
            for (int i = 0; i < 4; i++) {
                acMatrix[0][i] = (byte)((ac[1]>>>4+i)&0x01);
            }
            // C2, Block 0-3
            for (int i = 0; i < 4; i++) {
                acMatrix[1][i] = (byte)((ac[2]>>>i)&0x01);
            }
            // C3, Block 0-3
            for (int i = 0; i < 4; i++) {
                acMatrix[2][i] = (byte)((ac[2]>>>4+i)&0x01);
            }
            return acMatrix;
        }
        return null;
    }

    /**
     * Check if the given block (hex string) is a value block.
     * NXP has PDFs describing what value blocks are. Google something
     * like "nxp mifare classic value block" if you want to have a
     * closer look.
     * @param hexString Block data as hex string.
     * @return True if it is a value block. False otherwise.
     */
    public static boolean isValueBlock(byte[] b) {
        if (b.length == 16) {
            // Google some NXP info PDFs about Mifare Classic to see how
            // Value Blocks are formated.
            // For better reading (~ = invert operator):
            // if (b0=b8 and b0=~b4) and (b1=b9 and b9=~b5) ...
            // ... and (b12=b14 and b13=b15 and b12=~b13) then
            if (    (b[0] == b[8] && (byte)(b[0]^0xFF) == b[4]) &&
                    (b[1] == b[9] && (byte)(b[1]^0xFF) == b[5]) &&
                    (b[2] == b[10] && (byte)(b[2]^0xFF) == b[6]) &&
                    (b[3] == b[11] && (byte)(b[3]^0xFF) == b[7]) &&
                    (b[12] == b[14] && b[13] == b[15] &&
                    (byte)(b[12]^0xFF) == b[13])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reverse a byte Array (e.g. Little Endian -> Big Endian).
     * Hmpf! Java has no Array.reverse(). And I don't want to use
     * Commons.Lang (ArrayUtils) form Apache....
     * @param array The array to reverse (in-place).
     */
    public static void reverseByteArrasInPlace(byte[] array) {
        for(int i = 0; i < array.length / 2; i++) {
            byte temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }

    /**
     * This method checks if the present tag is writable with the provided keys
     * on the given positions (sectors, blocks). This is done by authenticating
     * with one of the keys followed by reading and interpreting
     * ({@link Common#getOperationInfoForBlock(byte, byte, byte,
     * de.syss.MifareClassicTool.Operations, boolean, boolean)}) of the
     * Access Conditions.
     * @param pos A map of positions (key = sector, value = Array of blocks).
     * For each of these positions you will get the write information
     * (see return values).
     * @param keyMap A key map a generated by
     * {@link Activities.CreateKeyMapActivity}.
     * @return A map within a map (all with type = Integer).
     * The key of the outer map is the sector number and the value is another
     * map with key = block number and value = write information.
     * The write information indicates which key is needed to write to the
     * present tag on the given position.<br /><br />
     * Write informations are:<br />
     * <ul>
     * <li>0 - Never</li>
     * <li>1 - Key A</li>
     * <li>2 - Key B</li>
     * <li>3 - Key A|B</li>
     * <li>4 - Key A, but AC never</li>
     * <li>5 - Key B, but AC never</li>
     * <li>6 - Key B, but keys never</li>
     * <li>-1 - Error</li>
     * <li>Inner map == null - Whole sector is dead (IO Error)</li>
     * </ul>
     * @throws IOException 
     */
    public HashMap<Integer, HashMap<Integer, Integer>> isWritableOnPositions(MifareClassic mifareClassic, 
            HashMap<Integer, int[]> pos,
            SparseArray<byte[][]> keyMap) throws IOException {
        HashMap<Integer, HashMap<Integer, Integer>> ret =
                new HashMap<Integer, HashMap<Integer,Integer>>();
        for (int i = 0; i < keyMap.size(); i++) {
            int sector = keyMap.keyAt(i);
            if (pos.containsKey(sector)) {
                byte[][] keys = keyMap.get(sector);
                byte[] ac = null;
                // Authenticate.
                if (keys[0] != null) {
                    if (authenticate(mifareClassic, sector, keys[0], false) == false) {
                        return null;
                    }
                } else if (keys[1] != null) {
                    if (authenticate(mifareClassic, sector, keys[1], true) == false) {
                        return null;
                    }
                } else {
                    return null;
                }
                
                // Read Mifare Access Conditions.
                int acBlock = mifareClassic.sectorToBlock(sector)
                        + mifareClassic.getBlockCountInSector(sector) -1;
                try {
                    ac = mifareClassic.readBlock(acBlock);
                } catch (IOException e) {
                    ret.put(sector, null);
                    continue;
                }
                ac = Arrays.copyOfRange(ac, 6, 9);
                byte[][] acMatrix = acToACMatrix(ac);
                boolean isKeyBReadable = isKeyBReadable(
                        acMatrix[0][3], acMatrix[1][3], acMatrix[2][3]);

                // Check all Blocks with data (!= null).
                HashMap<Integer, Integer> blockWithWriteInfo =
                        new HashMap<Integer, Integer>();
                for (int block : pos.get(sector)) {
                    if ((block == 3 && sector <= 31)
                            || (block == 15 && sector >= 32)) {
                        // Sector Trailer.
                        // Are the Access Bits writable?
                        int acValue = getOperationInfoForBlock(
                                acMatrix[0][3],
                                acMatrix[1][3],
                                acMatrix[2][3],
                                Operations.WriteAC,
                                true, isKeyBReadable);
                        // Is key A writable? (If so, key B will be writable
                        // with the same key.)
                        int keyABValue = getOperationInfoForBlock(
                                acMatrix[0][3],
                                acMatrix[1][3],
                                acMatrix[2][3],
                                Operations.WriteKeyA,
                                true, isKeyBReadable);

                        int result = keyABValue;
                        if (acValue == 0 && keyABValue != 0) {
                            // Write key found, but ac-bits are not writable.
                            result += 3;
                        } else if (acValue == 2 && keyABValue == 0) {
                            // Access Bits are writable with key B,
                            // but keys are not writable.
                            result = 6;
                        }
                        blockWithWriteInfo.put(block, result);
                    } else {
                        // Data block.
                        int acBitsForBlock = block;
                        // Handle Mifare Classic 4k Tags.
                        if (sector >= 32) {
                            if (block >= 0 && block <= 4) {
                                acBitsForBlock = 0;
                            } else if (block >= 5 && block <= 9) {
                                acBitsForBlock = 1;
                            } else if (block >= 10 && block <= 14) {
                                acBitsForBlock = 2;
                            }
                        }
                        blockWithWriteInfo.put(
                                block, getOperationInfoForBlock(
                                        acMatrix[0][acBitsForBlock],
                                        acMatrix[1][acBitsForBlock],
                                        acMatrix[2][acBitsForBlock],
                                        Operations.Write,
                                        false, isKeyBReadable));
                    }

                }
                if (blockWithWriteInfo.size() > 0) {
                    ret.put(sector, blockWithWriteInfo);
                }
            }
        }
        return ret;
    }

    /**
     * Authenticate to given sector of the tag.
     * @param sectorIndex The sector to authenticate to.
     * @param key Key for the authentication.
     * @param useAsKeyB If true, key will be treated as key B
     * for authentication.
     * @return True if authentication was successful. False otherwise.
     * @throws IOException 
     */
    private boolean authenticate(MifareClassic mifareClassic, int sectorIndex, byte[] key, boolean useAsKeyB) throws IOException {
        if (!useAsKeyB) {
            // Key A.
            return mifareClassic.authenticateSectorWithKeyA(sectorIndex, key);
        } else {
            // Key B.
            return mifareClassic.authenticateSectorWithKeyB(sectorIndex, key);
        }
    }

	public static int getIndex(byte[][] accessBits, int i) {
		int value = (accessBits[0][i] << 8) + (accessBits[1][i]  << 4) + accessBits[2][i];
		
		for(int k = 0; k < accessBits.length; k++) {
			if(value == MifareClassicUtils.accessBits[k]) {
				return i;
			}
		}
		
		return -1;
	}
	
	public static boolean isPermanentAccesBytes(byte[] accessBytes) {
		int[] accessBitsIndexes = getAccessBitsIndexes(accessBytes);
		
		for(int i = 0; i < accessBitsIndexes.length - 1; i++) {
			if(isPermanentDataIndex(accessBitsIndexes[i])) {
				return true;
			}
		}
		
		return isPermanentTrailerIndex(accessBitsIndexes[accessBitsIndexes.length - 1]);
	}

	public static boolean isPermanentTrailerAccesBytes(byte[] accessBytes) {
		byte[][] accessBits = acToACMatrix(accessBytes);

		return isPermanentTrailerIndex(getIndex(accessBits, 3));
	}

	public static int[] getAccessBitsIndexes(byte[] accessBytes) {
		byte[][] accessBits = acToACMatrix(accessBytes);
		
		int[] indexes = new int[4];

		for(int i = 0; i < 4; i++) {
			indexes[i] = getIndex(accessBits, i);
		}
		
		return indexes;
	}

	public static List<String> getAccessBitsList() {
		List<String> list = new ArrayList<String>();
		
		for(int i = 0; i < accessBits.length; i++) {
			list.add(getAccessBits(i));
		}
		
		return list;
	}

	public static String getAccessBits(int index) {
		StringBuffer stringBuffer = new StringBuffer();
    	stringBuffer.append(Integer.toHexString(MifareClassicUtils.accessBits[index]));
    	
    	int length = stringBuffer.length();
    	for(int i = 0; i < 3 - length; i++) {
    		stringBuffer.insert(0, '0');
    	}
    	return stringBuffer.toString();
	}

	public static boolean isBinary(String key) {
		for (int i = key.length() - 1; i >= 0; i--) {
    		final char c = key.charAt(i);
    		if (!(c >= '0' && c <= '1')) {
    			return false;
    		}
    	}

    	return true;
    }
	
	public static int getAccessBitsIndex(String bits) {
		for(int i = 0; i < accessBits.length; i++) {
			if(getAccessBits(i).equals(bits)) {
				return i;
			}
		}
		
		return -1;
	}
	
	private static byte encode(int ... bit) {
		
		for(int i = 0; i < bit.length; i++) {
			bit[i] = bit[i] & 0x1;
		}
		
		int encoded = 0;
		for(int i = 0; i < bit.length; i++) {
			if(bit[i] != 0) {
				encoded = encoded | (bit[i] << (bit.length - 1 - i));
			}
		}
		return (byte)encoded;
	}
	
	public static byte[] fromAccessBytesIndexesToAccessConditionBytes(int trailer, int data0, int data1, int data2) {
		return toAccessBytes(accessBits[trailer], accessBits[data0], accessBits[data1], accessBits[data2]);
	}

	public static byte[] toAccessBytes(int trailer, int data0, int data1, int data2) {
		
		int c13 = (trailer >> 8) & 0x1;
		int c23 = (trailer >> 4) & 0x1;
		int c33 = trailer & 0x1;
		
		int c12 = (data2 >> 8) & 0x1;
		int c22 = (data2 >> 4) & 0x1;
		int c32 = data2 & 0x1;

		int c11 = (data1 >> 8) & 0x1;
		int c21 = (data1 >> 4) & 0x1;
		int c31 = data1 & 0xF;

		int c10 = (data0 >> 8) & 0x1;
		int c20 = (data0 >> 4) & 0x1;
		int c30 = data1 & 0x1;

		return toAccessBytes(c13, c23, c33, c12, c22, c32, c11, c21, c31, c10, c20, c30);
	}

	public static byte[] toAccessBytes(int c13, int c23, int c33, int c12, int c22,
			int c32, int c11, int c21, int c31, int c10, int c20, int c30) {
		byte[] ac = new byte[]{
				encode(~c23, ~c22, ~c21, ~c20, ~c13, ~c12, ~c11, ~c10),
				encode(c13, c12, c11, c10, ~c33, ~c32, ~c31, ~c30),
				encode(c33, c32, c31, c30, c23, c22, c21, c20),
				0
		};
		
        validate(ac);
        
        return ac;
	}

	public static void validate(byte[] ac) {
		if ((byte)((ac[1]>>>4)&0x0F)  == (byte)((ac[0]^0xFF)&0x0F) &&
                (byte)(ac[2]&0x0F) == (byte)(((ac[0]^0xFF)>>>4)&0x0F) &&
                (byte)((ac[2]>>>4)&0x0F)  == (byte)((ac[1]^0xFF)&0x0F)) {
        } else {
        	throw new RuntimeException();
        }
	}

	public static byte[] or(byte[] a, byte[] b) {
		byte[] result = new byte[a.length];
		for(int i = 0; i < a.length; i++) {
			result[i] = (byte)(a[i] | b[i]);
		}
		return result;
	}

	public static boolean isKeyRequired(byte[] ac, boolean a, Operations operation) {
		byte[][] acMatrix = MifareClassicUtils.acToACMatrix(ac);
        boolean isKeyBReadable = MifareClassicUtils.isKeyBReadable(acMatrix[0][3], acMatrix[1][3], acMatrix[2][3]);
        
        // check that we keys corresponding to access conditions
        for(int acIndex = 0; acIndex < 3; acIndex++) {
        	int operationInfoForBlock = MifareClassicUtils.getOperationInfoForBlock(acMatrix[0][acIndex], acMatrix[1][acIndex], acMatrix[2][acIndex], operation, acIndex == 3, isKeyBReadable);
            
            if(a && operationInfoForBlock == 1) { // a and only a can read
            	return true;
            } else if(!a && operationInfoForBlock == 2) { // b and only b can read
            	return true;
            }
        }
        
        return false;
	}
}
