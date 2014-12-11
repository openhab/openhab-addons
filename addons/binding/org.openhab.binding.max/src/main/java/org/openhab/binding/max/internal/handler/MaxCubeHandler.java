/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
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
 * The {@link MaxCubeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author Marcel Verpaalen - Initial contribution
 */
public class MaxCubeHandler extends BaseThingHandler implements DeviceStatusListener {

	private Logger logger = LoggerFactory.getLogger(MaxCubeHandler.class);
	private int refresh = 60; // refresh every minute as default 
	ScheduledFuture<?> refreshJob;
	private MaxCubeBridgeHandler bridgeHandler;

	private String maxCubeDeviceSerial;
	private boolean newDevice = true;

	public MaxCubeHandler(Thing thing) {
		super(thing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {

		Configuration config = getThing().getConfiguration();
		final String configDeviceId = (String) config.get(MaxBinding.SERIAL_NUMBER);

		if (configDeviceId != null) {
			maxCubeDeviceSerial = configDeviceId;
		}
		if (maxCubeDeviceSerial != null){
			logger.debug("Initialized maxcube device handler for {}.", maxCubeDeviceSerial);}
		else {
			logger.debug("Initialized maxcube device missing serialNumber configuration... troubles ahead");
		}
		//until we get an update put the Thing offline
		updateStatus(ThingStatus.OFFLINE);
		deviceOnlineWatchdog();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#dispose()
	 */
	@Override
	public void dispose() {
		logger.debug("Thing {} {} disposed.", getThing().getUID(), maxCubeDeviceSerial);
		if(bridgeHandler!=null) bridgeHandler.clearDeviceList();
		if(refreshJob!=null && !refreshJob.isCancelled()) {
			refreshJob.cancel(true);
			refreshJob = null;
		}
		updateStatus(ThingStatus.OFFLINE);
		if (bridgeHandler !=null) bridgeHandler.unregisterDeviceStatusListener(this);
		super.dispose();
	}

	private void deviceOnlineWatchdog() {
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					MaxCubeBridgeHandler bridgeHandler = getMaxCubeBridgeHandler();
					if(bridgeHandler!=null) {
						if ( bridgeHandler.getDevice(maxCubeDeviceSerial) == null) 	{
							updateStatus(ThingStatus.OFFLINE);
						} else {
							updateStatus(ThingStatus.ONLINE);
						}

					} else {
						logger.debug("Bridge for maxcube device {} not found.", maxCubeDeviceSerial);
						updateStatus(ThingStatus.OFFLINE);
					}

				} catch(Exception e) {
					logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
				}

			}
		};

		refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, refresh, TimeUnit.SECONDS);
	}


	private synchronized MaxCubeBridgeHandler getMaxCubeBridgeHandler() {

		if(this.bridgeHandler==null) {
			Bridge bridge = getBridge();
			if (bridge == null) {
				return null;
			}
			ThingHandler handler = bridge.getHandler();
			if (handler instanceof MaxCubeBridgeHandler) {
				this.bridgeHandler = (MaxCubeBridgeHandler) handler;
				this.bridgeHandler.registerDeviceStatusListener(this);
			} else {
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
		//TODO: handle refreshtype commands
		MaxCubeBridgeHandler maxCubeBridge = getMaxCubeBridgeHandler();
		if (maxCubeBridge == null) {
			logger.warn("maxCube LAN gateway bridge handler not found. Cannot handle command without bridge.");
			return;
		}
		if (maxCubeDeviceSerial == null){
			logger.warn("Serial number missing. Can't send command to device '{}'", getThing());
			return;
		}

		if(channelUID.getId().equals(CHANNEL_SETTEMP) || channelUID.getId().equals(CHANNEL_MODE)) {
			SendCommand sendCommand = new SendCommand (maxCubeDeviceSerial,channelUID,command);
			maxCubeBridge.queueCommand (sendCommand);
		}
		else {
			logger.warn("Setting of channel {} not possible. Read-only", channelUID);
		}
	}

	@Override
	public void onDeviceStateChanged(ThingUID bridge, Device device) {
		if (device.getSerialNumber().equals (maxCubeDeviceSerial) ){
			updateStatus(ThingStatus.ONLINE);
			if (device.isUpdated() || newDevice){
				newDevice = false;
				logger.debug("Updating states of {} {} ({}) id: {}", device.getType(), device.getName(), device.getSerialNumber(), getThing().getUID()  );
				switch (device.getType()) {
				case WallMountedThermostat:
				case HeatingThermostat:
				case HeatingThermostatPlus:
					updateState(new ChannelUID(getThing().getUID(), CHANNEL_SETTEMP), (State) ( (HeatingThermostat) device).getTemperatureSetpoint());
					updateState(new ChannelUID(getThing().getUID(), CHANNEL_ACTUALTEMP), (State) ( (HeatingThermostat) device).getTemperatureActual());
					updateState(new ChannelUID(getThing().getUID(), CHANNEL_MODE), (State) ( (HeatingThermostat) device).getModeString());
					updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATTERY), (State) ( (HeatingThermostat) device).getBatteryLow());
					updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE), (State) ( (HeatingThermostat) device).getValvePosition());
					break;
				case ShutterContact:
					updateState(new ChannelUID(getThing().getUID(), CHANNEL_CONTACT_STATE), (State) ( (ShutterContact) device).getShutterState());
					updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATTERY), (State) ( (ShutterContact) device).getBatteryLow());
					break;
				case EcoSwitch:
					updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATTERY), (State) ( (EcoSwitch) device).getBatteryLow());
					break;
				default:
					logger.debug("Unhandled Device {}.",device.getType());
					break;

				}
			}
			else logger.debug("No changes for {} {} ({}) id: {}", device.getType(), device.getName(), device.getSerialNumber(), getThing().getUID()  );
		}
	}

	@Override
	public void onDeviceRemoved(MaxCubeBridgeHandler bridge, Device device) {
		newDevice = true;
	}

	@Override
	public void onDeviceAdded(Bridge bridge, Device device) {
		newDevice = true;
	}

}
