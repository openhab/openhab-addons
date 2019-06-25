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
package org.openhab.binding.homepilot.internal.discovery;

import static org.openhab.binding.homepilot.HomePilotBindingConstants.CHANNEL_POSITION;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.homepilot.HomePilotBindingConstants;
import org.openhab.binding.homepilot.internal.HomePilotDevice;
import org.openhab.binding.homepilot.internal.handler.HomePilotBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomePilotDeviceDiscoveryService} is used to discover devices that
 * are connected to a Homematic gateway.
 *
 * @author Steffen Stundzig - Initial contribution
 */
@NonNullByDefault
// @Component(service = DiscoveryService.class, immediate = false,
// configurationPid = "discovery.homepilot")
public class HomePilotDeviceDiscoveryService extends AbstractDiscoveryService {

	private static final Logger logger = LoggerFactory.getLogger(HomePilotDeviceDiscoveryService.class);
	private static final int DISCOVER_TIMEOUT_SECONDS = 30;

	private HomePilotBridgeHandler bridgeHandler;

	@Nullable
	private Future<?> scanFuture;

	public HomePilotDeviceDiscoveryService(HomePilotBridgeHandler bridgeHandler) {
		super(Collections.singleton(new ThingTypeUID(HomePilotBindingConstants.BINDING_ID, "-")),
				DISCOVER_TIMEOUT_SECONDS, false);
		this.bridgeHandler = bridgeHandler;
	}

	public void activate() {
		logger.debug("activate");
		super.activate(null);
	}

	@Override
	protected void startScan() {
		logger.info("Starting HomePilot discovery scan");
		loadDevices();
	}

	@Override
	public synchronized void stopScan() {
		logger.info("Stopping HomePilot discovery scan");
		waitForScanFinishing();
		super.stopScan();
	}

	/**
	 * Waits for the discovery scan to finish and then returns.
	 */
	public void waitForScanFinishing() {
		if (scanFuture != null) {
			logger.info("Waiting for finishing HomePilot device discovery scan");
			try {
				scanFuture.get();
				logger.debug("HomePilot device discovery scan finished");
			} catch (CancellationException ex) {
				// ignore
			} catch (Exception ex) {
				logger.error("Error waiting for device discovery scan: {}", ex.getMessage(), ex);
			}
		}
	}

	/**
	 * Starts a thread which loads all HomePilot devices connected to the gateway.
	 */
	private void loadDevices() {
		if (scanFuture == null) {
			scanFuture = scheduler.submit(new Runnable() {

				@Override
				public void run() {
					try {
						final List<HomePilotDevice> allKnownDevices = bridgeHandler.getGateway().loadAllDevices();
						// discover all things
						for (HomePilotDevice device : allKnownDevices) {
							deviceDiscovered(device);
						}

						logger.debug("Finished HomePilot device discovery scan on gateway '{}'",
								bridgeHandler.getGateway().getId());
					} catch (Throwable ex) {
						logger.error(ex.getMessage(), ex);
					} finally {
						scanFuture = null;
						// bridgeHandler.setOfflineStatus();
						removeOlderResults(getTimestampOfLastScan());
					}
				}
			});
		} else {
			logger.debug("HomePilot devices discovery scan in progress");
		}
	}

	// TODO
	// public void deviceRemoved(HomePilotDevice device) {
	// ThingUID thingUID = createUID(device);
	// thingRemoved(thingUID);
	// }

	public void deviceDiscovered(final HomePilotDevice device) {
		// logger.info("deviceDiscoverd " + device);
		final String label = device.getName() != null ? device.getName() : "unknown";
		final ThingUID thingUID = createUID(device);

		final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
				.withBridge(bridgeHandler.getThing().getUID()).withLabel(label)
				.withProperty("description", device.getDescription())
				.withProperty(CHANNEL_POSITION, device.getPosition()).build();
		thingDiscovered(discoveryResult);
	}

	private ThingUID createUID(final HomePilotDevice device) {
		return new ThingUID(device.getTypeUID(), bridgeHandler.getThing().getUID(), device.getDeviceId());
	}
}
