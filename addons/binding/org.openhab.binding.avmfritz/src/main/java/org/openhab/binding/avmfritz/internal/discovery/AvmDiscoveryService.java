/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.discovery;

import static org.openhab.binding.avmfritz.BindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.avmfritz.BindingConstants;
import org.openhab.binding.avmfritz.handler.BoxHandler;
import org.openhab.binding.avmfritz.internal.ahamodel.DeviceModel;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaDiscoveryCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discover all AHA (AVM Home Automation) devices connected to a FRITZ!Box
 * device.
 *
 * @author Robert Bausdorf
 * 
 */
public class AvmDiscoveryService extends AbstractDiscoveryService {

	/**
	 * Logger
	 */
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Maximum time to search for devices.
	 */
	private final static int SEARCH_TIME = 30;

	/**
	 * Initial delay in s for scanning job.
	 */
	private final static int INITIAL_DELAY = 5;
	
	/**
	 * Scan interval in s for scanning job.
	 */
	private final static int SCAN_INTERVAL = 180;
	/**
	 * Handler of the bridge of which devices have to be discovered.
	 */
	private BoxHandler bridgeHandler;
	/**
	 * Job which will do the FRITZ!Box background scanning
	 */
	private FritzScan scanningRunnable;
	/**
	 * Schedule for scanning
	 */
	private ScheduledFuture<?> scanningJob;

	public AvmDiscoveryService(BoxHandler bridgeHandler) {
		super(BindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME);
		logger.debug("initialize discovery service");
		this.bridgeHandler = bridgeHandler;
		this.scanningRunnable = new FritzScan(this);
		if (bridgeHandler == null) {
			logger.warn("no bridge handler for scan given");
		}
		this.activate(null);
	}

	/**
	 * Called from the UI when starting a search.
	 */
	@Override
	public void startScan() {
		logger.debug("starting scan on bridge "
				+ bridgeHandler.getThing().getUID());
		FritzAhaDiscoveryCallback callback = new FritzAhaDiscoveryCallback(
				bridgeHandler.getWebInterface(), this);
		bridgeHandler.getWebInterface().asyncGet(callback);
	}

	/**
	 * Stops a running scan.
	 */
	@Override
	protected synchronized void stopScan() {
		super.stopScan();
		removeOlderResults(getTimestampOfLastScan());
	}

	/**
	 * Add one discovered AHA device to inbox.
	 * @param device Device model received from a FRITZ!Box
	 */
	public void onDeviceAddedInternal(DeviceModel device) {
		ThingUID thingUID = this.bridgeHandler.getThingUID(device);
		if (thingUID != null) {
			ThingUID bridgeUID = bridgeHandler.getThing().getUID();
			Map<String, Object> properties = new HashMap<>(1);
			properties.put(THING_AIN, device.getIdentifier());
			properties.put(SERIAL_NUMBER, device.getIdentifier());
			
			DiscoveryResult discoveryResult = DiscoveryResultBuilder
					.create(thingUID).withProperties(properties)
					.withRepresentationProperty(device.getIdentifier())
					.withBridge(bridgeUID).withLabel(device.getName()).build();

			thingDiscovered(discoveryResult);
		} else {
			logger.debug("discovered unsupported device with id {}",
					device.getIdentifier());
		}
	}

	/**
	 * Starts background scanning for attached devices.
	 */
	@Override
	protected void startBackgroundDiscovery() {
		if (scanningJob == null || scanningJob.isCancelled()) {
			logger.debug("start background scanning job");
			this.scanningJob = AvmDiscoveryService.scheduler
					.scheduleWithFixedDelay(this.scanningRunnable, 
							INITIAL_DELAY, SCAN_INTERVAL, TimeUnit.SECONDS);
		} else {
			logger.debug("scanningJob active");
		}
	}

	/**
	 * Stops background scanning for attached devices.
	 */
	@Override
	protected void stopBackgroundDiscovery() {
		if (scanningJob != null && !scanningJob.isCancelled()) {
			scanningJob.cancel(false);
			scanningJob = null;
		}
	}

	/**
	 * Scanning worker class.
	 */
	public class FritzScan implements Runnable {
		/**
		 * Handler for delegation to callbacks.
		 */
		private AvmDiscoveryService service;
		/**
		 * Constructor.
		 * @param handler
		 */
		public FritzScan(AvmDiscoveryService service) {
			this.service = service;
		}
		/**
		 * Poll the FRITZ!Box websevice one time. 
		 */
		@Override
		public void run() {
			logger.debug("starting scan on bridge "
					+ bridgeHandler.getThing().getUID());
			FritzAhaDiscoveryCallback callback = new FritzAhaDiscoveryCallback(
					bridgeHandler.getWebInterface(), service);
			bridgeHandler.getWebInterface().asyncGet(callback);
		}
	}
}
