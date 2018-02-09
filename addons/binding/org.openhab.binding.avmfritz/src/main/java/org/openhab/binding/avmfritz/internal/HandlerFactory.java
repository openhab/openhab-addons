/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal;

import static org.openhab.binding.avmfritz.BindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.avmfritz.handler.BoxHandler;
import org.openhab.binding.avmfritz.handler.DeviceHandler;
import org.openhab.binding.avmfritz.internal.discovery.AVMFritzDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Robert Bausdorf - Initial contribution
 *
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.avmfritz", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class HandlerFactory extends BaseThingHandlerFactory {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * Service registration map
     */
    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    /**
     * Provides the supported thing types
     */
    @Override
    public boolean supportsThingType(@NonNull ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Create handler of things.
     */
    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(BRIDGE_THING_TYPE)) {
            BoxHandler handler = new BoxHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        } else if (thingTypeUID.equals(PL546E_STANDALONE_THING_TYPE)) {
            DeviceHandler handler = new DeviceHandler(thing);
            return handler;
        } else if (supportsThingType(thing.getThingTypeUID())) {
            return new DeviceHandler(thing);
        } else {
            logger.warn("ThingHandler not found for {}", thing.getThingTypeUID());
        }

        return null;
    }

    /**
     * Remove handler of things.
     */
    @Override
    protected synchronized void removeHandler(@NonNull ThingHandler thingHandler) {
        if (thingHandler instanceof BoxHandler) {
            ServiceRegistration<?> serviceReg = discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                AVMFritzDiscoveryService discoveryService = (AVMFritzDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                discoveryService.deactivate();
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }

    /**
     * Register a new discovery service for a new FRITZ!Box.
     *
     * @param handler
     */
    private void registerDeviceDiscoveryService(BoxHandler handler) {
        AVMFritzDiscoveryService discoveryService = new AVMFritzDiscoveryService(handler);
        discoveryServiceRegs.put(handler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }
}
