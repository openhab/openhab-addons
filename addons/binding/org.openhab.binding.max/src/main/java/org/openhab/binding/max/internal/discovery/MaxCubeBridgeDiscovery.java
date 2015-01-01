/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.max.MaxBinding;
import org.openhab.binding.max.config.MaxCubeBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The {@link MaxCubeBridgeDiscovery} is responsible for discovering new 
 * MAX! Cube LAN gateway devices on the network
 * 
 * @author Marcel Verpaalen - Initial contribution
 * 
 */
public class MaxCubeBridgeDiscovery extends AbstractDiscoveryService  {
	private final static Logger logger = LoggerFactory.getLogger(MaxCubeBridgeDiscovery.class);

	public MaxCubeBridgeDiscovery() {
		super(MaxBinding.SUPPORTED_BRIDGE_THING_TYPES_UIDS, 15);
	}

	@Override
	public Set<ThingTypeUID> getSupportedThingTypes() {
		return MaxBinding.SUPPORTED_BRIDGE_THING_TYPES_UIDS;
	}

	private void discoverCube() {
		String cubeSerialNumber = null;
		MaxCubeBridgeDiscoveryResult discoverResults = MaxCubeDiscover.DiscoverCube(null);
		cubeSerialNumber = discoverResults.getSerialNumber();

		if(cubeSerialNumber!=null) {
			logger.trace("Adding new MAX! Cube Lan Gateway on {} with id '{}' to Smarthome inbox", discoverResults.getIpAddress(), cubeSerialNumber);

			Map<String, Object> properties = new HashMap<>(2);
	        properties.put(MaxCubeBridgeConfiguration.IP_ADDRESS,discoverResults.getIpAddress());
	        properties.put(MaxBinding.SERIAL_NUMBER,cubeSerialNumber);
			ThingUID uid = new ThingUID( MaxBinding.CUBEBRIDGE_THING_TYPE, cubeSerialNumber);
			if(uid!=null) {
				DiscoveryResult result = DiscoveryResultBuilder.create(uid)
						.withProperties(properties)
						.withLabel("MAX! Cube LAN Gateway (" + cubeSerialNumber + ")" )
						.build();
				thingDiscovered (result);
			} 
		}	
	}

	@Override
	public void startScan() {
		discoverCube();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.smarthome.config.discovery.AbstractDiscoveryService#startBackgroundDiscovery()
	 */
	
	@Override
	protected void startBackgroundDiscovery() {
		discoverCube();
	}


	@Override
	public boolean isBackgroundDiscoveryEnabled() {
		return true;
	}


}

