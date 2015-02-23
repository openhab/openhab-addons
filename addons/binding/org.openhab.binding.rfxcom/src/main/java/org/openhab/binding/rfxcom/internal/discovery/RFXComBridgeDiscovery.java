/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.discovery;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import jd2xx.JD2XX;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.rfxcom.RFXComBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RFXComBridgeDiscovery} is responsible for discovering new RFXCOM
 * transceivers.
 * 
 * @author Pauli Anttila - Initial contribution
 * 
 */
public class RFXComBridgeDiscovery extends AbstractDiscoveryService {

	private final static Logger logger = LoggerFactory
			.getLogger(RFXComBridgeDiscovery.class);

	/** The refresh interval for background discovery */
	private long refreshInterval = 600;

	public RFXComBridgeDiscovery() {
		super(RFXComBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS, 10);
	}

	@Override
	public Set<ThingTypeUID> getSupportedThingTypes() {
		return RFXComBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS;
	}

	@Override
	public void startScan() {
		logger.debug("Start discovery scan for RFXCOM transceivers");
		discoverRfxcom();
	}
	
	@Override
	protected void startBackgroundDiscovery() {
		logger.debug("Start background discovery for RFXCOM transceivers");
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				discoverRfxcom();
			}
		}, 0, refreshInterval, TimeUnit.SECONDS);
	}

	private synchronized void discoverRfxcom() {

		try {
			JD2XX jd2xx = new JD2XX();
			logger.debug(
					"Discovering RFXCOM tranceiver devices by JD2XX version {}",
					jd2xx.getLibraryVersion());
			String[] devDescriptions = (String[]) jd2xx
					.listDevicesByDescription();
			String[] devSerialNumbers = (String[]) jd2xx
					.listDevicesBySerialNumber();
			logger.debug("Discovered {} FTDI device(s)", devDescriptions.length);

			for (int i = 0; i < devSerialNumbers.length; ++i) {
				if (devDescriptions != null && devDescriptions.length > 0) {
					switch (devDescriptions[i]) {
					case RFXComBindingConstants.BRIDGE_TYPE_RFXTRX433:
						addBridge(RFXComBindingConstants.BRIDGE_RFXTRX443,
								devSerialNumbers[i]);
						break;
					case RFXComBindingConstants.BRIDGE_TYPE_RFXTRX315:
						addBridge(RFXComBindingConstants.BRIDGE_RFXTRX315,
								devSerialNumbers[i]);
						break;
					case RFXComBindingConstants.BRIDGE_TYPE_RFXREC433:
						addBridge(RFXComBindingConstants.BRIDGE_RFXREC443,
								devSerialNumbers[i]);
						break;
					default:
						logger.trace("Ignore unknown device '{}'",
								devDescriptions[i]);
					}
				}
			}
			
			logger.debug("Discovery done");

		} catch (IOException e) {
			logger.error("Error occured during discovery", e);
		} catch (UnsatisfiedLinkError e) {
			logger.error(
					"Error occured when trying to load native library for OS '{}' version '{}', processor '{}'",
					System.getProperty("os.name"),
					System.getProperty("os.version"),
					System.getProperty("os.arch"), e);
		}
	}

	private void addBridge(ThingTypeUID bridgeType, String bridgeId) {
		logger.debug(
				"Discovered RFXCOM tranceiver, bridgeType='{}', bridgeId='{}'",
				bridgeType, bridgeId);

		Map<String, Object> properties = new HashMap<>(2);
		properties.put(RFXComBindingConstants.BRIDGE_ID, bridgeId);

		ThingUID uid = new ThingUID(bridgeType, bridgeId);
		if (uid != null) {
			DiscoveryResult result = DiscoveryResultBuilder.create(uid)
					.withProperties(properties).withLabel("RFXCOM tranceiver")
					.build();
			thingDiscovered(result);
		}

	}
}
