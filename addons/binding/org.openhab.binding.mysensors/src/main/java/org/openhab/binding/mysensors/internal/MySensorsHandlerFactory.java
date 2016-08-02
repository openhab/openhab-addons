/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.mysensors.discovery.MySensorsDiscoveryService;
import org.openhab.binding.mysensors.handler.MySensorsBridgeHandler;
import org.openhab.binding.mysensors.handler.MySensorsHandler;
import org.osgi.framework.ServiceRegistration;

/**
 * The {@link MySensorsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Tim Oberföll
 */
public class MySensorsHandlerFactory extends BaseThingHandlerFactory {

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_DEVICE_TYPES_UIDS.contains(thingTypeUID);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory#createHandler(org.eclipse.smarthome.core.thing.
     * Thing)
     */
    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new MySensorsHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_BRIDGE_SER) || thingTypeUID.equals(THING_TYPE_BRIDGE_ETH)) {
            MySensorsBridgeHandler handler = new MySensorsBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory#createThing(org.eclipse.smarthome.core.thing.
     * ThingTypeUID, org.eclipse.smarthome.config.core.Configuration, org.eclipse.smarthome.core.thing.ThingUID)
     */
    @Override
    protected Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID) {
        return super.createThing(thingTypeUID, configuration, thingUID);
    }

    private void registerDeviceDiscoveryService(MySensorsBridgeHandler mySensorsBridgeHandler) {
        MySensorsDiscoveryService discoveryService = new MySensorsDiscoveryService(mySensorsBridgeHandler);
        this.discoveryServiceRegs.put(mySensorsBridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof MySensorsBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }
}
