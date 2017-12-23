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
