package org.openhab.binding.BluetoothLE.discovery;

import org.openhab.binding.BluetoothLE.protocol.ScanResult;

public interface BluetoothLEDiscoveryService {
	
	public ScanResult getScanResult(String device);

}
