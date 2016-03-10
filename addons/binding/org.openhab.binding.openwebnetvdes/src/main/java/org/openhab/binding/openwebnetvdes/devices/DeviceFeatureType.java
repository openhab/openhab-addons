package org.openhab.binding.openwebnetvdes.devices;

/**
* This enumeration represents the Video Door Entry System's feature abstraction of devices. 
* 
* @author Dmytro Kulyanda
* @since 0.1
*/
public enum DeviceFeatureType {
	RISER_CAMERA(0),    		
    INDOOR_CAMERA(1),
    RISER_DOOR_LOCK_ACTUATOR(2),
    DOOR_LOCK_ACTUATOR(3),
    LIGHT(4),
    INVALID(-1);

	private int value;

	private DeviceFeatureType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	public static DeviceFeatureType create(int value) {
		switch(value) {
		case 0:
	    	return RISER_CAMERA;
		case 1:
			return INDOOR_CAMERA;
		case 2:
			return RISER_DOOR_LOCK_ACTUATOR;
		case 3:
			return DOOR_LOCK_ACTUATOR;
		case 4: 
			return LIGHT;
		default:
			return INVALID;
		}
	}
	
	public String ownFrameOn() {
		switch(value) {
	    case 0:
	    	return "*6*0*WHERE#2##";
	    case 1:
	    	return "*6*0*WHERE##";	    
	    case 4: 
	    	return "*6*12*WHERE##";
	    default:
	    	return null;
	    }
	}
	
	public String ownFrameOff() {
		switch(value) {
	    case 0:
	    	return "*6*9##";
	    case 1:
	    	return "*6*9##";
	    case 4: 
	    	return "*6*11*WHERE##";
	    default:
	    	return null;
	    }
	}
	
	public String ownFrameOpenLock() {
		switch(value) {
	    case 2:
	    	return "*6*10*WHERE#2##";
	    case 3:
	    	return "*6*10*WHERE##";
	    default:
	    	return null;
	    }
	}	
	
	public String toString() {
		switch(value) {
	    case 0:
	    	return "Riser Camera";
	    case 1:
	    	return "Indoor camera";
	    case 2:
	    	return "Riser door lock activator";
	    case 3:
	    	return "Door lock activator";
	    case 4: 
	    	return "Light";
	    default:
	    	return "Invalid";
	    }
	}
}
