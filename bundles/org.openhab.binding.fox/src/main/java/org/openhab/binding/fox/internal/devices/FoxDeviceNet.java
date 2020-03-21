package org.openhab.binding.fox.internal.devices;

import org.openhab.binding.fox.internal.core.FoxDevice;
import org.openhab.binding.fox.internal.core.FoxException;

public class FoxDeviceNet extends FoxDevice {

	public FoxDeviceNet(int address) throws FoxException {
		super(address);
		type = "Fox NET";
	}

}
