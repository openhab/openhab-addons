package org.openhab.binding.fox.internal.core;


class FoxMessageGet extends FoxMessage {

	int slotIndex;
	
	public FoxMessageGet() {
		super();
		slotIndex = 0;
	}
	
	@Override
	protected void prepareMessage() {
		message = String.format("get %d", slotIndex);
	}
	
	void setSlotIndex(int index) {
		slotIndex = index;
	}

	@Override
	protected void interpretMessage() {
		
	}

}
