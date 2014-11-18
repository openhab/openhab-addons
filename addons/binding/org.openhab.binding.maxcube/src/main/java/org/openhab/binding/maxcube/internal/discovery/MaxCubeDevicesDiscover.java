/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.discovery;


import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.maxcube.MaxCubeBinding;
import org.openhab.binding.maxcube.config.MaxCubeConfiguration;
import org.openhab.binding.maxcube.internal.MaxCubeBridge;
import org.openhab.binding.maxcube.internal.handler.DeviceStatusListener;
import org.openhab.binding.maxcube.internal.handler.MaxCubeBridgeHandler;
import org.openhab.binding.maxcube.internal.message.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxCubeDevicesDiscover} class is used to discover Max!Cube devices that  
 * are connected to the Lan gateway. 
 * 
 * @author Marcel Verpaalen - Initial contribution
 */
public class MaxCubeDevicesDiscover  extends AbstractDiscoveryService implements DeviceStatusListener {

	private final static Logger logger = LoggerFactory.getLogger(MaxCubeDevicesDiscover.class);

	private MaxCubeBridgeHandler maxCubeBridgeHandler;

	public MaxCubeDevicesDiscover( MaxCubeBridgeHandler maxCubeBridgeHandler) {
		super(MaxCubeBinding.SUPPORTED_DEVICE_THING_TYPES_UIDS, 10,true);
		this.maxCubeBridgeHandler = maxCubeBridgeHandler;
	}

	public void activate() {
		maxCubeBridgeHandler.registerDeviceStatusListener(this);
	}

	public void deactivate() {
		maxCubeBridgeHandler.unregisterDeviceStatusListener(this);
	}

	@Override
	public Set<ThingTypeUID> getSupportedThingTypes() {
		return MaxCubeBinding.SUPPORTED_DEVICE_THING_TYPES_UIDS;
	}

	@Override
	public void onDeviceAdded(MaxCubeBridge bridge, Device device) {
		logger.debug("Adding new Max!Cube {} with id '{}' to smarthome inbox", device.getType(), device.getSerialNumber());
		ThingUID thingUID = null;
		String deviceid = device.getType() + "_" +  device.getSerialNumber();
		deviceid = deviceid.replaceAll("\\s+","");
		deviceid = deviceid.replaceAll("\\+","Plus");
		switch (device.getType()) {
		case WallMountedThermostat:
		case HeatingThermostat:
		case HeatingThermostatPlus:
			thingUID = new ThingUID(MaxCubeBinding.HEATHINGTHERMOSTAT_THING_TYPE,deviceid);
			break;
		case ShutterContact:
		case EcoSwitch:
			
			thingUID = new ThingUID(MaxCubeBinding.SWITCH_THING_TYPE, deviceid );
			break;
		default:
			break;
		}
		if(thingUID!=null) {
			ThingUID bridgeUID = maxCubeBridgeHandler.getThing().getUID();
			DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
					.withProperty(MaxCubeConfiguration.SERIAL_NUMBER, device.getSerialNumber())
					.withBridge(bridgeUID)
					.withLabel( device.getType() + ": " + device.getName() + " (" + device.getSerialNumber() +")")
					.build();
			thingDiscovered(discoveryResult);
		} else {
			logger.debug("Discovered Max!Cube item is unsupported: type '{}' with id '{}'", device.getType(), device.getSerialNumber());
		}
	}

	@Override
	protected void startScan() {
		//this can be ignored here as we discover via the bridge
	}

	@Override
	public void onDeviceStateChanged(ThingUID bridge, Device device) {
		//this can be ignored here
	}

	@Override
	public void onDeviceRemoved(MaxCubeBridge bridge, Device device) {
		//this can be ignored here
	}
}
