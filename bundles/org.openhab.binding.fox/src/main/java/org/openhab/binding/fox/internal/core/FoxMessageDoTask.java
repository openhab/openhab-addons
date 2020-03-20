package org.openhab.binding.fox.internal.core;

class FoxMessageDoTask extends FoxMessage {

	int taskId;
	
	public FoxMessageDoTask() {
		super();
		taskId = 0;
	}

	@Override
	protected void prepareMessage() {
		message = String.format("do T%d", taskId);
	}
	
	void setTaskId(int id) {
		taskId = id;
	}

	@Override
	protected void interpretMessage() {
		
	}
}
