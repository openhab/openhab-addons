/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.discovery;

import static org.openhab.binding.network.NetworkBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.network.service.DiscoveryCallback;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.openhab.binding.network.service.NetworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetworkDiscoveryService} is responsible for discovering devices on 
 * the current Network. It uses every Network Interface which is connect to a Network
 * 
 * @author Marc Mettke - Initial contribution
 */
public class NetworkDiscoveryService extends AbstractDiscoveryService {
	private final Logger logger = LoggerFactory.getLogger(NetworkDiscoveryService.class);

	public NetworkDiscoveryService() {
		super(SUPPORTED_THING_TYPES_UIDS, 300, false);
	}

	public Set<ThingTypeUID> getSupportedThingTypes() {
		return SUPPORTED_THING_TYPES_UIDS;
	}

	@Override
	protected void startScan() {
		logger.debug("Starting Discovery");
		NetworkService.discoverNetwork(new DiscoveryCallback() {
			@Override
			public void newDevice(String ip) {
				submitDiscoveryResults(ip);
			}
		}, scheduler);
	}

	/**
	 * Submit the discovered Devices to the Smarthome inbox,
	 * 
	 * @param ip The Device IP
	 */
	private void submitDiscoveryResults(String ip) {

		// uid must not contains dots
		ThingUID uid = new ThingUID(THING_TYPE_DEVICE, ip.replace('.', '_') ); 	
		
		if(uid!=null) { 
			Map<String, Object> properties = new HashMap<>(1); 
			properties.put(PARAMETER_HOSTNAME ,ip);
			DiscoveryResult result = DiscoveryResultBuilder.create(uid)
					.withProperties(properties)
					.withLabel("Network Device (" + ip +")").build();
			thingDiscovered(result); 
		}
		 
	}

}
