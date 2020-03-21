package org.openhab.binding.fox.internal.slots;

import org.openhab.binding.fox.internal.core.FoxException;
import org.openhab.binding.fox.internal.core.FoxSlot;

public class FoxSlotOutput extends FoxSlot {

	public FoxSlotOutput() {
		
	}
	
	public void turnOn() throws FoxException {
		writeSet(0x01);
	}
	
	public void turnOff() throws FoxException {
		writeSet(0x00);
	}
	
	public void toggle() throws FoxException {
		writeSet(0x02);
	}
	
	public void flash() throws FoxException {
		writeSet(0x03);
	}
	
	public void flash(int timeMillisec) throws FoxException {
		writeSet(0x03, convertArg(timeMillisec, 25, 5*60*1000, 25));
	}
	
	public void pulse() throws FoxException {
		writeSet(0x04);
	}
	
	public void pulse(int timeMillisec) throws FoxException {
		writeSet(0x04, convertArg(timeMillisec, 25, 5*60*1000, 25));
	}
	
	public void pwm() throws FoxException {
		writeSet(0x05);
	}
	
	public void pwm(int timeOnMillisec, int timeOffMillisec) throws FoxException {
		writeSet(0x05, convertArg(timeOnMillisec, 25, 5000, 25), 0,
				convertArg(timeOffMillisec, 25, 5000, 25), 0);
	}
	
	public boolean isOn() throws FoxException {
		return readGet(1)[0] != 0x00;
	}
}
