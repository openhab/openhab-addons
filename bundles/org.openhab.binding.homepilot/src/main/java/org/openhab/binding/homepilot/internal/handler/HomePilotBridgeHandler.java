/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.homepilot.internal.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.homepilot.internal.HomePilotConfig;
import org.openhab.binding.homepilot.internal.HomePilotDevice;
import org.openhab.binding.homepilot.internal.HomePilotGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Steffen Stundzig - Initial contribution
 */
public class HomePilotBridgeHandler extends BaseBridgeHandler {

	private static final Logger logger = LoggerFactory.getLogger(HomePilotBridgeHandler.class);

	private HomePilotConfig config;
	private HomePilotGateway gateway;

	private ScheduledFuture<?> refreshJob;

	public HomePilotBridgeHandler(Bridge bridge) {
		super(bridge);
		config = createConfig();
		gateway = new HomePilotGateway(bridge.getUID().getId(), config);
	}

	@Override
	public void initialize() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					final Map<String, HomePilotDevice> devices = new HashMap<String, HomePilotDevice>();
					for (HomePilotDevice device : gateway.loadAllDevices()) {
						devices.put(device.getDeviceId(), device);
					}
					for (Thing thing : getThing().getThings()) {
						if (thing.getHandler() != null) {
							if (devices.containsKey(thing.getUID().getId())) {
								((HomePilotThingHandler) thing.getHandler())
										.refresh(devices.get(thing.getUID().getId()));
							}
						} else {
							logger.error("Missing handler on refresh for " + thing.toString());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		refreshJob = scheduler.scheduleAtFixedRate(runnable, 60, 10, TimeUnit.SECONDS);
		updateStatus(ThingStatus.ONLINE);
	}

	private HomePilotConfig createConfig() {
		HomePilotConfig config = getThing().getConfiguration().as(HomePilotConfig.class);
		return config;
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		logger.info("handleCommand " + channelUID + "; " + command);
	}

	public HomePilotGateway getGateway() {
		return gateway;
	}

	@Override
	public void dispose() {
		if (refreshJob != null) {
			refreshJob.cancel(true);
		}
		super.dispose();
	}
}
