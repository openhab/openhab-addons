/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.handler;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.maxcube.config.MaxCubeBridgeConfiguration;
import org.openhab.binding.maxcube.internal.MaxCube;
import org.openhab.binding.maxcube.internal.message.Device;
import org.openhab.binding.maxcube.internal.message.SendCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MaxCubeBridgeHandler} is the handler for a MaxCube Cube and connects it to
 * the framework. All {@link MaxCubeHandler}s use the {@link MaxCubeBridgeHandler}
 * to execute the actual commands.
 * 
 * @author Marcel Verpaalen - Initial contribution
 * 
 */
public class MaxCubeBridgeHandler extends BaseBridgeHandler  {

	private MaxCube bridge = null;

	public MaxCubeBridgeHandler(Bridge br) {
		super(br);
	}

	private Logger logger = LoggerFactory.getLogger(MaxCubeBridgeHandler.class);

	/** The refresh interval which is used to poll given MAX!Cube */
	private long refreshInterval = 10000;
	ScheduledFuture<?> refreshJob;

	private ArrayList<Device> devices = new ArrayList<Device>();
	private HashSet<String>  lastActiveDevices = new HashSet<String>();

	private boolean previousOnline = false;

	private List<DeviceStatusListener> deviceStatusListeners = new CopyOnWriteArrayList<>();

	private ScheduledFuture<?> pollingJob;
	private Runnable pollingRunnable = new Runnable() {
		@Override
		public void run() {
			refreshData();  }
	};
	private ScheduledFuture<?> sendCommandJob;
	private long sendCommandInterval = 10000;
	private Runnable sendCommandRunnable = new Runnable() {
		@Override
		public void run() {
			sendCommands(); }
	};
	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		logger.warn("No bridge commands defined.");
	}

	@Override
	public void dispose() {
		logger.debug("Handler disposed.");
		if(pollingJob!=null && !pollingJob.isCancelled()) {
			pollingJob.cancel(true);
			pollingJob = null;
		}
		if(sendCommandJob!=null && !sendCommandJob.isCancelled()) {
			sendCommandJob.cancel(true);
			sendCommandJob = null;
		}
		if (bridge != null) {
			bridge = null;
		}
	}

	@Override
	public void initialize() {
		logger.debug("Initializing MAX! Cube bridge handler.");

		MaxCubeBridgeConfiguration configuration = getConfigAs(MaxCubeBridgeConfiguration.class);
		if (configuration.refreshInterval != 0) {
			logger.debug("MaxCube refreshInterval {}.", configuration.refreshInterval);
			refreshInterval =  configuration.refreshInterval;}
		startAutomaticRefresh();
	}

	private synchronized void initializeBridge() {
		MaxCubeBridgeConfiguration configuration = getConfigAs(MaxCubeBridgeConfiguration.class);

		if (bridge == null) {
			bridge = new MaxCube (configuration.ipAddress);
			if (configuration.port != 0) {
				logger.trace("MaxCube Port {}.", configuration.port);
				bridge.setPort ( configuration.port);
			}
			if (bridge.getIp()==null) {
				bridge = null;
				updateStatus(ThingStatus.OFFLINE);
			}
		}		
	}

	private synchronized void startAutomaticRefresh() {
		if (pollingJob == null || pollingJob.isCancelled()) {
			pollingJob = scheduler.scheduleAtFixedRate(pollingRunnable, 0, refreshInterval, TimeUnit.MILLISECONDS);
		}
		if (sendCommandJob == null || sendCommandJob.isCancelled()) {
			sendCommandJob = scheduler.scheduleAtFixedRate(sendCommandRunnable, 0, sendCommandInterval, TimeUnit.MILLISECONDS);

		}
	}


	/**
	 * initiates send commands to the maxCube bridge
	 */
	private synchronized void sendCommands() {
		if (bridge==null) initializeBridge() ;
		try {
			if (bridge !=null) bridge.sendCommands();
			} catch(Exception e) {
				logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
			}
		}
	/**
	 * initiates read data from the maxCube bridge
	 */
	private synchronized void refreshData() {

		if (bridge==null){

			initializeBridge() ;
		}
		try {
			if (bridge !=null){
				bridge.refreshData();
				if (bridge.isConnectionEstablished()){
					updateStatus(ThingStatus.ONLINE);
					previousOnline = true;
					devices = bridge.getDevices();
					for (Device di : devices){
						if (lastActiveDevices.contains(di.getSerialNumber())) {
							for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
								try {
									deviceStatusListener.onDeviceStateChanged(getThing().getUID(), di);
								} catch (Exception e) {
									logger.error(
											"An exception occurred while calling the DeviceStatusListener", e);
								}
							} }
						//New device, not seen before, pass to Discovery
						else {
							for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
								try {
									deviceStatusListener.onDeviceAdded(bridge, di);
								} catch (Exception e) {
									logger.error(
											"An exception occurred while calling the DeviceStatusListener", e);
								}
								lastActiveDevices.add (di.getSerialNumber());
							}
						}
					}
				}else if (previousOnline) onConnectionLost (bridge);
			} 

		} catch(Exception e) {
			logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
		}
	}


	public Device getDeviceById(String maxCubeDeviceSerial) {
		if (bridge !=null){
			bridge.getDevice (maxCubeDeviceSerial);
		}
		return null;
	}

	public void onConnectionLost(MaxCube bridge) {
		logger.info("Bridge connection lost. Updating thing status to OFFLINE.");
		previousOnline = false;
		this.bridge = null;
		updateStatus(ThingStatus.OFFLINE);
	}

	public void onConnection(MaxCube bridge) {
		logger.info("Bridge connected. Updating thing status to ONLINE.");
		this.bridge = bridge;
		updateStatus(ThingStatus.ONLINE);
	}

	public boolean registerDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
		if (deviceStatusListener == null) {
			throw new NullPointerException("It's not allowed to pass a null deviceStatusListener.");
		}
		boolean result = deviceStatusListeners.add(deviceStatusListener);
		if (result) {
			// onUpdate();
		}
		return result;
	}

	public boolean unregisterDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
		boolean result = deviceStatusListeners.remove(deviceStatusListener);
		if (result) {
			//   onUpdate();
		}
		return result;
	}

	public void clearDeviceList(){
		lastActiveDevices=null;
	}

	/**
	 * Processes device command and sends it to the MAX!Cube Lan Gateway.
	 * 
	 * @param serialNumber
	 *            the serial number of the device as String
	 * @param channelUID
	 *            the ChannelUID used to send the command
	 * @param command
	 *            the command data
	 */
	public void processCommand(SendCommand sendCommand) {
		if (bridge !=null){
			bridge.queueCommand (sendCommand);
		} else{
			logger.warn("Bridge not connected. Cannot set send command.");
		}


	}


}