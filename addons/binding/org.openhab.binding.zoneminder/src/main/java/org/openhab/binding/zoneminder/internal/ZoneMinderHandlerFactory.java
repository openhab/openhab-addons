/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.discovery.ZoneMinderMonitorDiscoveryService;
import org.openhab.binding.zoneminder.handler.ZoneMinderServerBridgeHandler;
import org.openhab.binding.zoneminder.handler.ZoneMinderThingMonitorHandler;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link ZoneMinderHandlerFactory} is responsible for creating things and thing
 * handlers.l
 *
 * @author Martin S. Eskildsen - Initial contribution
 *
 *         TODO:: Implement Discovery handling like SqueezeBox binding does for Players
 *
 */
public class ZoneMinderHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(ZoneMinderHandlerFactory.class);
    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.union(
            ZoneMinderServerBridgeHandler.SUPPORTED_THING_TYPES, ZoneMinderThingMonitorHandler.SUPPORTED_THING_TYPES);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(ZoneMinderConstants.THING_TYPE_BRIDGE_ZONEMINDER_SERVER)) {

            logger.trace("creating handler for bridge thing {}", thing);
            ZoneMinderServerBridgeHandler bridge = new ZoneMinderServerBridgeHandler((Bridge) thing);
            registerZoneMinderMonitorDiscoveryService(bridge);

            return bridge; // new ZoneMinderServerBridgeHandler((Bridge) thing);
        } else if (thingTypeUID.equals(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR)) {
            return new ZoneMinderThingMonitorHandler(thing);
        }

        return null;
    }

    /**
     * Adds ZoneMinderServerBridgeHandler to the discovery service to find ZoneMinder Monitors
     *
     * @param zoneMinderServerBridgeHandler
     */
    private synchronized void registerZoneMinderMonitorDiscoveryService(
            ZoneMinderServerBridgeHandler zoneMinderServerBridgeHandler) {
        logger.trace("Registering discovery service for ZoneMinder Monitor");

        ZoneMinderMonitorDiscoveryService discoveryService = new ZoneMinderMonitorDiscoveryService(
                zoneMinderServerBridgeHandler);

        // Register the PlayerListener with the SqueezeBoxServerHandler
        // zoneMinderServerBridgeHandler.registerSqueezeBoxPlayerListener(discoveryService);

        // Register the service, then add the service to the ServiceRegistration map
        discoveryServiceRegs.put(zoneMinderServerBridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {

        if (thingHandler instanceof ZoneMinderServerBridgeHandler) {
            logger.trace("Removing handler for bridge thing {}", thingHandler.getThing());

            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                logger.trace("Unregistering player discovery service");

                // Get the discovery service object and use it to cancel the RequestMonitorJob
                ZoneMinderMonitorDiscoveryService discoveryService = (ZoneMinderMonitorDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                discoveryService.cancelRequestMonitorJob();

                // Unregister the PlayerListener from the SqueezeBoxServerHandler
                // ((SqueezeBoxServerHandler) thingHandler).unregisterSqueezeBoxPlayerListener(
                // (SqueezeBoxPlayerEventListener) bundleContext.getService(serviceReg.getReference()));

                // Unregister the PlayerListener service
                serviceReg.unregister();

                // Remove the service from the ServiceRegistration map
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }

        if (thingHandler instanceof ZoneMinderThingMonitorHandler) {
            ZoneMinderThingMonitorHandler thingMonitor = (ZoneMinderThingMonitorHandler) thingHandler;
            ZoneMinderServerBridgeHandler bridge = (ZoneMinderServerBridgeHandler) thingMonitor
                    .getZoneMinderBridgeHandler();

            if (bridge != null) {
                logger.trace("removing handler for monitor thing {}", thingHandler.getThing());
                // bridge.removePlayerCache(((SqueezeBoxPlayerHandler) thingHandler).getMac());
            }
        }
    }

}
