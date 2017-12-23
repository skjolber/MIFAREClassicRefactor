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

import java.io.File;
import java.io.FileNotFoundException;
 
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
 
// http://stephendnicholas.com/archives/974
public class CachedFileProvider extends ContentProvider {
 
    private static final String TAG = CachedFileProvider.class.getName();
 
    // The authority is the symbolic name for the provider class
    public static final String AUTHORITY = "com.skjolberg.nfc.clone2.gmailattach.provider";
 
    // UriMatcher used to match against incoming requests
    private UriMatcher uriMatcher;
 
    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 
        // Add a URI to the matcher which will match against the form
        // 'content://com.stephendnicholas.gmailattach.provider/*'
        // and return 1 in the case that the incoming Uri matches this pattern
        uriMatcher.addURI(AUTHORITY, "*", 1);
 
        return true;
    }
 
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
 
        Log.v(TAG,
                "Called with uri: '" + uri + "'." + uri.getLastPathSegment());
 
        // Check incoming Uri against the matcher
        switch (uriMatcher.match(uri)) {
 
        // If it returns 1 - then it matches the Uri defined in onCreate
        case 1:
 
            // The desired file name is specified by the last segment of the
            // path
            // E.g.
            // 'content://com.stephendnicholas.gmailattach.provider/Test.txt'
            // Take this and build the path to the file
            String fileLocation = getContext().getCacheDir() + File.separator
                    + uri.getLastPathSegment();
 
            // Create & return a ParcelFileDescriptor pointing to the file
            // Note: I don't care what mode they ask for - they're only getting
            // read only
            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(new File(
                    fileLocation), ParcelFileDescriptor.MODE_READ_ONLY);
            return pfd;
 
            // Otherwise unrecognised Uri
        default:
            Log.v(TAG, "Unsupported uri: '" + uri + "'.");
            throw new FileNotFoundException("Unsupported uri: "
                    + uri.toString());
        }
    }
 
    // //////////////////////////////////////////////////////////////
    // Not supported / used / required for this example
    // //////////////////////////////////////////////////////////////
 
    @Override
    public int update(Uri uri, ContentValues contentvalues, String s,
            String[] as) {
        return 0;
    }
 
    @Override
    public int delete(Uri uri, String s, String[] as) {
        return 0;
    }
 
    @Override
    public Uri insert(Uri uri, ContentValues contentvalues) {
        return null;
    }
 
    @Override
    public String getType(Uri uri) {
        return null;
    }
 
    @Override
    public Cursor query(Uri uri, String[] projection, String s, String[] as1,
            String s1) {
        return null;
    }
}