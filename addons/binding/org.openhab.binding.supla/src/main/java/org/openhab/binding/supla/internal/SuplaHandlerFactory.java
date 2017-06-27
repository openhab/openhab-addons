/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal;

import static org.openhab.binding.supla.SuplaBindingConstants.*;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.openhab.binding.supla.handler.SuplaCloudBridgeHandler;
import org.openhab.binding.supla.handler.SuplaZamelRow01Handler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.supla.internal.discovery.SuplaDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SuplaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
public class SuplaHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(SuplaHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(SUPLA_ZAMEL_ROW_01_THING_TYPE)) {
            return new SuplaZamelRow01Handler(thing);
        } else if (thingTypeUID.equals(BRIDGE_THING_TYPE)) {
            final SuplaCloudBridgeHandler bridgeHandler = new SuplaCloudBridgeHandler((Bridge) thing);
            registerThingDiscovery(bridgeHandler);
            return bridgeHandler;
        }

        return null;
    }

    private synchronized void registerThingDiscovery(SuplaCloudBridgeHandler bridgeHandler) {
        logger.trace("Try to register Discovery service on BundleID: {} Service: {}",
                bundleContext.getBundle().getBundleId(), DiscoveryService.class.getName());

        SuplaDiscoveryService discoveryService = new SuplaDiscoveryService(bridgeHandler);
        bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>());
        discoveryService.activate();
    }
}
