package org.openhab.binding.fox.internal.devices;

import org.openhab.binding.fox.internal.core.FoxDevice;
import org.openhab.binding.fox.internal.core.FoxException;
import org.openhab.binding.fox.internal.slots.FoxSlotBeep;
import org.openhab.binding.fox.internal.slots.FoxSlotInput;
import org.openhab.binding.fox.internal.slots.FoxSlotOutput;
import org.openhab.binding.fox.internal.slots.FoxSlotThermometer;
import org.openhab.binding.fox.internal.slots.FoxSlotThermostate;

public class FoxDeviceTouch extends FoxDevice {

	public final static int inputsCount = 7;
	public final static int outputsCount = 7;
	public final static int beepsCount = 1;
	public final static int thermometerCount = 1;
	public final static int thermostateCount = 4;
	
	public FoxDeviceTouch(int address) throws FoxException {
		super(address);
		type = "Fox TOUCH";
		
		for (int i = 0; i < inputsCount; i++)
			addSlot(new FoxSlotInput());
		
		for (int i = 0; i < outputsCount; i++)
			addSlot(new FoxSlotOutput());
		
		for (int i = 0; i < beepsCount; i++)
			addSlot(new FoxSlotBeep());
		
		for (int i = 0; i < thermometerCount; i++)
			addSlot(new FoxSlotThermometer());
		
		for (int i = 0; i < thermostateCount; i++)
			addSlot(new FoxSlotThermostate());
	}

	public FoxSlotInput getButton(int index) {
		return (FoxSlotInput) findSlot(FoxSlotInput.class, index);
	}
	
	public FoxSlotOutput getLed(int index) {
		return (FoxSlotOutput) findSlot(FoxSlotOutput.class, index);
	}
	
	public FoxSlotBeep getSpeaker() {
		return (FoxSlotBeep) findSlot(FoxSlotBeep.class, 0);
	}
	
	public FoxSlotThermometer getThermometer() {
		return (FoxSlotThermometer) findSlot(FoxSlotThermometer.class, 0);
	}
	
	public FoxSlotThermostate getThermostate(int index) {
		return (FoxSlotThermostate) findSlot(FoxSlotThermostate.class, index);
	}
}
