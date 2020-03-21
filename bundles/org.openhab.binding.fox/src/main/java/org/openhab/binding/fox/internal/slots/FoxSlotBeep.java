package org.openhab.binding.fox.internal.slots;

import org.openhab.binding.fox.internal.core.FoxException;
import org.openhab.binding.fox.internal.core.FoxSlot;

public class FoxSlotBeep extends FoxSlot {
	
	public FoxSlotBeep() {
		
	}
	
	public void stop() throws FoxException {
		writeSet(0x00);
	}
	
	public void play(int freqHz) throws FoxException {
		writeSet(0x40, convertArg(freqHz, 25, 5000, 25));
	}
	
	public void play(int freqHz, int timeMillisec) throws FoxException {
		writeSet(0x41, convertArg(freqHz, 100, 5000, 25), convertArg(timeMillisec, 25, 5000, 25));
	}
}
