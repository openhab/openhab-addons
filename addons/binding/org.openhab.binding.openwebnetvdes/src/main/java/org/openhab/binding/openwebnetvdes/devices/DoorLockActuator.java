package org.openhab.binding.openwebnetvdes.devices;

import java.util.EnumSet;

import org.eclipse.smarthome.core.library.types.OpenClosedType;

public class DoorLockActuator extends BticinoDevice {
	OpenClosedType openClosed; 
	
	@Override
	public VdesDeviceType getType() {
		return VdesDeviceType.DOOR_LOCK_ACTUATOR;
	}

	public OpenClosedType getOpenClosed() {
		return openClosed;
	}

	public void setOpenClosed(OpenClosedType openClosed) {
		this.openClosed = openClosed;
	}
	
	static {
		features = EnumSet.of(DeviceFeatureType.RISER_DOOR_LOCK_ACTUATOR);
	}
}
