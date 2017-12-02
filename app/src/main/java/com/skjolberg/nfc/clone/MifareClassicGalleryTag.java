package com.skjolberg.nfc.clone;

import com.skjolberg.nfc.clone.mifareclassic.data.MifareClassicTag;

public class MifareClassicGalleryTag extends GalleryTag {

	private MifareClassicTag tag;

	public MifareClassicTag getMifareClassicDataTag() {
		return tag;
	}

	public void setMifareClassicDataTag(MifareClassicTag tag) {
		this.tag = tag;
	}
	
	
}
