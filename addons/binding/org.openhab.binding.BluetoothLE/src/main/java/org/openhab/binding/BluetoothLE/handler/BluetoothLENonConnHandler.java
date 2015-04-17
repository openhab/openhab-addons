/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.BluetoothLE.handler;

import static org.openhab.binding.BluetoothLE.BluetoothLEBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.BluetoothLE.discovery.BluetoothLEDiscoveryService;
import org.openhab.binding.BluetoothLE.protocol.ScanRecord;
import org.openhab.binding.BluetoothLE.protocol.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BluetoothLENonConnHandler} is responsible for handling Bluetooth low energy devices, to which
 * it is not possible to connect. They send periodically an EVT_LE_ADVERTISING_REPORT frame in responds to a low energy device scan.
 * 
 * @author Patrick Ammann - Initial contribution
 */
public class BluetoothLENonConnHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(BluetoothLENonConnHandler.class);

    private static final int DEFAULT_ALLOWED_FRAME_TIMEOUT = (2 * 60) + 12; // in seconds
    private static final int DEFAULT_REFRESH_INTERVAL      = (1 * 30); // in seconds 

    BluetoothLEDiscoveryService discoveryService;
    //DiscoveryServiceRegistry discoveryServiceRegistry;
    
    private String deviceAddress = null;
    private ScanResult lastSR = null;

    
    ScheduledFuture<?> refreshJob;

	public BluetoothLENonConnHandler(Thing thing, BluetoothLEDiscoveryService discoveryService, DiscoveryServiceRegistry discoveryServiceRegistry) {
		super(thing);
		this.discoveryService = discoveryService;
		/*this.discoveryServiceRegistry = discoveryServiceRegistry;
		if (this.discoveryServiceRegistry != null) {
			this.discoveryServiceRegistry.addDiscoveryListener(this);
		}*/
	}

    @Override
    public void initialize() {
        logger.debug("Initializing BluetoothLEHandler handler.");
    	super.initialize();

        Configuration config = getThing().getConfiguration();
        deviceAddress = (String)config.get("device_address");
        
        startAutomaticRefresh();
    }

    @Override
    public void dispose() {
    	refreshJob.cancel(true);
    }
    
	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		/*
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
	            case CHANNEL_TEMPERATURE:
	                updateState(channelUID, getTemperature());
	                break;
	            case CHANNEL_BATTERY:
	                updateState(channelUID, getBattery());
	                break;
	            default:
	                logger.debug("Command received for an unknown channel: {}", channelUID.getId());
	                break;
            }
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
        */
	}
    
	private void startAutomaticRefresh() {
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					if (updateStatus()) {
		                updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE), getTemperature());
		                updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATTERY), getBattery());
					}
				} catch(Exception e) {
					logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
				}
			}
		};
		refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, DEFAULT_REFRESH_INTERVAL, TimeUnit.SECONDS);
	}
	
	private boolean updateStatus() {
		ScanResult sr = discoveryService.getScanResult(deviceAddress);
		if (lastSR != sr && sr != null) {
			long estimatedTime = (System.nanoTime() - sr.getTimestampNanos()) / 1000000000;
			if (estimatedTime > DEFAULT_ALLOWED_FRAME_TIMEOUT) {
				getThing().setStatus(ThingStatus.OFFLINE);
			} else {
				getThing().setStatus(ThingStatus.ONLINE);
			}
			lastSR = sr;
			return true;
		} else {
			getThing().setStatus(ThingStatus.OFFLINE);
			return false;
		}
	}
	
	private State getTemperature() {
		ScanResult sr = discoveryService.getScanResult(deviceAddress);
		if (sr != null)
		{
			ScanRecord r = sr.getScanRecord();
			byte[] b = r.getServiceData(UUID_HEALTH_THERMOMETER);
			if (b != null) {
				if ((b[3] & 0x01) == 0) {
					// org.bluetooth.unit.thermodynamic_temperature.degree_celsius
					int d = b[0] + (b[1]<<8) + (b[2]<<16);	
					double temperature = d * Math.pow(10, -2);
					return new DecimalType(temperature);
				}
			}
		}
		return UnDefType.UNDEF;
	}
	
	private State getBattery() {
		ScanResult sr = discoveryService.getScanResult(deviceAddress);
		if (sr != null)
		{
			ScanRecord r = sr.getScanRecord();	
			byte[] b = r.getServiceData(UUID_BATTERY);
			if (b != null) {
				// org.bluetooth.characteristic.battery_level
				return new PercentType(b[0]);
			}
		}
		return UnDefType.UNDEF;
	}
}
