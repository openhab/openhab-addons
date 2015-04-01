package org.openhab.binding.openwebnetvdes.devices;

import static org.openhab.binding.openwebnetvdes.OpenWebNetVdesBindingConstants.*;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
* This enumeration represents the Video Door Entry System's devices. 
* 
* @author Dmytro Kulyanda
* @since 0.1
*/
public enum VdesDeviceType {
	VIDEO_CAMERA_ENTRANCE_PANEL(VIDEO_CAMERA_ENTRANCE_PANEL_THING_TYPE),    		
    INDOOR_CAMERA(APARTMENT_CAMERA_THING_TYPE),
    DOOR_LOCK_ACTUATOR(DOOR_LOCK_ACTUATOR_THING_TYPE),
    INVALID(null);

	private ThingTypeUID value;

	private VdesDeviceType(ThingTypeUID value) {
		this.value = value;
	}

	public ThingTypeUID getValue() {
		return value;
	}
	
	public static VdesDeviceType create(ThingTypeUID value) {
		if (VIDEO_CAMERA_ENTRANCE_PANEL_THING_TYPE.equals(value)) 
			return VIDEO_CAMERA_ENTRANCE_PANEL;
		else if (APARTMENT_CAMERA_THING_TYPE.equals(value))
			return INDOOR_CAMERA;
		else if (DOOR_LOCK_ACTUATOR_THING_TYPE.equals(value))
			return DOOR_LOCK_ACTUATOR;
		else
			return INVALID;
	}
	
	public String toString() {
		if (VIDEO_CAMERA_ENTRANCE_PANEL_THING_TYPE.equals(value)) 
			return "Video camera entrance panel";
		else if (APARTMENT_CAMERA_THING_TYPE.equals(value))
			return "Indoor camera";
		else if (DOOR_LOCK_ACTUATOR_THING_TYPE.equals(value))
			return "Door lock activator";
		else
			return "Invalid";
	}
}
