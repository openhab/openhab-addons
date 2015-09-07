/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.handler;

import static org.openhab.binding.max.MaxBinding.CHANNEL_ACTUALTEMP;
import static org.openhab.binding.max.MaxBinding.CHANNEL_BATTERY;
import static org.openhab.binding.max.MaxBinding.CHANNEL_CONTACT_STATE;
import static org.openhab.binding.max.MaxBinding.CHANNEL_MODE;
import static org.openhab.binding.max.MaxBinding.CHANNEL_SETTEMP;
import static org.openhab.binding.max.MaxBinding.CHANNEL_VALVE;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.max.MaxBinding;
import org.openhab.binding.max.internal.message.Device;
import org.openhab.binding.max.internal.message.EcoSwitch;
import org.openhab.binding.max.internal.message.HeatingThermostat;
import org.openhab.binding.max.internal.message.SendCommand;
import org.openhab.binding.max.internal.message.ShutterContact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxDevicesHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author Marcel Verpaalen - Initial contribution
 */
public class MaxDevicesHandler extends BaseThingHandler implements DeviceStatusListener {

	private Logger logger = LoggerFactory.getLogger(MaxDevicesHandler.class);
	ScheduledFuture<?> refreshJob;
	private MaxCubeBridgeHandler bridgeHandler;

	private String maxDeviceSerial;
	private boolean forceRefresh = true;
	private boolean propertiesSet = false;
	private boolean configSet = false;

