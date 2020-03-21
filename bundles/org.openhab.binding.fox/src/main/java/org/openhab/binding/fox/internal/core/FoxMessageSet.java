package org.openhab.binding.fox.internal.core;

import java.util.ArrayList;

class FoxMessageSet extends FoxMessage {

	int slotIndex;
	String setArgs;
	
	public FoxMessageSet() {
		super();
		slotIndex = 0;
		setArgs = "";
	}
	
	@Override
	protected void prepareMessage() {
		message = String.format("set %d %s", slotIndex, setArgs);
	}
	
	void setSlotIndex(int index) {
		slotIndex = index;
	}
	
	void setArgs(ArrayList<Byte> args) {
		StringBuilder builder = new StringBuilder();
		for (Byte i : args) {
			builder.append(i & 0xff);
			builder.append(" ");
		}
		setArgs = builder.toString().trim();
	}

	@Override
	protected void interpretMessage() {
		
	}

}
