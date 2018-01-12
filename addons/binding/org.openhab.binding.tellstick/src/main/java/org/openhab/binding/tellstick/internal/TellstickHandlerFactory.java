/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tellstick.internal;

import static org.openhab.binding.tellstick.TellstickBindingConstants.*;

import java.util.Hashtable;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.tellstick.handler.TelldusBridgeHandler;
import org.openhab.binding.tellstick.handler.TelldusDevicesHandler;
import org.openhab.binding.tellstick.internal.core.TelldusCoreBridgeHandler;
import org.openhab.binding.tellstick.internal.discovery.TellstickDiscoveryService;
import org.openhab.binding.tellstick.internal.live.TelldusLiveBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TellstickHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jarle Hjortland - Initial contribution
 */
public class TellstickHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(TellstickHandlerFactory.class);
    private TellstickDiscoveryService discoveryService = null;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    private void registerDeviceDiscoveryService(TelldusBridgeHandler tellstickBridgeHandler) {
        if (discoveryService == null) {
            discoveryService = new TellstickDiscoveryService(tellstickBridgeHandler);
            discoveryService.activate();
            bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                    new Hashtable<String, Object>());
        } else {
            discoveryService.addBridgeHandler(tellstickBridgeHandler);
        }
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (thing.getThingTypeUID().equals(TELLDUSCOREBRIDGE_THING_TYPE)) {
            TelldusCoreBridgeHandler handler = new TelldusCoreBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        } else if (thing.getThingTypeUID().equals(TELLDUSLIVEBRIDGE_THING_TYPE)) {
            TelldusLiveBridgeHandler handler = new TelldusLiveBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        } else if (supportsThingType(thing.getThingTypeUID())) {
            return new TelldusDevicesHandler(thing);
        } else {
            logger.debug("ThingHandler not found for {}", thing.getThingTypeUID());
            return null;
        }
    }
}
