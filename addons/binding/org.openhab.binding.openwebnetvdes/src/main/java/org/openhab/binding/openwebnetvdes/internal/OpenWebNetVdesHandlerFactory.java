/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnetvdes.internal;

import static org.openhab.binding.openwebnetvdes.OpenWebNetVdesBindingConstants.*;

import java.util.Hashtable;

import org.openhab.binding.openwebnetvdes.OpenWebNetVdesBindingConstants;
import org.openhab.binding.openwebnetvdes.handler.Ip2WireBridgeHandler;
import org.openhab.binding.openwebnetvdes.handler.OpenWebNetVdesHandler;
import org.openhab.binding.openwebnetvdes.internal.discovery.BticinoDeviceDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

import org.osgi.framework.ServiceRegistration;

/**
 * The {@link OpenWebNetVdesHandlerFactory} is responsible for creating things and thing 
 * handlers.
 * 
 * @author DmytroKulyanda - Initial contribution
 */
public class OpenWebNetVdesHandlerFactory extends BaseThingHandlerFactory {
	private Logger logger = LoggerFactory.getLogger(OpenWebNetVdesHandlerFactory.class);
	private ServiceRegistration<?> discoveryServiceReg;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

	@Override
	public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
			ThingUID bridgeUID) {

		if (IP_2WIRE_INTERFACE_THING_TYPE.equals(thingTypeUID)) {
			ThingUID bticinoBridgeUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
			logger.debug("createThing: {}", bticinoBridgeUID);
			return super.createThing(thingTypeUID, configuration, bticinoBridgeUID, null);
		}
		else if (supportsThingType(thingTypeUID)) {
			ThingUID deviceUID = getBtcinoDeviceUID(thingTypeUID, thingUID, configuration, bridgeUID);
			logger.debug("createThing: {}", deviceUID);
			return super.createThing(thingTypeUID, configuration, deviceUID, bridgeUID);
		}
		throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the binding.");
	}
	
	private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
		if (thingUID == null) {
			String SerialNumber = (String) configuration.get(IP_ADDRESS);
			thingUID = new ThingUID(thingTypeUID, SerialNumber);
		}
		logger.debug("getBridgeThingUID : {}", thingUID);
		return thingUID;
	}

	private ThingUID getBtcinoDeviceUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
			ThingUID bridgeUID) {
		String SerialNumber = (String) configuration.get(OWN_WHERE_ADDRESS);

		if (thingUID == null) {
			thingUID = new ThingUID(thingTypeUID, SerialNumber, bridgeUID.getId());
		}
		logger.debug("getBtcinoDeviceUID : {}", thingUID);
		return thingUID;
	}

	private void registerDeviceDiscoveryService(Ip2WireBridgeHandler bridgeHandler) {
		BticinoDeviceDiscoveryService discoveryService = new BticinoDeviceDiscoveryService(bridgeHandler);
		discoveryService.activate();
		this.discoveryServiceReg = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
				new Hashtable<String, Object>());
	}

	@Override
	protected void removeHandler(ThingHandler thingHandler) {
		if (this.discoveryServiceReg != null) {
			BticinoDeviceDiscoveryService service = (BticinoDeviceDiscoveryService) bundleContext
					.getService(discoveryServiceReg.getReference());
			service.deactivate();
			discoveryServiceReg.unregister();
			discoveryServiceReg = null;
		}
		super.removeHandler(thingHandler);
	}

	@Override
	protected ThingHandler createHandler(Thing thing) {
		if (thing.getThingTypeUID().equals(OpenWebNetVdesBindingConstants.IP_2WIRE_INTERFACE_THING_TYPE)) {
			Ip2WireBridgeHandler handler = new Ip2WireBridgeHandler((Bridge) thing);
			registerDeviceDiscoveryService(handler);
			return handler;
		} else if (supportsThingType(thing.getThingTypeUID())) {
			return new OpenWebNetVdesHandler(thing);
		} else {
			logger.debug("ThingHandler not found for {}", thing.getThingTypeUID());
			return null;
		}
	}

}

