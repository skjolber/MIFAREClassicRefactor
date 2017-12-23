package com.skjolberg.nfc.refactor;

import com.skjolberg.nfc.refactor.mifareclassic.data.MifareClassicTag;

public class MifareClassicGalleryTag extends GalleryTag {

	private MifareClassicTag tag;

	public MifareClassicTag getMifareClassicDataTag() {
		return tag;
	}

	public void setMifareClassicDataTag(MifareClassicTag tag) {
		this.tag = tag;
	}
	
	
}
