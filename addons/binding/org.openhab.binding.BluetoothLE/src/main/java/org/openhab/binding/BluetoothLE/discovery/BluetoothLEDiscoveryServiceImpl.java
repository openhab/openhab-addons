/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.BluetoothLE.discovery;

import static org.openhab.binding.BluetoothLE.BluetoothLEBindingConstants.UUID_BATTERY;
import static org.openhab.binding.BluetoothLE.BluetoothLEBindingConstants.UUID_HEALTH_THERMOMETER;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.BluetoothLE.BluetoothLEBindingConstants;
import org.openhab.binding.BluetoothLE.protocol.BluetoothLEConnector;
import org.openhab.binding.BluetoothLE.protocol.BluetoothLESimulator;
import org.openhab.binding.BluetoothLE.protocol.BluetoothLEUDPConnector;
import org.openhab.binding.BluetoothLE.protocol.ScanRecord;
import org.openhab.binding.BluetoothLE.protocol.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BluetoothLEDiscoveryServiceImpl} is responsible for discovering Bluetooth low energy events
 * 
 * @author Patrick Ammann - Initial contribution
 */
public class BluetoothLEDiscoveryServiceImpl extends AbstractDiscoveryService implements BluetoothLEDiscoveryService {
	private static final Logger logger = LoggerFactory.getLogger(BluetoothLEDiscoveryServiceImpl.class);
	
	private MessageListener messageListener = null;
	private int udpPort = 9998;
	
	
	private Map<String, ScanResult> cachedScanResults = new HashMap<String, ScanResult>();
	

	public BluetoothLEDiscoveryServiceImpl() {
		super(BluetoothLEBindingConstants.SUPPORTED_THING_TYPES_UIDS, 10);
	}
	
	@Override
	public Set<ThingTypeUID> getSupportedThingTypes() {
		return BluetoothLEBindingConstants.SUPPORTED_THING_TYPES_UIDS;
	}
	
	@Override
	protected void startBackgroundDiscovery() {
		logger.debug("startBackgroundDiscovery");
		if (messageListener != null) {
			logger.debug("Close previous message listener");
			messageListener.setInterrupted(true);
			try {
				messageListener.join();
			} catch (InterruptedException e) {
				logger.info("Previous message listener closing interrupted", e);
			}
			messageListener = null;
		}
		messageListener = new MessageListener();
		messageListener.start();
	}

	@Override
	protected void stopBackgroundDiscovery() {
		logger.debug("stopBackgroundDiscovery");
		messageListener.setInterrupted(true);
		messageListener = null;
	}

	@Override
	protected void startScan() {
		logger.debug("startScan N/A");
	}
	
	@Override
	public ScanResult getScanResult(String device) {
		ScanResult sr;
		synchronized (cachedScanResults) {
			sr = cachedScanResults.get(device);
		}
		return sr;
	}
	
	/**
	 * The MessageListener runs as a separate thread.
	 *
	 * Thread listening message from Bluetooth low energy devices and send
	 * updates to openHAB bus.
	 *
	 */
	private class MessageListener extends Thread {
		private boolean interrupted = false;
		
		MessageListener() {
		}
		
		public void setInterrupted(boolean interrupted) {
			this.interrupted = interrupted;
			messageListener.interrupt();
		}
		
		@Override
		public void run() {
			logger.debug("Message listener started");
			BluetoothLEConnector connector = new BluetoothLEUDPConnector(udpPort);
			//BluetoothLEConnector connector = new BluetoothLESimulator();
			try {
				connector.connect();
			} catch (Exception e) {
				logger.error("Error occured when connecting to device", e);
				logger.warn("Closing message listener");
				// exit
				interrupted = true;
			}
			// as long as no interrupt is requested, continue running
			while (!interrupted) {
				try {
					// Wait a packet (blocking)
					Object obj = connector.getData();
					if (obj == null) continue;
					
					if (obj instanceof ScanResult) {
						ScanResult sr = (ScanResult)obj;
						discoverNonConnDevice(sr);					
					}
				} catch (Exception e) {
					logger.error("Error occured when received data from device", e);
				}
			}
			
			try {
				connector.disconnect();
			} catch (Exception e) {
				logger.error("Error occured when disconnecting from device", e);
			}
		}
	}
	
	/**
	 * Submit the discovered location to the Smarthome inbox
	 */
	private void discoverNonConnDevice(ScanResult sr) {
		logger.debug("Received data: {}", sr);
		ScanRecord r = sr.getScanRecord();
		List<UUID> uuids = r.getServiceUuids();

		ThingUID uid = null;
		if (uuids.size() == 1 && r.getServiceData(UUID_HEALTH_THERMOMETER) != null ||
			uuids.size() == 2 && r.getServiceData(UUID_HEALTH_THERMOMETER) != null && r.getServiceData(UUID_BATTERY) != null) {
			uid = new ThingUID(BluetoothLEBindingConstants.THING_TYPE_TemperatureSensor, sr.getDeviceAddress().replace(":", ""));
		}

		if (uid != null) {
			synchronized (cachedScanResults) {
				cachedScanResults.put(sr.getDeviceAddress(), sr);
			}
			
			String deviceName = r.getLocalName();
			if (deviceName == null) deviceName = sr.getDeviceAddress();
			
			Map<String, Object> properties = new HashMap<>(1);
			properties.put("device_address", sr.getDeviceAddress());
			DiscoveryResult result = DiscoveryResultBuilder.create(uid)
					.withProperties(properties)
					.withLabel("Temperature Sensor (" + deviceName + ")")
					.build();
			thingDiscovered(result);
		}
	}
}
