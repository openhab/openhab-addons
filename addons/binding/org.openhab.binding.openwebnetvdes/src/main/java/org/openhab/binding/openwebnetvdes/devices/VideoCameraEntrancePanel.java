package org.openhab.binding.openwebnetvdes.devices;

import java.util.EnumSet;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;

public class VideoCameraEntrancePanel extends BticinoDevice {	
	private OnOffType cameraMode = null;
	private OpenClosedType openClosed = null;
	
	@Override
	public VdesDeviceType getType() {
		return VdesDeviceType.VIDEO_CAMERA_ENTRANCE_PANEL;
	}

	public OnOffType getCameraMode() {
		return cameraMode;
	}

	public void setCameraMode(OnOffType cameraMode) {
		this.cameraMode = cameraMode;
	}	
	
	public OpenClosedType getOpenClosed() {
		return openClosed;
	}

	public void setOpenClosed(OpenClosedType openClosed) {
		this.openClosed = openClosed;
	}

	static {
		features = EnumSet.of(DeviceFeatureType.RISER_CAMERA, 
				DeviceFeatureType.RISER_DOOR_LOCK_ACTUATOR,
				DeviceFeatureType.LIGHT);
	}
}
