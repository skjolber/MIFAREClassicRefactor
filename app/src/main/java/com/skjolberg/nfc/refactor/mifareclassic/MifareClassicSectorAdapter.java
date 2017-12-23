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
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.skjolberg.nfc.refactor.mifareclassic.data.MifareClassicSectorData;
import com.skjolberg.nfc.refactor.mifareclassic.data.MifareClassicTag;
import com.skjolberg.nfc.mifareclassic.R;

public class MifareClassicSectorAdapter extends ArrayAdapter<MifareClassicTag> {
	
	private static final String TAG = MifareClassicSectorAdapter.class.getName();
	
	private static final int[] block_resources = new int[]{R.id.block0, R.id.block1, R.id.block2, R.id.block3, R.id.block4, R.id.block5, R.id.block6, R.id.block7, R.id.block8, R.id.block9, R.id.block10, R.id.block11, R.id.block12, R.id.block13, R.id.block14, R.id.block15};
	
	private Context context;
	private MifareClassicTag tag;


	public MifareClassicSectorAdapter(Context context, MifareClassicTag tag) {
		super(context, R.layout.mifareclassic_sector_data);
		this.context = context;
		this.tag = tag;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view;
		if(tag.isCompressed() && tag.getSectors().size() == position) {
			
			view = inflater.inflate(R.layout.mifareclassic_sector_data_item, parent, false);
			
			TextView textView = (TextView) view.findViewById(R.id.value);
			textView.setText(R.string.mifareClassicBlankSectors);
		} else {
			view = inflater.inflate(R.layout.mifareclassic_sector_data, parent, false);
	
			MifareClassicSectorData mifareClassicSectorData = tag.get(position);
			
			TextView textView = (TextView) view.findViewById(R.id.sectorId);
			textView.setText(context.getString(R.string.mifareClassicSectorNumber, position));
			
			Log.d(TAG, "Sector " + position + " block count is " + mifareClassicSectorData.blockCount());
			
			for(int i = 0; i < mifareClassicSectorData.blockCount(); i++) {
				String blockString = mifareClassicSectorData.getBlockString(i);
				
				TextView v = (TextView) view.findViewById(block_resources[i]);
				if(i == mifareClassicSectorData.blockCount() - 1) {
					Spannable WordtoSpan = new SpannableString(blockString);        
					
					WordtoSpan.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 12 + 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

					WordtoSpan.setSpan(new ForegroundColorSpan(Color.GRAY), 12 + 5, 12 + 5 + 8 + 4 + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

					WordtoSpan.setSpan(new ForegroundColorSpan(Color.WHITE), blockString.length() - 12 - 5, blockString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

					v.setText(WordtoSpan);
				} else if(i == 0 && position == 0) {
					Spannable WordtoSpan = new SpannableString(blockString);        

					WordtoSpan.setSpan(new ForegroundColorSpan(Color.RED), 0, blockString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					
					v.setText(WordtoSpan);
				} else {
					v.setText(blockString);
					
				}
			}
			
			for(int i = mifareClassicSectorData.blockCount(); i < 16; i++) {
				View v = view.findViewById(block_resources[i]);
				v.setVisibility(View.GONE);
			}
		}		
		return view;
	}
	
	@Override
	public int getCount() {
		int sectorCount = tag.getSectorCount();
		
		if(tag.isCompressed()) {
			sectorCount++;
		}
		return sectorCount;
	}

	
}