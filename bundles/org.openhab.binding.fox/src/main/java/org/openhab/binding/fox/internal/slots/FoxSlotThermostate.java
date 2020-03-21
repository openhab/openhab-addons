package org.openhab.binding.fox.internal.slots;

import java.util.ArrayList;

import org.openhab.binding.fox.internal.core.FoxException;
import org.openhab.binding.fox.internal.core.FoxSlot;

public class FoxSlotThermostate extends FoxSlot {

	public FoxSlotThermostate() {
		
	}
	
	public void turnOn() throws FoxException {
		writeSet(0x01);
	}
	
	private void thresholdOperation(int cmd, double temperatureThreshold)  throws FoxException {
		ArrayList<Integer> args = new ArrayList<Integer>();
		args.add(cmd);
		args.addAll(convertArgAligned((int) (10 * temperatureThreshold), -1200, 1200, 2));
		Integer[] array = new Integer[args.size()];
		writeSet(args.toArray(array));
	}
	
	public void turnOn(double temperatureThreshold) throws FoxException {
		thresholdOperation(0x01, temperatureThreshold);
	}
	
	public void turnOff() throws FoxException {
		writeSet(0x00);
	}
	
	public void toggle() throws FoxException {
		writeSet(0x02);
	}
	
	public void toggle(double temperatureThreshold) throws FoxException {
		thresholdOperation(0x02, temperatureThreshold);
	}
	
	public void setThreshold(double temperatureThreshold) throws FoxException {
		thresholdOperation(0x10, temperatureThreshold);
	}
	
	public boolean isOn() throws FoxException {
		return readGet(2)[1] != 0x00;
	}
	
	public boolean isActive() throws FoxException {
		return readGet(1)[0] != 0x00;
	}
}