	public MaxDevicesHandler(Thing thing) {
		super(thing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {

		Configuration config = getThing().getConfiguration();
		final String configDeviceId = (String) config.get(MaxBinding.PROPERTY_SERIAL_NUMBER);

		if (configDeviceId != null) {
			maxDeviceSerial = configDeviceId;
		}
		if (maxDeviceSerial != null) {
			logger.debug("Initialized MAX! device handler for {}.", maxDeviceSerial);
		} else {
			logger.debug("Initialized MAX! device missing serialNumber configuration... troubles ahead");
		}
		// until we get an update put the Thing offline
		propertiesSet = false;
		configSet = false;
		forceRefresh = true;
		updateStatus(ThingStatus.OFFLINE);
		getMaxCubeBridgeHandler();
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
		logger.debug("Thing {} {} disposed.", getThing().getUID(), maxDeviceSerial);
		super.dispose();
	}

	private synchronized MaxCubeBridgeHandler getMaxCubeBridgeHandler() {

		if (this.bridgeHandler == null) {
			Bridge bridge = getBridge();
			if (bridge == null) {
				logger.debug("Required bridge not defined for device {}.", maxDeviceSerial);
				return null;
			}
			ThingHandler handler = bridge.getHandler();
			if (handler instanceof MaxCubeBridgeHandler) {
				this.bridgeHandler = (MaxCubeBridgeHandler) handler;
				this.bridgeHandler.registerDeviceStatusListener(this);
				forceRefresh = true;
			} else {
				logger.debug("No available bridge handler found for {} bridge {} .", maxDeviceSerial,
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
		MaxCubeBridgeHandler maxCubeBridge = getMaxCubeBridgeHandler();
		if (maxCubeBridge == null) {
			logger.warn("MAX! Cube LAN gateway bridge handler not found. Cannot handle command without bridge.");
			return;
		}
		if (command instanceof RefreshType) {
			forceRefresh = true;
			maxCubeBridge.handleCommand(channelUID, command);
			return;
		}
		if (maxDeviceSerial == null) {
			logger.warn("Serial number missing. Can't send command to device '{}'", getThing());
			return;
		}

		if (channelUID.getId().equals(CHANNEL_SETTEMP) || channelUID.getId().equals(CHANNEL_MODE)) {
			SendCommand sendCommand = new SendCommand(maxDeviceSerial, channelUID, command);
			maxCubeBridge.queueCommand(sendCommand);
		} else {
			logger.warn("Setting of channel {} not possible. Read-only", channelUID);
		}
	}

	@Override
	public void onDeviceStateChanged(ThingUID bridge, Device device) {
		if (device.getSerialNumber().equals(maxDeviceSerial)) {
			if (!device.isLinkStatusError()) updateStatus(ThingStatus.ONLINE);
			else updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
			if (!propertiesSet) setProperties(device);
			if (!configSet) setDeviceConfiguration(device);
			if (device.isUpdated() || forceRefresh) {
				logger.debug("Updating states of {} {} ({}) id: {}", device.getType(), device.getName(),
						device.getSerialNumber(), getThing().getUID());
				switch (device.getType()) {
				case WallMountedThermostat:
				case HeatingThermostat:
				case HeatingThermostatPlus:
					updateState(new ChannelUID(getThing().getUID(), CHANNEL_SETTEMP),
							(State) ((HeatingThermostat) device).getTemperatureSetpoint());
					updateState(new ChannelUID(getThing().getUID(), CHANNEL_MODE),
							(State) ((HeatingThermostat) device).getModeString());
					updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATTERY),
							(State) ((HeatingThermostat) device).getBatteryLow());
					updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE),
							(State) ((HeatingThermostat) device).getValvePosition());
					State actualTemp =  ((HeatingThermostat) device).getTemperatureActual();
					if ( actualTemp != DecimalType.ZERO) {
						updateState(new ChannelUID(getThing().getUID(), CHANNEL_ACTUALTEMP),
								actualTemp);
					} 
					break;
				case ShutterContact:
					updateState(new ChannelUID(getThing().getUID(), CHANNEL_CONTACT_STATE),
							(State) ((ShutterContact) device).getShutterState());
					updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATTERY),
							(State) ((ShutterContact) device).getBatteryLow());
					break;
				case EcoSwitch:
					updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATTERY),
							(State) ((EcoSwitch) device).getBatteryLow());
					break;
				default:
					logger.debug("Unhandled Device {}.", device.getType());
					break;

				}
				forceRefresh = false;
			} else
				logger.debug("No changes for {} {} ({}) id: {}", device.getType(), device.getName(),
						device.getSerialNumber(), getThing().getUID());
		}
	}

	@Override
	public void onDeviceRemoved(MaxCubeBridgeHandler bridge, Device device) {
		if (device.getSerialNumber().equals(maxDeviceSerial)) {
			bridgeHandler.unregisterDeviceStatusListener(this);
			bridgeHandler = null;
			forceRefresh = true;
			updateStatus(ThingStatus.OFFLINE);
		}
	}

	@Override
	public void onDeviceAdded(Bridge bridge, Device device) {
		forceRefresh = true;
	}

	/**
	 * Set the forceRefresh flag to ensure update when next data is coming
	 */
	public void setForceRefresh() {
		forceRefresh = true;
	}

	/**
	 * Set the properties for this device
	 * @param device
	 */
	private void setProperties(Device device) {
		try {
			logger.debug ("MAX! {} {} properties update",device.getType().toString(),device.getSerialNumber());
			Map<String, String> properties = editProperties();
			properties.put(Thing.PROPERTY_MODEL_ID, device.getType().toString());
			properties.put(Thing.PROPERTY_SERIAL_NUMBER,device.getSerialNumber());
			properties.put(Thing.PROPERTY_VENDOR, MaxBinding.PROPERTY_VENDOR_NAME);
			updateProperties(properties);
			//TODO: Remove this once UI is displaying this info
			for (Map.Entry<String, String> entry : properties.entrySet()){
				logger.debug ("key: {}  : {}", entry.getKey(), entry.getValue());
			}
			logger.debug ("properties updated");
			propertiesSet = true;
		} catch (Exception e) {
			logger.debug("Exception occurred during property edit: {}", e.getMessage(), e);
		}
	}

	/**
	 * Set the Configurable properties for this device
	 * @param device
	 */

	private void setDeviceConfiguration(Device device) { 
		try {
			logger.debug ("MAX! {} {} configuration update",device.getType().toString(),device.getSerialNumber());
			Configuration configuration = editConfiguration();
			configuration.put(MaxBinding.PROPERTY_ROOMNAME, device.getRoomName());
			configuration.put(MaxBinding.PROPERTY_DEVICENAME, device.getName());
			configuration.put(MaxBinding.PROPERTY_RFADDRESS,device.getRFAddress());	
			updateConfiguration(configuration);
			logger.debug ("Config updated: {}",configuration.getProperties() );
			configSet = true;
		} catch (Exception e) {
			logger.debug("Exception occurred during configuration edit: {}", e.getMessage(), e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#bridgeHandlerInitialized(org.eclipse.smarthome.core.thing.binding.ThingHandler, org.eclipse.smarthome.core.thing.Bridge)
	 */
	@Override
	protected void bridgeHandlerInitialized(ThingHandler thingHandler,
			Bridge bridge) {
		logger.debug ("Bridge {} initialized for device: {}", bridge.getUID().toString() , getThing().getUID().toString());
		if (bridgeHandler != null) {
			bridgeHandler.unregisterDeviceStatusListener(this);
			bridgeHandler = null;
		}
		getMaxCubeBridgeHandler();
		super.bridgeHandlerInitialized(thingHandler, bridge);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#bridgeHandlerDisposed(org.eclipse.smarthome.core.thing.binding.ThingHandler, org.eclipse.smarthome.core.thing.Bridge)
	 */
	@Override
	protected void bridgeHandlerDisposed(ThingHandler thingHandler,
			Bridge bridge) {
		logger.debug ("Bridge {} disposed for device: {}", bridge.getUID().toString() , getThing().getUID().toString());
		bridgeHandler = null;
		forceRefresh = true;
		super.bridgeHandlerDisposed(thingHandler, bridge);
	}

}
