/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.vitotronic.internal;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.vitotronic.internal.discovery.VitotronicDiscoveryService;
import org.openhab.binding.vitotronic.internal.handler.VitotronicBridgeHandler;
import org.openhab.binding.vitotronic.internal.handler.VitotronicThingHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VitotronicHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Stefan Andres - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.vitotronic")
public class VitotronicHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(VitotronicHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        logger.trace("Ask Handler for Supported Thing {}",
                VitotronicBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID));
        return VitotronicBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        logger.trace("Install Handler for Thing {}", thing.getUID());

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(VitotronicBindingConstants.THING_TYPE_UID_BRIDGE)) {
            VitotronicBridgeHandler handler = new VitotronicBridgeHandler((Bridge) thing);
            registerThingDiscovery(handler);
            return handler;
        }

        if (supportsThingType(thingTypeUID)) {
            return new VitotronicThingHandler(thing);
        }

        return null;
    }

    private synchronized void registerThingDiscovery(VitotronicBridgeHandler bridgeHandler) {
        VitotronicDiscoveryService discoveryService = new VitotronicDiscoveryService(bridgeHandler);
        logger.trace("Try to register Discovery service on BundleID: {} Service: {}",
                bundleContext.getBundle().getBundleId(), DiscoveryService.class.getName());
        bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>());
        discoveryService.activate();
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        logger.trace("Create Thing for Type {}", thingUID);

        String adapterID = (String) configuration.get(VitotronicBindingConstants.ADAPTER_ID);

        if (VitotronicBindingConstants.THING_TYPE_UID_BRIDGE.equals(thingTypeUID)) {
            logger.trace("Create Bridge: {}", adapterID);
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else {
            if (supportsThingType(thingTypeUID)) {
                logger.trace("Create Thing: {}", adapterID);
                return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
            }
        }

        return null;
    }
}
