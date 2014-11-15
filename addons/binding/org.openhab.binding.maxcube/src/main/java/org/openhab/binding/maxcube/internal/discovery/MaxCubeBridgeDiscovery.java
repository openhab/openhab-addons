/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.discovery;

import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.maxcube.MaxCubeBinding;
import org.openhab.binding.maxcube.config.MaxCubeBridgeConfiguration;
import org.openhab.binding.maxcube.config.MaxCubeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * The {@link MaxCubeBridgeDiscovery} is responsible for discovering new 
 * Max!Cube LAN gateway devices on the network
 * 
 * @author Marcel Verpaalen - Initial contribution
 * 
 */
public class MaxCubeBridgeDiscovery extends AbstractDiscoveryService  {
	private final static Logger logger = LoggerFactory.getLogger(MaxCubeBridgeDiscovery.class);
	private static final int INITIAL_START_DELAY = 60;


	public MaxCubeBridgeDiscovery() {
		super(15);
	}

	@Override
	public Set<ThingTypeUID> getSupportedThingTypes() {
		return MaxCubeBinding.SUPPORTED_BRIDGE_THING_TYPES_UIDS;
	}

	private void discoverCube() {
		String cubeSerialNumber = null;

		HashMap<String, String > discoverResults = new HashMap<String, String>(MaxCubeDiscover.DiscoverCube(null));
		if (discoverResults.containsKey(MaxCubeConfiguration.SERIAL_NUMBER)){
			cubeSerialNumber = discoverResults.get(MaxCubeConfiguration.SERIAL_NUMBER);
		}
		String ipAddress = null;
		if (discoverResults.containsKey(MaxCubeBridgeConfiguration.IP_ADDRESS)){
			ipAddress = discoverResults.get(MaxCubeBridgeConfiguration.IP_ADDRESS);
		}

		if(cubeSerialNumber!=null) {
			logger.debug("Adding new Max!Cube Lan Gateway on {} with id '{}' to Smarthome inbox", ipAddress, cubeSerialNumber);

			ThingUID uid = new ThingUID( MaxCubeBinding.CubeBridge_THING_TYPE, "MaxCube_" + cubeSerialNumber);
			if(uid!=null) {
				DiscoveryResult result = DiscoveryResultBuilder.create(uid)
						.withProperty(MaxCubeConfiguration.SERIAL_NUMBER,cubeSerialNumber)
						.withLabel("MaxCube LAN Gateway on " + ipAddress )
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
		Timer timer = new Timer();
		// to avoid conflict with normal maxcube starting delay this first discovery
		timer.schedule(new TimerTask() {
			public void run() {
				discoverCube();
			}
		}, (INITIAL_START_DELAY * 1000));
	}


	@Override
	public boolean isBackgroundDiscoveryEnabled() {
		return false;
	}


}

