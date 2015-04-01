package org.openhab.binding.openwebnetvdes.devices;

import java.util.EnumSet;

import org.eclipse.smarthome.core.library.types.OnOffType;

public class IndoorCamera extends BticinoDevice {
	private OnOffType cameraMode = null;
	
	@Override
	public VdesDeviceType getType() {
		return VdesDeviceType.INDOOR_CAMERA;
	}	
	
	public OnOffType getCameraMode() {
		return cameraMode;
	}

	public void setCameraMode(OnOffType cameraMode) {
		this.cameraMode = cameraMode;
	}

	static {
		features = EnumSet.of(DeviceFeatureType.INDOOR_CAMERA);
	}
}