/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.factory;

import java.util.Hashtable;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.maxcube.MaxCubeBinding;
import org.openhab.binding.maxcube.config.MaxCubeBridgeConfiguration;
import org.openhab.binding.maxcube.config.MaxCubeConfiguration;
import org.openhab.binding.maxcube.internal.discovery.MaxCubeDevicesDiscover;
import org.openhab.binding.maxcube.internal.handler.MaxCubeBridgeHandler;
import org.openhab.binding.maxcube.internal.handler.MaxCubeHandler;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxCubeHandlerFactory} is responsible for creating things and thing 
 * handlers.
 * 
 * @author Marcel Verpaalen - Initial contribution
 */

public class MaxCubeHandlerFactory extends BaseThingHandlerFactory {

	private Logger logger = LoggerFactory.getLogger(MaxCubeHandlerFactory.class);
	private ServiceRegistration<?> discoveryServiceReg;


	@Override
	public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
			ThingUID thingUID, ThingUID bridgeUID) {

		if (MaxCubeBinding.CubeBridge_THING_TYPE.equals(thingTypeUID)) {
			ThingUID cubeBridgeUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
			return super.createThing(thingTypeUID, configuration, cubeBridgeUID, null);
		}
		if (MaxCubeBinding.HEATHINGTHERMOSTAT_THING_TYPE.equals(thingTypeUID)) {
			ThingUID thermostatUID = getMaxCubeDeviceUID(thingTypeUID, thingUID, configuration, bridgeUID);
			return super.createThing(thingTypeUID, configuration, thermostatUID , bridgeUID);
		}
		if (MaxCubeBinding.SWITCH_THING_TYPE.equals(thingTypeUID)) {
			ThingUID thermostatUID = getMaxCubeDeviceUID(thingTypeUID, thingUID, configuration, bridgeUID);
			return super.createThing(thingTypeUID, configuration, thermostatUID , bridgeUID);
		}
		throw new IllegalArgumentException("The thing type " + thingTypeUID
				+ " is not supported by the MaxCube binding.");
	}

	@Override
	public boolean supportsThingType(ThingTypeUID thingTypeUID) {
		return MaxCubeBinding.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
	}


	private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID,
			Configuration configuration) {
		if (thingUID == null) {
			//TODO: Should this look at IP or at serial #
			String ipAddress = (String) configuration.get(MaxCubeBridgeConfiguration.IP_ADDRESS);
			thingUID = new ThingUID(thingTypeUID, ipAddress);
		}
		return thingUID;
	}

	private ThingUID getMaxCubeDeviceUID(ThingTypeUID thingTypeUID, ThingUID thingUID,
			Configuration configuration , ThingUID bridgeUID ) {
		String SerialNumber = (String) configuration.get(MaxCubeConfiguration.SERIAL_NUMBER);

		if (thingUID == null) {
			thingUID = new ThingUID(thingTypeUID, "Device" + SerialNumber , bridgeUID.getId());
		}
		return thingUID;
	}



	private void registerDeviceDiscoveryService(MaxCubeBridgeHandler maxCubeBridgeHandler) {
		MaxCubeDevicesDiscover discoveryService = new MaxCubeDevicesDiscover(maxCubeBridgeHandler);
		discoveryService.activate();
		this.discoveryServiceReg = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>());
	}

	@Override
	protected void removeHandler(ThingHandler thingHandler) {
		if(this.discoveryServiceReg!=null) {
			MaxCubeDevicesDiscover service = (MaxCubeDevicesDiscover) bundleContext.getService(discoveryServiceReg.getReference());
			service.deactivate();
			discoveryServiceReg.unregister();
			discoveryServiceReg = null;
		}
	}

	@Override
	protected ThingHandler createHandler(Thing thing) {
		if (thing.getThingTypeUID().equals(MaxCubeBinding.CubeBridge_THING_TYPE)) {
			MaxCubeBridgeHandler handler = new MaxCubeBridgeHandler((Bridge) thing);
			registerDeviceDiscoveryService(handler);
			return handler;
		} else if (thing.getThingTypeUID().equals(MaxCubeBinding.SWITCH_THING_TYPE)) {
			return new MaxCubeHandler(thing);            
		} else if (thing.getThingTypeUID().equals(MaxCubeBinding.HEATHINGTHERMOSTAT_THING_TYPE)) {
			return new MaxCubeHandler(thing);
		} else {
			logger.debug("ThingHandler createHandler return null");
			return null;
		}
	}

}