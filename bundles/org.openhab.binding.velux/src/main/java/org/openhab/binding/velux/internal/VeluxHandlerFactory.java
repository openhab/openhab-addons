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
package org.openhab.binding.velux.internal;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.velux.VeluxBindingConstants;
import org.openhab.binding.velux.discovery.VeluxDiscoveryService;
import org.openhab.binding.velux.handler.VeluxBindingHandler;
import org.openhab.binding.velux.handler.VeluxBridgeHandler;
import org.openhab.binding.velux.handler.VeluxHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeluxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, name = "binding.velux")
public class VeluxHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(VeluxHandlerFactory.class);

    // Class internal

    private @Nullable ServiceRegistration<?> discoveryServiceReg;
    private @Nullable VeluxBindingHandler veluxBindingHandler = null;
    private Integer veluxBridgeCount = 0;

    // Private

    private void registerDeviceDiscoveryService(VeluxBridgeHandler bridgeHandler) {
        VeluxDiscoveryService discoveryService = new VeluxDiscoveryService(bridgeHandler);
        discoveryServiceReg = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
    }

    private synchronized void unregisterDeviceDiscoveryService() {
        logger.trace("unregisterDeviceDiscoveryService({}) called.");

        if (discoveryServiceReg != null) {
            discoveryServiceReg.unregister();
            discoveryServiceReg = null;
        }
    }

    // Utility methods

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        boolean result = VeluxBindingConstants.SUPPORTED_THINGS_BRIDGE.contains(thingTypeUID)
                || VeluxBindingConstants.SUPPORTED_THINGS_ITEMS.contains(thingTypeUID)
                || VeluxBindingConstants.SUPPORTED_THINGS_BINDING.contains(thingTypeUID);
        logger.trace("supportsThingType({}) called and returns {}.", thingTypeUID, result);
        return result;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        logger.trace("createHandler({}) called.", thing.getLabel());

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        // Handle Binding creation as 1st choice
        if (VeluxBindingConstants.SUPPORTED_THINGS_BINDING.contains(thingTypeUID)) {
            logger.trace("createHandler(): Creating a Handler for thing '{}'.", thing.getUID());
            veluxBindingHandler = new VeluxBindingHandler(thing);
            return veluxBindingHandler;
        }

        else
        // Handle Bridge creation as 2nd choice
        if (VeluxBindingConstants.SUPPORTED_THINGS_BRIDGE.contains(thingTypeUID)) {
            logger.trace("createHandler(): Creating a VeluxBridgeHandler for thing '{}'.", thing.getUID());
            VeluxBridgeHandler handler = new VeluxBridgeHandler((Bridge) thing);
            veluxBridgeCount++;
            synchronized (this) {
                if (veluxBindingHandler != null) {
                    veluxBindingHandler.updateNoOfBridges(veluxBridgeCount);
                }
            }
            registerDeviceDiscoveryService(handler);
            return handler;
        }

        else
        // Handle creation of Things behind the Bridge
        if (VeluxBindingConstants.SUPPORTED_THINGS_ITEMS.contains(thingTypeUID)) {
            logger.trace("createHandler(): Creating a VeluxHandler for thing '{}'.", thing.getUID());
            return new VeluxHandler(thing);
        } else {
            logger.warn("ThingHandler not found for {}.", thingTypeUID);
            return null;
        }
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        logger.trace("removeHandler({}) called.", thingHandler.toString());

        if (thingHandler.getThing().getThingTypeUID().equals(VeluxBindingConstants.THING_TYPE_BRIDGE)) {
            veluxBridgeCount--;
            unregisterDeviceDiscoveryService();
            synchronized (this) {
                if (veluxBindingHandler != null) {
                    veluxBindingHandler.updateNoOfBridges(veluxBridgeCount);
                }
            }
        }
        super.removeHandler(thingHandler);
    }

}
