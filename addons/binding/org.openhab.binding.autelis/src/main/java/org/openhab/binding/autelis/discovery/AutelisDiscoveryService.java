/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.autelis.discovery;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.autelis.AutelisBindingConstants;

/**
 * 
 * Discovery Service for Autelis Pool Controllers.
 * 
 * @author Dan Cunningham
 *
 */
public class AutelisDiscoveryService extends AbstractDiscoveryService {

	private final Logger logger = LoggerFactory
			.getLogger(AutelisDiscoveryService.class);

	private static String DEFAULT_NAME = "poolcontrol";

	public AutelisDiscoveryService() throws IllegalArgumentException {
		super(AutelisBindingConstants.SUPPORTED_THING_TYPES_UIDS, 10);
	}

	@Override
	public Set<ThingTypeUID> getSupportedThingTypes() {
		return AutelisBindingConstants.SUPPORTED_THING_TYPES_UIDS;
	}

	@Override
	protected void startBackgroundDiscovery() {
		scheduler.schedule(new Runnable() {
			@Override
			public void run() {
				discoverAutelis();
			}
		}, 0, TimeUnit.MILLISECONDS);
	}

	@Override
	protected void startScan() {
		discoverAutelis();
	}

	/**
	 * Looks for devices that respond back with the proper title tags
	 */
	private void discoverAutelis() {
		String response = get("http://" + DEFAULT_NAME + "/app.html");
		if (response != null
				&& response.contains("<title>Autelis Pool Control</title>")) {
			ThingUID uid = new ThingUID(
					AutelisBindingConstants.POOLCONTROL_THING_TYPE_UID, "pool");
			Map<String, Object> properties = new HashMap<>(1);
			properties.put("host", DEFAULT_NAME);
			properties.put("user", "admin");
			properties.put("password", "admin");
			properties.put("port", new Integer(80));
			DiscoveryResult result = DiscoveryResultBuilder.create(uid)
					.withProperties(properties)
					.withLabel("Autelis Pool Controller").build();
			thingDiscovered(result);
		}
	}

	/**
	 * Performs a get request
	 * 
	 * @param url
	 *            to get
	 * @return the string response or null
	 */
	private String get(String url) {
		String response = null;
		try {
			URL _url = new URL(url);
			URLConnection connection = _url.openConnection();
			response = IOUtils.toString(connection.getInputStream());
		} catch (MalformedURLException e) {
			logger.debug("Constructed url '{}' is not valid: {}", url,
					e.getMessage());
		} catch (IOException e) {
			logger.error("Error accessing url '{}' : {} ", url, e.getMessage());
		}
		return response;
	}
}
