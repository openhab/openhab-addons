package org.openhab.binding.mysensors.handler;

import org.openhab.binding.mysensors.internal.MySensorsMessage;

/**
 * @author Tim Oberföll
 * 
 * If a new message from the gateway/bridge is received 
 * a MySensorsStatusUpdateEvent is generated containing the MySensors message 
 */
public class MySensorsStatusUpdateEvent {
	private MySensorsMessage data;
	
	public MySensorsStatusUpdateEvent(MySensorsMessage data) {
		this.data = data;
	}

	public MySensorsMessage getData() {
		return data;
	}

	public void setData(MySensorsMessage data) {
		this.data = data;
	}
	
}
