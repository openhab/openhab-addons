/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pulseaudio.handler;

import static org.openhab.binding.pulseaudio.PulseaudioBindingConstants.BRIDGE_PARAMETER_HOST;
import static org.openhab.binding.pulseaudio.PulseaudioBindingConstants.BRIDGE_PARAMETER_PORT;
import static org.openhab.binding.pulseaudio.PulseaudioBindingConstants.BRIDGE_PARAMETER_REFRESH_INTERVAL;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.pulseaudio.PulseaudioBindingConstants;
import org.openhab.binding.pulseaudio.internal.PulseaudioClient;
import org.openhab.binding.pulseaudio.internal.items.AbstractAudioDeviceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PulseaudioBridgeHandler} is the handler for a Pulseaudio server and
 * connects it to the framework.
 * 
 * @author Tobias Bräutigam
 *
 */
public class PulseaudioBridgeHandler extends BaseBridgeHandler {
	private Logger logger = LoggerFactory
			.getLogger(PulseaudioBridgeHandler.class);

	public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
			.singleton(PulseaudioBindingConstants.BRIDGE_THING_TYPE);

	public String host = "localhost";
	public int port = 4712;

	public int refreshInterval = 30000;

	private PulseaudioClient client;

	private HashSet<String> lastActiveDevices = new HashSet<String>();

	private ScheduledFuture<?> pollingJob;
	private Runnable pollingRunnable = new Runnable() {
		@Override
		public void run() {
			client.update();
			for (AbstractAudioDeviceConfig device : client.getItems()) {
				if (lastActiveDevices != null
						&& lastActiveDevices.contains(device.getPaName())) {
					for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
						try {
							deviceStatusListener.onDeviceStateChanged(
									getThing().getUID(), device);
						} catch (Exception e) {
							logger.error(
									"An exception occurred while calling the DeviceStatusListener",
									e);
						}
					}
				} else {
					for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
						try {
							deviceStatusListener.onDeviceAdded(getThing(),
									device);
							deviceStatusListener.onDeviceStateChanged(
									getThing().getUID(), device);
						} catch (Exception e) {
							logger.error(
									"An exception occurred while calling the DeviceStatusListener",
									e);
						}
						lastActiveDevices.add(device.getPaName());
					}
				}
			}
		}
	};

	private List<DeviceStatusListener> deviceStatusListeners = new CopyOnWriteArrayList<>();

	public PulseaudioBridgeHandler(Bridge bridge) {
		super(bridge);
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		if (command instanceof RefreshType) {
			client.update();
		} else {
			logger.warn("received invalid command for pulseaudio bridge '{}'.",
					host);
		}
	}

	private synchronized void startAutomaticRefresh() {
		if (pollingJob == null || pollingJob.isCancelled()) {
			pollingJob = scheduler.scheduleAtFixedRate(pollingRunnable, 0,
					refreshInterval, TimeUnit.MILLISECONDS);
		}
	}

	public AbstractAudioDeviceConfig getDevice(String name) {
		return client.getGenericAudioItem(name);
	}

	public PulseaudioClient getClient() {
		return client;
	}

	@Override
	public void initialize() {
		logger.debug("Initializing Pulseaudio handler.");
		Configuration conf = this.getConfig();
		super.initialize();

		if (conf.get(BRIDGE_PARAMETER_HOST) != null) {
			this.host = String.valueOf(conf.get(BRIDGE_PARAMETER_HOST));
		}
		if (conf.get(BRIDGE_PARAMETER_PORT) != null) {
			this.port = ((BigDecimal) conf.get(BRIDGE_PARAMETER_PORT))
					.intValue();
		}
		if (conf.get(BRIDGE_PARAMETER_REFRESH_INTERVAL) != null) {
			this.refreshInterval = ((BigDecimal) conf
					.get(BRIDGE_PARAMETER_REFRESH_INTERVAL)).intValue();
		}

		if (host != null && !host.isEmpty()) {
			Runnable connectRunnable = new Runnable() {
				@Override
				public void run() {
					try {
						client = new PulseaudioClient(host, port);
						if (client.isConnected()) {
							updateStatus(ThingStatus.ONLINE);
							logger.info(
									"Established connection to Pulseaudio server on Host '{}':'{}'.",
									host, port);
							startAutomaticRefresh();
						}
					} catch (IOException e) {
						logger.error("Couldn't connect to Pulsaudio server [Host '"
								+ host
								+ "':'"
								+ port
								+ "']: "
								+ e.getLocalizedMessage());
						updateStatus(ThingStatus.OFFLINE);
					}
				}
			};
			scheduler.schedule(connectRunnable, 0, TimeUnit.SECONDS);
		} else {
			logger.warn(
					"Couldn't connect to Pulseaudio server because of missing connection parameters [Host '{}':'{}'].",
					host, port);
			updateStatus(ThingStatus.OFFLINE);
		}
	}

	@Override
	public void dispose() {
		pollingJob.cancel(true);
		client.disconnect();
		super.dispose();
	}

	public boolean registerDeviceStatusListener(
			DeviceStatusListener deviceStatusListener) {
		if (deviceStatusListener == null) {
			throw new IllegalArgumentException(
					"It's not allowed to pass a null deviceStatusListener.");
		}
		return deviceStatusListeners.add(deviceStatusListener);
	}

	public boolean unregisterDeviceStatusListener(
			DeviceStatusListener deviceStatusListener) {
		return deviceStatusListeners.remove(deviceStatusListener);
	}
}
