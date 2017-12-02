package com.skjolberg.nfc.clone.mifareclassic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.tech.MifareClassic;
import android.preference.PreferenceManager;
import android.util.Log;

import com.skjolberg.nfc.clone.desfire.Utils;
import com.skjolberg.nfc.mifareclassic.R;

public class MifareClassicDataSource {

	private static final String TAG = MifareClassicDataSource.class.getName();

	public static final String DEFAULT_KEYS = "datasource:mifareclassic:defaultKeys";

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;

	private String[] allTagColumns = { MySQLiteHelper.TAG_COLUMN_ID, MySQLiteHelper.TAG_COLUMN_NAME, MySQLiteHelper.TAG_COLUMN_TIME, MySQLiteHelper.TAG_COLUMN_KEY_A, MySQLiteHelper.TAG_COLUMN_KEY_B, MySQLiteHelper.TAG_COLUMN_ACCESS_BITS_DATA_INDEX_0, MySQLiteHelper.TAG_COLUMN_ACCESS_BITS_DATA_INDEX_1, MySQLiteHelper.TAG_COLUMN_ACCESS_BITS_DATA_INDEX_2, MySQLiteHelper.TAG_COLUMN_ACCESS_BITS_TRAILER};
	private String[] allSectorColumns = {MySQLiteHelper.SECTOR_COLUMN_ID, MySQLiteHelper.SECTOR_COLUMN_INDEX, MySQLiteHelper.SECTOR_COLUMN_KEY_A, MySQLiteHelper.SECTOR_COLUMN_KEY_B, MySQLiteHelper.SECTOR_COLUMN_TAG, MySQLiteHelper.TAG_COLUMN_ACCESS_BITS_DATA_INDEX_0, MySQLiteHelper.TAG_COLUMN_ACCESS_BITS_DATA_INDEX_1, MySQLiteHelper.TAG_COLUMN_ACCESS_BITS_DATA_INDEX_2, MySQLiteHelper.TAG_COLUMN_ACCESS_BITS_TRAILER};
	private String[] allKeyColumns = { MySQLiteHelper.KEY_COLUMN_ID, MySQLiteHelper.KEY_COLUMN_NAME, MySQLiteHelper.KEY_COLUMN_VALUE, MySQLiteHelper.KEY_COLUMN_TIME};
	
	private List<MifareClassicScheme<MifareClassicKey>> tagList;
	private Map<Long, MifareClassicScheme<MifareClassicKey>> tags = new ConcurrentHashMap<Long, MifareClassicScheme<MifareClassicKey>>();
	
	private List<MifareClassicKey> keyList;
	private Map<Long, MifareClassicKey> keys = new ConcurrentHashMap<Long, MifareClassicKey>();

