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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// http://www.codeproject.com/Articles/119293/Using-SQLite-Database-with-Android

public class MySQLiteHelper extends SQLiteOpenHelper {

	public static final String TAG_TABLE = "Tag";
	public static final String TAG_COLUMN_ID = "id";
	public static final String TAG_COLUMN_NAME = "name";
	public static final String TAG_COLUMN_TIME = "time";
	public static final String TAG_COLUMN_KEY_A = "aKey";
	public static final String TAG_COLUMN_KEY_B = "bKey";
	public static final String TAG_COLUMN_ACCESS_BITS_DATA_INDEX_0 = "ac0";
	public static final String TAG_COLUMN_ACCESS_BITS_DATA_INDEX_1 = "ac1";
	public static final String TAG_COLUMN_ACCESS_BITS_DATA_INDEX_2 = "ac2";
	public static final String TAG_COLUMN_ACCESS_BITS_TRAILER = "ac3";

	public static final String SECTOR_TABLE = "Sector";
	public static final String SECTOR_COLUMN_ID = "id";
	public static final String SECTOR_COLUMN_INDEX = "sectorIndex";
	public static final String SECTOR_COLUMN_KEY_A = "aKey";
	public static final String SECTOR_COLUMN_KEY_B = "bKey";
	public static final String SECTOR_COLUMN_TAG = "tagId";
	public static final String SECTOR_COLUMN_ACCESS_BITS_DATA_INDEX_0 = "ac0";
	public static final String SECTOR_COLUMN_ACCESS_BITS_DATA_INDEX_1 = "ac1";
	public static final String SECTOR_COLUMN_ACCESS_BITS_DATA_INDEX_2 = "ac2";
	public static final String SECTOR_COLUMN_ACCESS_BITS_TRAILER = "ac3";

	public static final String KEY_TABLE = "Key";
	public static final String KEY_COLUMN_ID = "id";
	public static final String KEY_COLUMN_NAME = "name";
	public static final String KEY_COLUMN_VALUE = "value";
	public static final String KEY_COLUMN_TIME = "time";

	private static final String DATABASE_CREATE_TAG_TABLE = "CREATE TABLE "
			+ TAG_TABLE+" ("+TAG_COLUMN_ID+ " INTEGER PRIMARY KEY, "
		    + TAG_COLUMN_NAME+ " TEXT, " 
		    + TAG_COLUMN_TIME + " INTEGER,"
		    + TAG_COLUMN_KEY_A + " INTEGER, " 
		    + TAG_COLUMN_KEY_B+ " INTEGER, "
		    + TAG_COLUMN_ACCESS_BITS_DATA_INDEX_0 + " INTEGER,"
		    + TAG_COLUMN_ACCESS_BITS_DATA_INDEX_1 + " INTEGER,"
		    + TAG_COLUMN_ACCESS_BITS_DATA_INDEX_2 + " INTEGER,"
		    + TAG_COLUMN_ACCESS_BITS_TRAILER + " INTEGER, "
		    
			+ "FOREIGN KEY ("+TAG_COLUMN_KEY_A+") REFERENCES " + KEY_TABLE +" ("+KEY_COLUMN_ID+"), " 
			+ "FOREIGN KEY ("+TAG_COLUMN_KEY_B+") REFERENCES " + KEY_TABLE +" ("+KEY_COLUMN_ID+") " 
    		+ ")";

	private static final String DATABASE_CREATE_KEY_TABLE = "CREATE TABLE "
			+ KEY_TABLE + " (" + KEY_COLUMN_ID+ " INTEGER PRIMARY KEY, "
		    + KEY_COLUMN_NAME + " TEXT, " 
		    + KEY_COLUMN_VALUE + " BLOB, " 
		    + KEY_COLUMN_TIME + " INTEGER"
    		+ ")";
	

	private static final String DATABASE_NAME = "mifareclassic.db";
	private static final int DATABASE_VERSION = 1;

	private static final String TAG = MySQLiteHelper.class.getName();
	
	private static final String DATABASE_CREATE_SECTOR_TABLE = "create table "
			+ SECTOR_TABLE + "( " + SECTOR_COLUMN_ID
			+ " integer primary key autoincrement, " 
			+ SECTOR_COLUMN_INDEX + " integer, " 
			+ SECTOR_COLUMN_KEY_A + " INTEGER, "
			+ SECTOR_COLUMN_KEY_B + " INTEGER," 
			+ SECTOR_COLUMN_TAG +" INTEGER NOT NULL, "
		    + SECTOR_COLUMN_ACCESS_BITS_DATA_INDEX_0 + " INTEGER,"
		    + SECTOR_COLUMN_ACCESS_BITS_DATA_INDEX_1 + " INTEGER,"
		    + SECTOR_COLUMN_ACCESS_BITS_DATA_INDEX_2 + " INTEGER,"
		    + SECTOR_COLUMN_ACCESS_BITS_TRAILER + " INTEGER, "
			
			+ "FOREIGN KEY ("+SECTOR_COLUMN_TAG+") REFERENCES " + TAG_TABLE +" ("+TAG_COLUMN_ID+"), " 
			+ "FOREIGN KEY ("+TAG_COLUMN_KEY_A+") REFERENCES " + KEY_TABLE +" ("+KEY_COLUMN_ID+"), " 
			+ "FOREIGN KEY ("+TAG_COLUMN_KEY_B+") REFERENCES " + KEY_TABLE +" ("+KEY_COLUMN_ID+") " 
			
			+ ");";

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.d(TAG, "onCreate");
		
		database.execSQL(DATABASE_CREATE_KEY_TABLE);
		
		database.execSQL(DATABASE_CREATE_TAG_TABLE);
		
		database.execSQL(DATABASE_CREATE_SECTOR_TABLE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion >= newVersion) {
			return;
		}
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	} 
	
	
	

}