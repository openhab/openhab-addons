package org.openhab.binding.fox.internal.slots;

import org.openhab.binding.fox.internal.core.FoxException;
import org.openhab.binding.fox.internal.core.FoxSlot;

public class FoxSlotInput extends FoxSlot {

	public FoxSlotInput() {
		
	}
	
	public boolean isActive() throws FoxException {
		return readGet(1)[0] != 0;
	}
}