	public MifareClassicDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
		open();
		handleDefaultKeys(context);
	}
	
	private void addTag(MifareClassicScheme<MifareClassicKey> tag) {
		tags.put(tag.getId(), tag);
	}

	private void addKey(MifareClassicKey key) {
		keys.put(key.getId(), key);
	}

	private void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
		
		//verify();
	}
	
	public void verify() {
		if(!verifyColumns(MySQLiteHelper.KEY_TABLE, allKeyColumns)) {
			Log.w(TAG, "Key table not verified");
		}
		if(!verifyColumns(MySQLiteHelper.TAG_TABLE, allTagColumns)) {
			Log.w(TAG, "Tag table not verified");
		}
		if(!verifyColumns(MySQLiteHelper.SECTOR_TABLE, allSectorColumns)) {
			Log.w(TAG, "Sector table not verified");
		}
	}

	public boolean verifyColumns(String table, String[] columns) {

		List<String> expected = new ArrayList<String>();
		for(String column : columns) {
			expected.add(column);
		}
		List<String> actual = new ArrayList<String>();

		try {
			Cursor c = database.query(table, null, null, null, null, null, null);
			if (c != null) {
				int num = c.getColumnCount();
				for (int i = 0; i < num; ++i) {
					String colname = c.getColumnName(i);

					actual.add(colname);
				}
			}
		} catch (Exception e) {
			Log.v(MySQLiteHelper.KEY_TABLE, e.getMessage(), e);
		}
		
		if(actual.size() != expected.size()) {
			Log.w(TAG, "Expected " + expected + ", got " + actual);
			return false;
		}
		
		Collections.sort(expected);
		Collections.sort(actual);

		for(int i = 0; i < actual.size(); i++) {
			if(!actual.get(i).equals(expected.get(i))) {
				Log.w(TAG, "Expected " + expected.get(i) + ", got " + actual.get(i));
				
				return false;
			}
		}
		
		return true;
	}
	
	public void loadAll() {
		loadKeys();

		loadTags();
				
		getAllSectors();

	}

	private void loadTags() {
		tagList = getAllTags();
		for(MifareClassicScheme tag : tagList) {
			tags.put(tag.getId(), tag);
		}
		Collections.sort(tagList);
	}

	public void loadKeys() {
		keyList = getAllKeys();
		for(MifareClassicKey key : keyList) {
			keys.put(key.getId(), key);
		}
		Collections.sort(keyList);
	}
	
	public void close() {
		Log.d(TAG, "Close database");
		
		dbHelper.close();
	}

	public boolean createKey(MifareClassicKey entity) {
		long result = createKeyImpl(entity);
		if(result == -1) {
			Log.w(TAG, "Unable to insert user " + entity.getName());
			
			return false;
		} else {
			entity.setId(result);
			
			keys.put(result, entity);
			keyList.add(entity);
			return true;
		}
	}

	private long createKeyImpl(MifareClassicKey entity) {
		Log.d(TAG, "Insert key " + entity.getName() + " value " + Utils.getHexString(entity.getValue()) + " to database");
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.KEY_COLUMN_NAME, entity.getName());
		values.put(MySQLiteHelper.KEY_COLUMN_VALUE, entity.getValue());
		values.put(MySQLiteHelper.KEY_COLUMN_TIME, entity.getTime());
		
		long result = database.insert(MySQLiteHelper.KEY_TABLE, null, values);
		return result;
	}
	
	public List<MifareClassicKey> getAllKeys() {
		List<MifareClassicKey> keys = new ArrayList<MifareClassicKey>();

		Cursor cursor = database.query(MySQLiteHelper.KEY_TABLE, allKeyColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			keys.add(cursorToKey(cursor));
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		
		Log.d(TAG, "Got " + keys.size() + " keys");

		return keys;
	}

	private MifareClassicKey cursorToKey(Cursor cursor) {
		MifareClassicKey item = new MifareClassicKey();
		
		// id, name, value, time, tag id
		item.setId(cursor.getLong(0)); // autogenerated id
		item.setName(cursor.getString(1));
		item.setValue(cursor.getBlob(2));
		item.setTime(cursor.getInt(3));
		
		return item;
	}
	
	public boolean updateKey(MifareClassicKey key) {
	    ContentValues args = new ContentValues();
	    args.put(MySQLiteHelper.KEY_COLUMN_NAME, key.getName());
	    args.put(MySQLiteHelper.KEY_COLUMN_VALUE, key.getValue());
	    int rows = database.update(MySQLiteHelper.KEY_TABLE, args, MySQLiteHelper.KEY_COLUMN_ID + "=" + key.getId(), null);
	    
	    return rows > 0;
	}
	
	public boolean deleteKey(MifareClassicKey key) {
	    int rows = database.delete(MySQLiteHelper.KEY_TABLE, MySQLiteHelper.KEY_COLUMN_ID + "= ?", new String[]{Long.toString(key.getId())});
		
	    return rows > 0;
	}

	public void deleteAllKeys() {
		int result  = database.delete(MySQLiteHelper.KEY_TABLE, null, null);
		if(result <= 0) {
			Log.d(TAG, "Unable to delete all tags from database");
		}
	}

	// ******************* tag *********************
	
	public boolean createTag(MifareClassicScheme<MifareClassicKey> tag) {
		Log.d(TAG, "Insert tag  " + tag.getName() + " to database");
		ContentValues values = new ContentValues();

		values.put(MySQLiteHelper.TAG_COLUMN_TIME, tag.getTime().getTime());

		setTagValues(tag, values);

		long result = database.insert(MySQLiteHelper.TAG_TABLE, null, values);
		if(result == -1) {
			Log.w(TAG, "Unable to insert user " + tag.getName());
			
			return false;
		} else {
			tag.setId(result);
			tagList.add(tag);
			tags.put(result, tag);
			
			Collections.sort(tagList);
			
			return true;
		}
	}
	
	public List<MifareClassicScheme<MifareClassicKey>> getAllTags() {
		List<MifareClassicScheme<MifareClassicKey>> tags = new ArrayList<MifareClassicScheme<MifareClassicKey>>();

		Cursor cursor = database.query(MySQLiteHelper.TAG_TABLE, allTagColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			MifareClassicScheme<MifareClassicKey> user = cursorToTag(cursor);
			tags.add(user);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		
		Log.d(TAG, "Got " + tags.size() + " tags");

		return tags;
	}

	private MifareClassicScheme<MifareClassicKey> cursorToTag(Cursor cursor) {
		MifareClassicScheme<MifareClassicKey> tag = new MifareClassicScheme<MifareClassicKey>();
		
		// id, name, time
		tag.setId(cursor.getLong(0)); // autogenerated id
		tag.setName(cursor.getString(1));
		tag.setTime(new Date(cursor.getLong(2)));
		
		tag.setAKey(findKey(cursor.getLong(3)));
		tag.setBKey(findKey(cursor.getLong(4)));
		
		tag.setAccessBitsDataIndex0(cursor.getInt(5));
		tag.setAccessBitsDataIndex1(cursor.getInt(6));
		tag.setAccessBitsDataIndex2(cursor.getInt(7));
		tag.setAccessBitsTrailerIndex(cursor.getInt(8));
		return tag;
	}
	
	private MifareClassicKey findKey(long id) {
		for(MifareClassicKey key : keyList) {
			if(key.getId() == id) {
				return key;
			}
		}
		return null;
	}

	public boolean updateTag(MifareClassicScheme<MifareClassicKey> tag) {
		Log.d(TAG, "Update scheme");
		
	    ContentValues args = new ContentValues();
	    setTagValues(tag, args);

	    int rows = database.update(MySQLiteHelper.TAG_TABLE, args, MySQLiteHelper.TAG_COLUMN_ID + "=" + tag.getId(), null);
	    
	    return rows > 0;
	}

	private void setTagValues(MifareClassicScheme<MifareClassicKey> tag, ContentValues args) {
		args.put(MySQLiteHelper.TAG_COLUMN_NAME, tag.getName());
		if(tag.hasKeyA()) {
			args.put(MySQLiteHelper.TAG_COLUMN_KEY_A, tag.getAKey().getId());
		}
		if(tag.hasKeyB()) {
			args.put(MySQLiteHelper.TAG_COLUMN_KEY_B, tag.getBKey().getId());
		}
	    args.put(MySQLiteHelper.TAG_COLUMN_ACCESS_BITS_DATA_INDEX_0, tag.getAccessBitsDataIndex0());
	    args.put(MySQLiteHelper.TAG_COLUMN_ACCESS_BITS_DATA_INDEX_1, tag.getAccessBitsDataIndex1());
	    args.put(MySQLiteHelper.TAG_COLUMN_ACCESS_BITS_DATA_INDEX_2, tag.getAccessBitsDataIndex2());
	    args.put(MySQLiteHelper.TAG_COLUMN_ACCESS_BITS_TRAILER, tag.getAccessBitsTrailerIndex());
	}	
	
	public boolean deleteTag(MifareClassicScheme<MifareClassicKey> scheme) {
		
		List<MifareClassicSector<MifareClassicKey>> sectors = new ArrayList<>(scheme.getSectors());
		for(MifareClassicSector<MifareClassicKey> mifareClassicSector : sectors) {
			deleteSector(mifareClassicSector);
		}
		
	    int rows = database.delete(MySQLiteHelper.TAG_TABLE, MySQLiteHelper.TAG_COLUMN_ID + "= ?", new String[]{Long.toString(scheme.getId())});
		
	    tags.remove(scheme.getId());
	    tagList.remove(scheme);
	    
	    return rows > 0;
	}
	
	public void deleteAllTags() {
		int result  = database.delete(MySQLiteHelper.TAG_TABLE, null, null);
		if(result <= 0) {
			Log.d(TAG, "Unable to delete all tags from database");
		}
		
	    tags.clear();
	    tagList.clear();

	}

	// ***************** sector ******************
	
	public boolean createSector(MifareClassicSector<MifareClassicKey> user) {
		Log.d(TAG, "Insert sector index " + user.getIndex() + " for tag "+ user.getScheme().getName() + " to database");
		ContentValues values = new ContentValues();
		
		setSectorValues(user, values);

		values.put(MySQLiteHelper.SECTOR_COLUMN_TAG, user.getScheme().getId());

		long result = database.insert(MySQLiteHelper.SECTOR_TABLE, null, values);
		if(result == -1) {
			Log.w(TAG, "Unable to insert sector index " + user.getIndex() + " for tag "+ user.getScheme().getName());
			
			return false;
		} else {
			user.setId(result);
			
			user.getScheme().add(user);

			Log.w(TAG, "Inserted sector index " + user.getIndex() + " for tag "+ user.getScheme().getName());

			return true;
		}	
	}
	
	public List<MifareClassicSector<MifareClassicKey>> getAllSectors() {
		List<MifareClassicSector<MifareClassicKey>> sectors = new ArrayList<MifareClassicSector<MifareClassicKey>>();

		Cursor cursor = database.query(MySQLiteHelper.SECTOR_TABLE, allSectorColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			MifareClassicSector<MifareClassicKey> item = cursorToSector(cursor);
			sectors.add(item);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		
		Log.d(TAG, "Got " + sectors.size() + " sectors");

		return sectors;
	}

	private MifareClassicSector<MifareClassicKey> cursorToSector(Cursor cursor) {
		MifareClassicSector<MifareClassicKey> sector = new MifareClassicSector<MifareClassicKey>();
		
		// id, colum index, a key id, b key id, tag id
		sector.setId(cursor.getLong(0)); // autogenerated id
		sector.setIndex(cursor.getInt(1));
		if(!cursor.isNull(2)) {
			sector.setAKey(findKey(cursor.getLong(2)));
		}
		if(!cursor.isNull(3)) {
			sector.setBKey(findKey(cursor.getLong(3)));
		}
		
		MifareClassicScheme<MifareClassicKey> mifareClassicTag = tags.get(cursor.getLong(4));
		sector.setScheme(mifareClassicTag);
		mifareClassicTag.add(sector);
		
		sector.setAccessBitsDataIndex0(cursor.getInt(5));
		sector.setAccessBitsDataIndex1(cursor.getInt(6));
		sector.setAccessBitsDataIndex2(cursor.getInt(7));
		sector.setAccessBitsTrailerIndex(cursor.getInt(8));

		return sector;
	}
	
	public boolean updateSector(MifareClassicSector<MifareClassicKey> sector) {
	    ContentValues values = new ContentValues();
	    
		setSectorValues(sector, values);

	    int rows = database.update(MySQLiteHelper.SECTOR_TABLE, values, MySQLiteHelper.SECTOR_COLUMN_ID + "=" + sector.getId(), null);
	    
	    return rows > 0;
	}

	private void setSectorValues(MifareClassicSector<MifareClassicKey> sector,
			ContentValues values) {
		values.put(MySQLiteHelper.SECTOR_COLUMN_INDEX, sector.getIndex());
		if(sector.hasAKey()) {
			values.put(MySQLiteHelper.SECTOR_COLUMN_KEY_A, sector.getAKey().getId());
		}
		
		if(sector.hasBKey()) {
			values.put(MySQLiteHelper.SECTOR_COLUMN_KEY_B, sector.getBKey().getId());
		}
		
		values.put(MySQLiteHelper.TAG_COLUMN_ACCESS_BITS_DATA_INDEX_0, sector.getAccessBitsDataIndex0());
		values.put(MySQLiteHelper.TAG_COLUMN_ACCESS_BITS_DATA_INDEX_1, sector.getAccessBitsDataIndex1());
		values.put(MySQLiteHelper.TAG_COLUMN_ACCESS_BITS_DATA_INDEX_2, sector.getAccessBitsDataIndex2());
		values.put(MySQLiteHelper.TAG_COLUMN_ACCESS_BITS_TRAILER, sector.getAccessBitsTrailerIndex());
	}	
	
	public boolean deleteSector(MifareClassicSector<MifareClassicKey> sector) {
	    int rows = database.delete(MySQLiteHelper.SECTOR_TABLE, MySQLiteHelper.SECTOR_COLUMN_ID + "= ?", new String[]{Long.toString(sector.getId())});
		
	    if(rows > 0) {
	    	
	    	sector.getScheme().remove(sector);
	    	sector.setScheme(null);

			Log.d(TAG, "Deleted sector " + sector.getId());

	    	return true;
	    }
	    return false;
	}

	public void deleteAllSectors() {
		int result  = database.delete(MySQLiteHelper.SECTOR_TABLE, null, null);
		if(result <= 0) {
			Log.d(TAG, "Unable to delete all tags from database");
		} else {
			for(MifareClassicScheme<MifareClassicKey> mifareClassicTag : tagList) {
				mifareClassicTag.clearSectors();
			}
		}
	}

	//*************************************************

	public List<MifareClassicScheme<MifareClassicKey>> getTags() {
		return tagList;
	}
	
	public List<MifareClassicKey> getKeys() {
		return keyList;
	}

	public void deleteAll() {
		deleteAllSectors();
		deleteAllTags();
		deleteAllKeys();
	}
	
	public boolean isKeyInUse(long id) {
		Log.d(TAG, "Check use for key " + id);
		
		String [] selectionArgs = {String.valueOf(Long.toString(id)), String.valueOf(Long.toString(id))};
		
		Cursor cursor = database.query(MySQLiteHelper.SECTOR_TABLE, new String[]{ MySQLiteHelper.SECTOR_COLUMN_ID }, MySQLiteHelper.SECTOR_COLUMN_KEY_A + " = ? or " + MySQLiteHelper.SECTOR_COLUMN_KEY_B + " = ?", selectionArgs, null, null, null);
	    int cnt = cursor.getCount();
	    cursor.close();
	    if(cnt > 0) {
	    	return true;
	    }
	    
		cursor = database.query(MySQLiteHelper.TAG_TABLE, new String[]{ MySQLiteHelper.TAG_COLUMN_ID }, MySQLiteHelper.TAG_COLUMN_KEY_A + " = ? or " + MySQLiteHelper.TAG_COLUMN_KEY_B + " = ?", selectionArgs, null, null, null);
	    cnt = cursor.getCount();
	    cursor.close();
	    return cnt > 0;
	}
	
	public List<MifareClassicSector<MifareClassicKey>> getSectorsForTag(long id) {
		List<MifareClassicSector<MifareClassicKey>> keys = new ArrayList<MifareClassicSector<MifareClassicKey>>();

		String [] selectionArgs = {String.valueOf(Long.toString(id))};
		
		Cursor cursor = database.query(MySQLiteHelper.SECTOR_TABLE, allSectorColumns, MySQLiteHelper.SECTOR_COLUMN_TAG + " = ?", selectionArgs, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			keys.add(cursorToSector(cursor));
			
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		
		Log.d(TAG, "Got " + keys.size() + " sectors for tag " + id);

		return keys;
	}
	// rawQuery("select * from todo where _id = ?", new String[] { id }); 

	public boolean hasTags() {
		return !tagList.isEmpty() ;
	}

	public boolean deleteTag(int index) {
		return deleteTag(tagList.get(index));
	}

	public MifareClassicScheme<MifareClassicKey> getTag(int index) {
		return tagList.get(index);
	}

	public List<String> getKeyValuesAsStrings() {
		List<String> keys = new ArrayList<String>();
		
		for(MifareClassicKey key : keyList) {
			keys.add(Utils.getHexString(key.getValue()));
		}
		
		Collections.sort(keys);
		
		return keys;
	}

	private void handleDefaultKeys(Context context) {
		Log.d(getClass().getName(), "Load preferences");
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		if(!prefs.getBoolean(DEFAULT_KEYS, false)) {
			Log.d(getClass().getName(), "Insert default keys");
			
			Editor edit = prefs.edit();

			edit.putBoolean(DEFAULT_KEYS, true);
			edit.commit();
			
			MifareClassicKey defaultKey = new MifareClassicKey();
			defaultKey.setName(context.getString(R.string.mifareClassicKeyDefault));
			defaultKey.setValue(MifareClassic.KEY_DEFAULT);
			
			createKeyImpl(defaultKey);
			
			MifareClassicKey nfcForumKey = new MifareClassicKey();
			nfcForumKey.setName(context.getString(R.string.mifareClassicKeyNfcForumDefault));
			nfcForumKey.setValue(MifareClassic.KEY_NFC_FORUM);
			
			createKeyImpl(nfcForumKey);

			MifareClassicKey mifareApplicationDirectoryKey = new MifareClassicKey();
			mifareApplicationDirectoryKey.setName(context.getString(R.string.mifareClassicKeyMAD));
			mifareApplicationDirectoryKey.setValue(MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY);

			createKeyImpl(mifareApplicationDirectoryKey);
			
			Log.d(getClass().getName(), "Inserted default keys");
		}
				
	}

	public MifareClassicKey refresh(MifareClassicKey key) {
		return keys.get(key.getId());
	}
}