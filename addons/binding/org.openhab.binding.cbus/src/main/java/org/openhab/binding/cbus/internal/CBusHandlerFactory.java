/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.cbus.internal;

import java.util.Hashtable;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.cbus.CBusBindingConstants;
import org.openhab.binding.cbus.handler.CBusCGateHandler;
import org.openhab.binding.cbus.handler.CBusDaliHandler;
import org.openhab.binding.cbus.handler.CBusLightHandler;
import org.openhab.binding.cbus.handler.CBusNetworkHandler;
import org.openhab.binding.cbus.handler.CBusTemperatureHandler;
import org.openhab.binding.cbus.handler.CBusTriggerHandler;
import org.openhab.binding.cbus.internal.discovery.CBusGroupDiscovery;
import org.openhab.binding.cbus.internal.discovery.CBusNetworkDiscovery;

/**
 * The {@link CBusHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Scott Linton - Initial contribution
 */
public class CBusHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return CBusBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(CBusBindingConstants.BRIDGE_TYPE_CGATE)) {
            CBusCGateHandler handler = new CBusCGateHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        }

        if (thingTypeUID.equals(CBusBindingConstants.BRIDGE_TYPE_NETWORK)) {
            CBusNetworkHandler handler = new CBusNetworkHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        }

        if (thingTypeUID.equals(CBusBindingConstants.THING_TYPE_LIGHT)) {
            return new CBusLightHandler(thing);
        }

        if (thingTypeUID.equals(CBusBindingConstants.THING_TYPE_TEMPERATURE)) {
            return new CBusTemperatureHandler(thing);
        }

        if (thingTypeUID.equals(CBusBindingConstants.THING_TYPE_TRIGGER)) {
            return new CBusTriggerHandler(thing);
        }

        if (thingTypeUID.equals(CBusBindingConstants.THING_TYPE_DALI)) {
            return new CBusDaliHandler(thing);
        }

        return null;
    }

    private void registerDeviceDiscoveryService(CBusCGateHandler cbusCgateHandler) {
        CBusNetworkDiscovery discoveryService = new CBusNetworkDiscovery(cbusCgateHandler);
        discoveryService.activate();
        super.bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
    }

    private void registerDeviceDiscoveryService(CBusNetworkHandler cbusNetworkHandler) {
        CBusGroupDiscovery discoveryService = new CBusGroupDiscovery(cbusNetworkHandler);
        discoveryService.activate();
        bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
    }
}
