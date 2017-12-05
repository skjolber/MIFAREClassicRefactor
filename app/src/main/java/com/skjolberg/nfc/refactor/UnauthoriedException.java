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

public class UnauthoriedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private boolean requiresKeyA;
	private boolean requiresKeyB;
	
	public UnauthoriedException(boolean requiresKeyA, boolean requiresKeyB) {
		super();
		
		this.requiresKeyA = requiresKeyA;
		this.requiresKeyB = requiresKeyB;
	}

	public UnauthoriedException(boolean requiresKeyA, boolean requiresKeyB, String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		
		this.requiresKeyA = requiresKeyA;
		this.requiresKeyB = requiresKeyB;
	}

	public UnauthoriedException(boolean requiresKeyA, boolean requiresKeyB, String detailMessage) {
		super(detailMessage);
		
		this.requiresKeyA = requiresKeyA;
		this.requiresKeyB = requiresKeyB;
	}

	public UnauthoriedException(boolean requiresKeyA, boolean requiresKeyB, Throwable throwable) {
		super(throwable);
		
		this.requiresKeyA = requiresKeyA;
		this.requiresKeyB = requiresKeyB;
	}

	public boolean isRequiresKeyA() {
		return requiresKeyA;
	}

	public void setRequiresKeyA(boolean requiresKeyA) {
		this.requiresKeyA = requiresKeyA;
	}

	public boolean isRequiresKeyB() {
		return requiresKeyB;
	}

	public void setRequiresKeyB(boolean requiresKeyB) {
		this.requiresKeyB = requiresKeyB;
	}
	
	

}
