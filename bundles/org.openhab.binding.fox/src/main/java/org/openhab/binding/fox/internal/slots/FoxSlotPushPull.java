package org.openhab.binding.fox.internal.slots;

import org.openhab.binding.fox.internal.core.FoxException;
import org.openhab.binding.fox.internal.core.FoxSlot;

public class FoxSlotPushPull extends FoxSlot {

	public enum State {
		STOPPED,
		OPENED,
		CLOSED
	}
	
	public enum Sequence {
		OPEN_STOP(0x05),
		CLOSE_STOP(0x06),
		OPEN_CLOSE(0x07),
		OPEN_CLOSE_STOP(0x08),
		CLOSE_OPEN_STOP(0x09),
		OPEN_STOP_CLOSE_STOP(0x0a);

		private int value;

		private Sequence(int value) {
			this.value = value;
		}
		
		int getValue() {
			return value;
		}
	}
	
	public FoxSlotPushPull() {
		
	}
	
	public void stop() throws FoxException {
		writeSet(0x00);
	}
	
	public void open() throws FoxException {
		writeSet(0x01);
	}
	
	public void open(int timeSec) throws FoxException {
		writeSet(0x01, convertArg(timeSec, 5, 1200, 5));
	}
	
	public void close() throws FoxException {
		writeSet(0x02);
	}
	
	public void close(int timeSec) throws FoxException {
		writeSet(0x02, convertArg(timeSec, 5, 1200, 5));
	}
	
	public void openPulse() throws FoxException {
		writeSet(0x03);
	}
	
	public void openPulse(int timeMillisec) throws FoxException {
		writeSet(0x03, convertArg(timeMillisec, 25, 5000, 25));
	}
	
	public void closePulse() throws FoxException {
		writeSet(0x04);
	}
	
	public void closePulse(int timeMillisec) throws FoxException {
		writeSet(0x04, convertArg(timeMillisec, 25, 5000, 25));
	}
	
	public void doNextFromSequence(Sequence sequence) throws FoxException {
		writeSet(sequence.getValue());
	}
	
	public State getState() throws FoxException {
		switch (readGet(1)[0]) {
		case 0:
			return State.STOPPED;
		case 1:
			return State.OPENED;
		case 2:
			return State.CLOSED;
		default:
			throw new FoxException("Unknown state"); 
		}
	}
}
