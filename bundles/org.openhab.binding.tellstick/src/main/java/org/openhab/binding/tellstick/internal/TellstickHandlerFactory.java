/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.tellstick.internal;

import static org.openhab.binding.tellstick.internal.TellstickBindingConstants.*;

import java.util.Hashtable;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.tellstick.internal.core.TelldusCoreBridgeHandler;
import org.openhab.binding.tellstick.internal.discovery.TellstickDiscoveryService;
import org.openhab.binding.tellstick.internal.handler.TelldusBridgeHandler;
import org.openhab.binding.tellstick.internal.handler.TelldusDevicesHandler;
import org.openhab.binding.tellstick.internal.live.TelldusLiveBridgeHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TellstickHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jarle Hjortland - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.tellstick")
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
            bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>());
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
