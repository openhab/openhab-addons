/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.evnotify.internal;

import static org.openhab.binding.evnotify.internal.EVNotifyBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.evnotify.internal.EVNotifyBindingConstants.THING_TYPE_VEHICLE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EVNotifyHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Schmidt - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.evnotify", service = ThingHandlerFactory.class)
public class EVNotifyHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(EVNotifyHandlerFactory.class);

    @Activate
    public EVNotifyHandlerFactory() {
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_VEHICLE.equals(thingTypeUID)) {
            return new EVNotifyHandler(thing);
        }

        // if (SUPPORTED_BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID)) {
        // logger.debug("Creating bridge handler for thing {}", thing.getUID());
        // EVNotifBridgeHandler bridgeHandler = new EVNotifBridgeHandler((Bridge) thing);
        //
        // logger.debug("Creating and registering discovery service");
        // BhyveDiscoveryService discoveryService = new BhyveDiscoveryService(bridgeHandler);
        // // Register the discovery service with the bridge handler
        // bridgeHandler.setDiscoveryService(discoveryService);
        // // Register the discovery service
        // ServiceRegistration<?> reg = bundleContext.registerService(DiscoveryService.class.getName(),
        // discoveryService, new Hashtable<String, Object>());
        // // Add the service to the ServiceRegistration map
        // discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), reg);
        // return bridgeHandler;
        // }

        return null;
    }
}
