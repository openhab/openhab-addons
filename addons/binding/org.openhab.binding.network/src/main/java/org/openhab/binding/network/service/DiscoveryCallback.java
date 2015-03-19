package org.openhab.binding.network.service;

/**
 * Callback for a new Device to be committed to Homematic
 * @author Marc Mettke - Initial contribution
 */
public interface DiscoveryCallback {
	public void newDevice(String ip);
}
