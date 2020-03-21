package org.openhab.binding.fox.internal.slots;

import org.openhab.binding.fox.internal.core.FoxException;
import org.openhab.binding.fox.internal.core.FoxSlot;

public class FoxSlotThermometer extends FoxSlot {

	public FoxSlotThermometer() {
		
	}
	
	public double getTemperature() throws FoxException {
		Byte[] state = readGet(3);
		return (256*state[2] + (state[1] & 0xff)) / 10.0;
	}
}
