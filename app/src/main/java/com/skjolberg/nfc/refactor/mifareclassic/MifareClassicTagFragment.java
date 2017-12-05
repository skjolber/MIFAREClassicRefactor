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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.skjolberg.nfc.mifareclassic.R;

public final class MifareClassicTagFragment extends Fragment implements OnItemClickListener {
	
    private static final String TAG = MifareClassicTagFragment.class.getName();

    public static MifareClassicTagFragment newInstance(MifareClassicScheme<MifareClassicKey> tag, MifareClassicDataSource dataSource) {
        MifareClassicTagFragment fragment = new MifareClassicTagFragment();

        fragment.setTag(tag);
        fragment.setDataSource(dataSource);
        
        return fragment;
    }
    
	private MifareClassicScheme<MifareClassicKey> scheme;
    private LayoutInflater mInflater;
    private KeysAdapter mAdapter;
    private MifareClassicDataSource dataSource;
    
    private MifareClassicUI mifareClassicUI;
    
    private class KeysAdapter extends ArrayAdapter<Object> {
      	 
        public KeysAdapter(Context context) {
            super(context, 0);
        }
 
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            if(position >= 0 && position <= 1) {
                view = mInflater.inflate(R.layout.mifareclassic_tag_key_item, null);

            	TextView key = (TextView) view.findViewById(R.id.key);
            	TextView value = (TextView) view.findViewById(R.id.value);

            	MifareClassicKey mifareClassicKey;
                if(position == 0) {
                	key.setText(R.string.a);
                	mifareClassicKey = scheme.getAKey();
                } else {
                	key.setText(R.string.b);
                	mifareClassicKey = scheme.getBKey();
                }
                if(mifareClassicKey != null) {
                	value.setText(mifareClassicKey.getName());
                } else {
                	value.setText(getString(R.string.none));
                }
            } else if(position >= 2 && position <= 3) {
                view = mInflater.inflate(R.layout.mifareclassic_tag_accessbits_item, null);

            	TextView key = (TextView) view.findViewById(R.id.key);
            	TextView value = (TextView) view.findViewById(R.id.value);

            	int accessBits;
                if(position == 2) {
                	key.setText(R.string.defaultAccessBitsTrailer);
                	accessBits = scheme.getAccessBitsIndex(3);
                } else {
                	key.setText(R.string.defaultAccessBitsData);
                	accessBits = scheme.getAccessBitsIndex(0);
                }
                if(accessBits != -1) {
                	value.setText(MifareClassicUtils.getAccessBits(accessBits));
                } else {
                	value.setText(getString(R.string.none));
                }
            } else if(position == getCount() -1) {
                view = mInflater.inflate(R.layout.mifareclassic_tag_sector_add, null);
            } else {
                view = mInflater.inflate(R.layout.mifareclassic_tag_sector_item, null);
                
                position -= 4;
                
                MifareClassicSector<MifareClassicKey> mifareClassicSector = scheme.getSectors().get(position);

            	TextView sectorValue = (TextView) view.findViewById(R.id.sectorValue);
            	sectorValue.setText(Integer.toString(mifareClassicSector.getIndex()));

            	TextView aKeyValue = (TextView) view.findViewById(R.id.aKeyValue);
            	TextView bKeyValue = (TextView) view.findViewById(R.id.bKeyValue);
            	
                if(mifareClassicSector.hasAKey()) {
                	aKeyValue.setText(mifareClassicSector.getAKey().getName());
                } else {
                	if(scheme.getAKey() != null) {
                		aKeyValue.setText(R.string.noDefaultDefault);
                	} else {
                		aKeyValue.setText(R.string.noDefaultSource);
                	}
                }
                
                if(mifareClassicSector.hasBKey()) {
                	bKeyValue.setText(mifareClassicSector.getBKey().getName());
                } else {
                	if(scheme.getBKey() != null) {
                		bKeyValue.setText(R.string.noDefaultDefault);
                	} else {
                		bKeyValue.setText(R.string.noDefaultSource);
                	}
                }

            	TextView trailer = (TextView) view.findViewById(R.id.accessBitsTrailerValue);
                if(mifareClassicSector.getAccessBitsTrailerIndex() != -1) {
                	trailer.setText(MifareClassicUtils.getAccessBits(mifareClassicSector.getAccessBitsTrailerIndex()));
                } else {
                	if(scheme.getAccessBitsTrailerIndex() != -1) {
                		trailer.setText(R.string.noDefaultDefault);
                	} else {
                		trailer.setText(R.string.noDefaultSource);
                	}
                }
                
            	TextView data = (TextView) view.findViewById(R.id.accessBitsDataValue);
                if(mifareClassicSector.getAccessBitsDataIndex0() != -1) {
                	data.setText(MifareClassicUtils.getAccessBits(mifareClassicSector.getAccessBitsDataIndex0()));
                } else {
                	if(scheme.getAccessBitsDataIndex0() != -1) {
                		data.setText(R.string.noDefaultDefault);
                	} else {
                		data.setText(R.string.noDefaultSource);
                	}
                }
            	
            }
            
