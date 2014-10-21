package org.openhab.io.transport.upnp;

public interface UpnpIOParticipant {
	
	public String getUDN();

	public void onValueReceived(String variable, String value, String service);
		
}
