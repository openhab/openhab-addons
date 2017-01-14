/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.factory;

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
import org.openhab.binding.mysensors.internal.handler.MySensorsBridgeHandler;
import org.openhab.binding.mysensors.internal.handler.MySensorsThingHandler;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MySensorsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Tim Oberföll
 */
public class MySensorsHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_DEVICE_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new MySensorsThingHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_BRIDGE_SER) || thingTypeUID.equals(THING_TYPE_BRIDGE_ETH)) {
            MySensorsBridgeHandler handler = new MySensorsBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        }

        logger.error("Thing {} cannot be configured, is this thing supported by the binding?", thingTypeUID);

        return null;
    }

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
    public void removeThing(ThingUID thingUID) {
        logger.trace("Removing thing: {}", thingUID);
        super.removeThing(thingUID);
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        logger.trace("Removing handler: {}", thingHandler);
        if (thingHandler instanceof MySensorsBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }
}
