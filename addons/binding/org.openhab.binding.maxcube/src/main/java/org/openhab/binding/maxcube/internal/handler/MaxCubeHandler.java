/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.handler;

import static org.openhab.binding.maxcube.MaxCubeBinding.CHANNEL_ACTUALTEMP;
import static org.openhab.binding.maxcube.MaxCubeBinding.CHANNEL_BATTERY;
import static org.openhab.binding.maxcube.MaxCubeBinding.CHANNEL_MODE;
import static org.openhab.binding.maxcube.MaxCubeBinding.CHANNEL_SETTEMP;
import static org.openhab.binding.maxcube.MaxCubeBinding.CHANNEL_VALVE;
import static org.openhab.binding.maxcube.MaxCubeBinding.CHANNEL_SWITCH_STATE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.maxcube.config.MaxCubeConfiguration;
import org.openhab.binding.maxcube.internal.MaxCubeBridge;
import org.openhab.binding.maxcube.internal.message.Device;
import org.openhab.binding.maxcube.internal.message.EcoSwitch;
import org.openhab.binding.maxcube.internal.message.HeatingThermostat;
import org.openhab.binding.maxcube.internal.message.SendCommand;
import org.openhab.binding.maxcube.internal.message.ShutterContact;
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

	public MaxCubeHandler(Thing thing) {
		super(thing);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		final String configDeviceId = getConfigAs(MaxCubeConfiguration.class).serialNumber;
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
		// TODO Auto-generated method stub
		logger.debug("Thing {} {} disposed.", getThing(), maxCubeDeviceSerial);
		if(bridgeHandler!=null) bridgeHandler.clearDeviceList();
		super.dispose();
	}
	
	private void deviceOnlineWatchdog() {
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					MaxCubeBridgeHandler bridgeHandler = getMaxCubeBridgeHandler();
					if(bridgeHandler!=null) {
						if ( bridgeHandler.getDeviceById(maxCubeDeviceSerial) == null) 	{
							updateStatus(ThingStatus.OFFLINE);
						} else updateStatus(ThingStatus.ONLINE);
							
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
				//no bridge is assigned to the device, will register it to the first found bridge 
				ArrayList<Bridge> maxCubeBridges = new ArrayList<Bridge>();
				Collection<Thing> allThings = thingRegistry.getAll();
				for ( Thing br : allThings ){
					if (br instanceof  Bridge){
						if (br.getHandler() instanceof MaxCubeBridgeHandler) maxCubeBridges.add ((Bridge) br);
					}
				}
				if (!(maxCubeBridges.isEmpty())) bridge = maxCubeBridges.get(0);
				logger.debug("maxCube LAN gateway bridge not assigned. registering automatically to {}." , bridge.getUID() );
				//TODO: Before assigning would be good to check if the item actually exists in the bridge.
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
			maxCubeBridge.processCommand (sendCommand);
		}
		else {
			logger.warn("Setting of channel {} not possible. Read-only", channelUID);
		}
	}

	@Override
	public void onDeviceStateChanged(ThingUID bridge, Device device) {
		if (device.getSerialNumber().equals (maxCubeDeviceSerial) ){
			logger.debug("Updating states of {} {} ({}) id: {}", device.getType(), device.getName(), device.getSerialNumber(), getThing().getUID()  );
			updateStatus(ThingStatus.ONLINE);
			//TODO: Could make this more intelligent by checking first if anything has changed, only then make the update
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
				updateState(new ChannelUID(getThing().getUID(), CHANNEL_SWITCH_STATE), (State) ( (ShutterContact) device).getShutterState());
				updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATTERY), (State) ( (ShutterContact) device).getBatteryLow());
				break;
			case EcoSwitch:
				updateState(new ChannelUID(getThing().getUID(), CHANNEL_SWITCH_STATE), (State) ( (EcoSwitch) device).getShutterState());
				updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATTERY), (State) ( (EcoSwitch) device).getBatteryLow());
				break;
			default:
				logger.debug("Unhandled Device {}.",device.getType());
				break;

			}}

	}

	@Override
	public void onDeviceRemoved(MaxCubeBridge bridge, Device device) {
		// not relevant here
	}

	@Override
	public void onDeviceAdded(MaxCubeBridge bridge, Device device) {
		// not relevant here

	}
}
