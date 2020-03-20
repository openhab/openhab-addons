package org.openhab.binding.fox.internal.slots;

import java.awt.Color;

import org.openhab.binding.fox.internal.core.FoxException;
import org.openhab.binding.fox.internal.core.FoxSlot;

public class FoxSlotColour extends FoxSlot {
	
	public FoxSlotColour() {
		
	}
	
	public void turnOn() throws FoxException {
		writeSet(0x01);
	}
	
	public void turnOn(Color c) throws FoxException {
		writeSet(0x01, convertArg(c.getRed(), 0, 255), convertArg(c.getGreen(), 0, 255), convertArg(c.getBlue(), 0, 255));
	}
	
	public void turnOff() throws FoxException {
		writeSet(0x00);
	}
	
	public void toggle() throws FoxException {
		writeSet(0x02);
	}
	
	public void toggle(Color c) throws FoxException {
		writeSet(0x02, convertArg(c.getRed(), 0, 255), convertArg(c.getGreen(), 0, 255), convertArg(c.getBlue(), 0, 255));
	}
	
	public boolean isOn() throws FoxException {
		return readGet(1)[0] != 0x00;
	}
	
	public Color getLevel() throws FoxException {
		Byte[] state = readGet(4);
		return new Color(state[1] & 0xff, state[2] & 0xff, state[3] & 0xff);
	}
}