            return view;
        }

        @Override
        public int getCount() {
        	return 2 + 2 + 1 + scheme.getSectors().size();
        }
 
    }
	public void setTag(MifareClassicScheme<MifareClassicKey> tag) {
		this.scheme = tag;
	}

    private void setDataSource(MifareClassicDataSource dataSource) {
    	this.dataSource = dataSource;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInflater = LayoutInflater.from(getActivity());

        mAdapter = new KeysAdapter(getActivity());
        
        mifareClassicUI = new MifareClassicUI(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	View view = inflater.inflate(R.layout.mifareclassic_tag, null);
    	
    	ListView listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
    	
    	TextView name = (TextView) view.findViewById(R.id.title);
    	if(scheme.hasName()) {
        	name.setText(scheme.getName());
    	}

		refreshFooter(view);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
    }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
		Log.d(TAG, "Click on key " + index);
		
		if(index <= 1) {
			showDefaultKeyDialog(index == 0);
        } else if(index <= 3) {
            if(index == 2) {
                showAccessConditionsActivity(3,  scheme.getAccessBitsIndex(3));
            } else {
                showAccessConditionsActivity(0,  scheme.getAccessBitsIndex(0));
            }
        } else if(index == mAdapter.getCount() -1) {
        	showSectorDialog(-1);
		} else {
			index -= 4;
			
			showSectorDialog(index);
		}
	}
	private void showSectorDialog(int index) {
		Intent i = new Intent(getActivity(), MifareClassicSectorActivity.class);

		if(index != -1) {
			i.putExtra(MifareClassicSectorActivity.MIFARE_CLASSIC_SECTOR_KEY, scheme.getSector(index));
			i.putExtra(MifareClassicSectorActivity.MIFARE_CLASSIC_SECTOR_INDEX_KEY, index);			
		}
		startActivityForResult(i, MifareClassicSectorActivity.MIFARE_CLASSIC_SECTOR);
	}

	public int getPx(int dp) {
		float scale = getResources().getDisplayMetrics().density;
		return ((int) (dp * scale + 0.5f));
	}
	
	private void showDefaultKeyDialog(final boolean alpha) {
		
		List<String> keys = new ArrayList<String>();
		keys.add(getString(R.string.noKey));

		final List<MifareClassicKey> allKeys = dataSource.getAllKeys();
		for(MifareClassicKey key : allKeys) {
			keys.add(key.getName() + " (" + Utils.getHexString(key.getValue(), true) + ")");
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.keySelect);
		builder.setCancelable(true);
		
		builder.setItems(keys.toArray(new CharSequence[keys.size()]), new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	item--;

		    	MifareClassicKey key = null;
		    	if(item != -1) {
		    		key = allKeys.get(item);
		    	}
		    	
	    		if(alpha) {
            		scheme.setAKey(key);
            	} else {
            		scheme.setBKey(key);
            	}
               	dataSource.updateTag(scheme);
				
               	mAdapter.notifyDataSetInvalidated();

        		refreshFooter(getView());
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void showAccessConditionsActivity(int type, int index) {
		Intent i = new Intent(getActivity(), MifareClassicAccessConditionActivity.class);
		
		i.putExtra(MifareClassicAccessConditionActivity.KEY_ACCESS_BITS_TYPE_INDEX, type);
		i.putExtra(MifareClassicAccessConditionActivity.KEY_ACCESS_BITS_INDEX, index);
		
		startActivityForResult(i, MifareClassicAccessConditionActivity.MIFARE_CLASSIC_ACCESS_CONDITION);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult");
		
		if(requestCode == MifareClassicAccessConditionActivity.MIFARE_CLASSIC_ACCESS_CONDITION) {
			if(resultCode == Activity.RESULT_OK) {
				Bundle extras = data.getExtras();
				
				int accessBitTypeIndex = extras.getInt(MifareClassicAccessConditionActivity.KEY_ACCESS_BITS_TYPE_INDEX);
				int accessBitIndex = extras.getInt(MifareClassicAccessConditionActivity.KEY_ACCESS_BITS_INDEX);

				Log.d(TAG, "Access condition " + accessBitTypeIndex + " index " + accessBitIndex);
				
				if(accessBitTypeIndex == 3) {
					scheme.setAccessBits(accessBitTypeIndex, accessBitIndex);
				} else {
					scheme.setAccessBits(0, accessBitIndex);
					scheme.setAccessBits(1, accessBitIndex);
					scheme.setAccessBits(2, accessBitIndex);
				}
				
				dataSource.updateTag(scheme);
				
				mAdapter.notifyDataSetInvalidated();
			}
		} else if(requestCode == MifareClassicSectorActivity.MIFARE_CLASSIC_SECTOR) {
			if(resultCode == Activity.RESULT_OK) {
				Bundle extras = data.getExtras();

				int index = extras.getInt(MifareClassicSectorActivity.MIFARE_CLASSIC_SECTOR_INDEX_KEY);

				if(extras.containsKey(MifareClassicSectorActivity.MIFARE_CLASSIC_SECTOR_KEY)) {
					MifareClassicSector<MifareClassicKey> mifareClassicSector = extras.getParcelable(MifareClassicSectorActivity.MIFARE_CLASSIC_SECTOR_KEY);

					if(mifareClassicSector.hasKeyA()) {
						mifareClassicSector.setAKey(dataSource.refresh(mifareClassicSector.getAKey()));
					}
					if(mifareClassicSector.hasKeyB()) {
						mifareClassicSector.setBKey(dataSource.refresh(mifareClassicSector.getBKey()));
					}
					
					mifareClassicSector.setScheme(scheme);
					
					if(index == -1) {
						dataSource.createSector(mifareClassicSector);
					} else {
						MifareClassicSector<MifareClassicKey> current = scheme.getSector(index);
						
						current.setAccessBitsDataIndex0(mifareClassicSector.getAccessBitsDataIndex0());
						current.setAccessBitsDataIndex1(mifareClassicSector.getAccessBitsDataIndex1());
						current.setAccessBitsDataIndex2(mifareClassicSector.getAccessBitsDataIndex2());
						current.setAccessBitsTrailerIndex(mifareClassicSector.getAccessBitsTrailerIndex());
						current.setAKey(mifareClassicSector.getAKey());
						current.setBKey(mifareClassicSector.getBKey());
						current.setIndex(mifareClassicSector.getIndex());
						
						dataSource.updateSector(mifareClassicSector);
					}
					
				} else {
					dataSource.deleteSector(scheme.getSector(index));
				}
				
				mAdapter.notifyDataSetInvalidated();
			}
			
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
		
		refreshFooter(getView());
	}

	private void refreshFooter(View view) {
		View footer = view.findViewById(R.id.footer);
		if(scheme.isEmpty()) {
			footer.setVisibility(View.GONE);
		} else {
			footer.setVisibility(View.VISIBLE);
		}
	}
}
