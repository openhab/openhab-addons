package org.openhab.binding.mysensors.handler;

import java.util.EventListener;

/**
 * @author Tim Oberföll
 *
 *	Handler that implement this interface receive update events from the MySensors network
 */
public interface MySensorsUpdateListener extends EventListener{
	/**
	 * Procedure for receive status update from MySensorsNetwork.
	 */
	public void statusUpdateReceived(MySensorsStatusUpdateEvent event);
	public void revertToOldStatus(MySensorsStatusUpdateEvent event);
}
