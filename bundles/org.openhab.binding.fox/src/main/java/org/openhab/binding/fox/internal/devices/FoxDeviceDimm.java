package org.openhab.binding.fox.internal.devices;

import org.openhab.binding.fox.internal.core.FoxDevice;
import org.openhab.binding.fox.internal.core.FoxException;
import org.openhab.binding.fox.internal.slots.FoxSlotInput;
import org.openhab.binding.fox.internal.slots.FoxSlotLevel;
import org.openhab.binding.fox.internal.slots.FoxSlotOutput;

public class FoxDeviceDimm extends FoxDevice {

	public final static int inputsCount = 8;
	public final static int outputsCount = 7;
	public final static int levelsCount = 7;
	
	public FoxDeviceDimm(int address) throws FoxException {
		super(address);
		type = "Fox DIMM";
		
		for (int i = 0; i < inputsCount; i++)
			addSlot(new FoxSlotInput());
		
		for (int i = 0; i < outputsCount; i++)
			addSlot(new FoxSlotOutput());
		
		for (int i = 0; i < levelsCount; i++)
			addSlot(new FoxSlotLevel());
	}

	public FoxSlotInput getButton(int index) {
		return (FoxSlotInput) findSlot(FoxSlotInput.class, index);
	}
	
	public FoxSlotOutput getOutput(int index) {
		return (FoxSlotOutput) findSlot(FoxSlotOutput.class, index);
	}
	
	public FoxSlotLevel getDimmer(int index) {
		return (FoxSlotLevel) findSlot(FoxSlotLevel.class, index);
	}
}
