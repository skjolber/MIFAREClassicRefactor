package com.skjolberg.nfc.clone.mifareclassic;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class MifareClassicTagFragmentAdapter extends FragmentStatePagerAdapter {
	
    private List<MifareClassicScheme<MifareClassicKey>> tags;
    private MifareClassicDataSource dataSource;
    
    public MifareClassicTagFragmentAdapter(FragmentManager fm, MifareClassicDataSource dataSource) {
        super(fm);
        
        this.tags = dataSource.getTags();
        this.dataSource = dataSource;
    }

    @Override
    public Fragment getItem(int position) {
        return MifareClassicTagFragment.newInstance(tags.get(position % tags.size()), dataSource);
    }

    @Override
    public int getCount() {
        return tags.size();
    }

    @Override
    public int getItemPosition(Object object){
    	// http://stackoverflow.com/questions/10396321/remove-fragment-page-from-viewpager-in-android
        return POSITION_NONE;
    }

   
}