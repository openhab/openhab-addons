package org.openhab.binding.fox.internal.slots;

import org.openhab.binding.fox.internal.core.FoxException;
import org.openhab.binding.fox.internal.core.FoxSlot;

public class FoxSlotLevel extends FoxSlot {

	public FoxSlotLevel() {
		
	}
	
	public void turnOn() throws FoxException {
		writeSet(0x01);
	}
	
	public void turnOn(int level) throws FoxException {
		writeSet(0x10, convertArg(level, 1, 255));
	}
	
	public void turnOn(double level) throws FoxException {
		turnOn((int)(255*level));
	}
	
	public void turnOff() throws FoxException {
		writeSet(0x00);
	}
	
	public void toggle() throws FoxException {
		writeSet(0x02);
	}
	
	public void toggle(int level) throws FoxException {
		writeSet(0x12, convertArg(level, 1, 255));
	}
	
	public void toggle(double level) throws FoxException {
		toggle((int)(255*level));
	}
	
	public void stopSweep() throws FoxException {
		writeSet(0x20);
	}
	
	public void sweep() throws FoxException {
		writeSet(0x21);
	}
	
	public void sweepUp() throws FoxException {
		writeSet(0x22);
	}
	
	public void sweepDown() throws FoxException {
		writeSet(0x23);
	}
	
	public boolean isOn() throws FoxException {
		return readGet(1)[0] != 0x00;
	}
	
	public double getLevel() throws FoxException {
		return readGet(2)[1] / 255.0;
	}
}
