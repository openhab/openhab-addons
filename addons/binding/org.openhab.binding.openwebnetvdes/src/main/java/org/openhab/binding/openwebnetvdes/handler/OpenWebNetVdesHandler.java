/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnetvdes.handler;

import static org.openhab.binding.openwebnetvdes.OpenWebNetVdesBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.openwebnetvdes.OpenWebNetVdesBindingConstants;
import org.openhab.binding.openwebnetvdes.devices.BticinoDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetVdesHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author DmytroKulyanda - Initial contribution
 */
public class OpenWebNetVdesHandler extends BaseThingHandler implements DeviceStatusListener {

    private Logger logger = LoggerFactory.getLogger(OpenWebNetVdesHandler.class);

    private int refresh = 60; // refresh every minute as default
	ScheduledFuture<?> refreshJob;
	private Ip2WireBridgeHandler bridgeHandler;

	private Integer deviceWhereAddress;
	private boolean forceRefresh = true;
	
	public OpenWebNetVdesHandler(Thing thing) {
		super(thing);
	}
		
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {

		Configuration config = getThing().getConfiguration();
		final Integer configDeviceId = (Integer) config.get(OpenWebNetVdesBindingConstants.OWN_WHERE_ADDRESS);

		if (configDeviceId != null) {
			deviceWhereAddress = configDeviceId;
		}
		if (deviceWhereAddress != null) {
			logger.debug("Initialized Bticino device handler for {}.", deviceWhereAddress);
		} else {
			logger.debug("Initialized Bticino device missing serialNumber configuration... troubles ahead");
		}
		// until we get an update put the Thing offline
		updateStatus(ThingStatus.OFFLINE);
		deviceOnlineWatchdog();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#dispose()
	 */
	@Override
	public void dispose() {
		if (refreshJob != null && !refreshJob.isCancelled()) {
			refreshJob.cancel(true);
			refreshJob = null;
		}
		updateStatus(ThingStatus.OFFLINE);
		if (bridgeHandler != null)
			bridgeHandler.clearDeviceList();
		if (bridgeHandler != null)
			bridgeHandler.unregisterDeviceStatusListener(this);
		bridgeHandler = null;
		logger.debug("Thing {} {} disposed.", getThing().getUID(), deviceWhereAddress);
		super.dispose();
	}

	private void deviceOnlineWatchdog() {
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					Ip2WireBridgeHandler bridgeHandler = getBridgeHandler();
					if (bridgeHandler != null) {
						if (bridgeHandler.getDevice(deviceWhereAddress) == null) {
							updateStatus(ThingStatus.OFFLINE);
							bridgeHandler = null;
						} else {
							updateStatus(ThingStatus.ONLINE);
						}

					} else {
						logger.debug("Bridge for maxcube device {} not found.", deviceWhereAddress);
						updateStatus(ThingStatus.OFFLINE);
					}

				} catch (Exception e) {
					logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
					bridgeHandler = null;
				}

			}
		};

		refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, refresh, TimeUnit.SECONDS);
	}

	private synchronized Ip2WireBridgeHandler getBridgeHandler() {

		if (this.bridgeHandler == null) {
			Bridge bridge = getBridge();
			if (bridge == null) {
				logger.debug("Required bridge not defined for device {}.", deviceWhereAddress);
				return null;
			}
			ThingHandler handler = bridge.getHandler();
			if (handler instanceof Ip2WireBridgeHandler) {
				this.bridgeHandler = (Ip2WireBridgeHandler) handler;
				this.bridgeHandler.registerDeviceStatusListener(this);
			} else {
				logger.debug("No available bridge handler found for {} bridge {} .", deviceWhereAddress,
						bridge.getUID());
				return null;
			}
		}
		return this.bridgeHandler;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {

		Ip2WireBridgeHandler bridgeHandler = getBridgeHandler();
		if (bridgeHandler == null) {
			logger.warn("MAX! Cube LAN gateway bridge handler not found. Cannot handle command without bridge.");
			return;
		}
		if (command instanceof RefreshType) {
			forceRefresh = true;
			bridgeHandler.handleCommand(channelUID, command);
			return;
		}
		if (deviceWhereAddress == null) {
			logger.warn("Where Address missing. Can't send command to device '{}'", getThing());
			return;
		}
		
		if (channelUID.getId().equals(CHANNEL_SWITCH_ON_OFF_CAMERA) || channelUID.getId().equals(CHANNEL_OPEN_LOCK)) {
			OwnRequest sendCommand = new OwnRequest(deviceWhereAddress, channelUID, command);
			bridgeHandler.queueCommand(sendCommand);
		} else {
			logger.warn("Setting of channel {} not possible. Read-only", channelUID);
		}
	}

	@Override
	public void onDeviceStateChanged(ThingUID bridge, BticinoDevice device) {
		if (deviceWhereAddress.equals(device.getWhereAddress())) {
			updateStatus(ThingStatus.ONLINE);
			if (device.isUpdated() || forceRefresh) {
				forceRefresh = false;
				logger.debug("Updating states of {} ({}) id: {}", device.getType(),
						device.getWhereAddress(), getThing().getUID());
				switch (device.getType()) {				
				case VIDEO_CAMERA_ENTRANCE_PANEL:
				case INDOOR_CAMERA:
				case DOOR_LOCK_ACTUATOR:					
					break;				
				default:
					logger.debug("Unhandled Device {}.", device.getType());
					break;

				}
			} else
				logger.debug("No changes for {} ({}) id: {}", device.getType(), 
						device.getWhereAddress(), getThing().getUID());
		}
	}

	@Override
	public void onDeviceRemoved(Ip2WireBridgeHandler bridge, BticinoDevice device) {
		if (device.getWhereAddress() == deviceWhereAddress.intValue()) {
			bridgeHandler.unregisterDeviceStatusListener(this);
			bridgeHandler = null;
			forceRefresh = true;
			getThing().setStatus(ThingStatus.OFFLINE);
		}
	}

	@Override
	public void onDeviceAdded(Bridge bridge, BticinoDevice device) {
		forceRefresh = true;
	}

}
