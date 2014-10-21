package org.openhab.io.transport.upnp;

import java.util.Map;

public interface UpnpIOService {

	public Map<String, String> invokeAction(UpnpIOParticipant participant,
			String serviceID, String actionID, Map<String,String> inputs);
	
	public void addSubscription(UpnpIOParticipant participant, String serviceID, int duration);
	
	public boolean isRegistered(UpnpIOParticipant participant);

}
