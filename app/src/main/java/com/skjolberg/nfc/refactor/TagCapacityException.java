package com.skjolberg.nfc.refactor;

public class TagCapacityException extends RuntimeException {

	private int capacity;
	private int required;
	
	public TagCapacityException(int capacity, int required) {
		super();
		
		this.capacity = capacity;
		this.required = required;
	}

	private TagCapacityException(String detailMessage, int capacity, int required, Throwable throwable) {
		super(detailMessage, throwable);
		this.capacity = capacity;
		this.required = required;
	}

	private TagCapacityException(String detailMessage, int capacity, int required) {
		super(detailMessage);
		this.capacity = capacity;
		this.required = required;
	}

	private TagCapacityException(int capacity, int required, Throwable throwable) {
		super(throwable);
		this.capacity = capacity;
		this.required = required;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getRequired() {
		return required;
	}

	public void setRequired(int required) {
		this.required = required;
	}

	
}
