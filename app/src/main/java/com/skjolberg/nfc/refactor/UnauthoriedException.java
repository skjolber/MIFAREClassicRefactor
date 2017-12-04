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
