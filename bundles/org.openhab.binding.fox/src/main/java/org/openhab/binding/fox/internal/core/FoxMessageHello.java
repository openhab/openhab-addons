package org.openhab.binding.fox.internal.core;

class FoxMessageHello extends FoxMessage {

	public FoxMessageHello() {
		super();
	}

	@Override
	protected void prepareMessage() {
		message = "hello";
	}

	@Override
	protected void interpretMessage() {
		
	}
}
