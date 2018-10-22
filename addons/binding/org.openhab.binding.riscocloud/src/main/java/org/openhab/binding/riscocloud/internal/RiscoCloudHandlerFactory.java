/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.riscocloud.internal;

import static org.openhab.binding.riscocloud.RiscoCloudBindingConstants.*;

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
import org.openhab.binding.riscocloud.handler.RiscoCloudBridgeHandler;
import org.openhab.binding.riscocloud.handler.SiteBridgeHandler;
import org.openhab.binding.riscocloud.handler.SiteHandler;
import org.openhab.binding.riscocloud.internal.discovery.RiscoCloudDiscoveryService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RiscoCloudHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Sebastien Cantineau - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.riscocloud")
public class RiscoCloudHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(RiscoCloudHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID) || BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        logger.debug("createThing(): Trying to create an '{}'", thingTypeUID);

        if (LOGIN_BRIDGE_THING_TYPE.equals(thingTypeUID)) {
            RiscoCloudBridgeHandler handler = new RiscoCloudBridgeHandler((Bridge) thing);
            registerRiscoCloudDiscoveryService(handler);
            logger.debug("createThing(): LOGIN_BRIDGE_THING_TYPE: Creating an '{}' type Thing - {}", thingTypeUID,
                    handler.getID());
            return handler;

        } else if (SITE_BRIDGE_THING_TYPE.equals(thingTypeUID)) {
            SiteBridgeHandler handler = new SiteBridgeHandler((Bridge) thing);
            registerRiscoCloudDiscoveryService(handler);
            logger.debug("createThing(): SITE_BRIDGE_THING_TYPE: Creating an '{}' type Thing - {}", thingTypeUID,
                    handler.getID());
            return handler;
        } else if (OVERVIEW_THING_TYPE.equals(thingTypeUID)) {
            SiteHandler handler = new SiteHandler(thing);
            logger.debug("createThing(): OVERVIEW_THING_TYPE: Creating an '{}' type Thing - {}", thingTypeUID,
                    handler.getThing().getUID());
            return handler;
        } else if (PART_THING_TYPE.equals(thingTypeUID)) {
            SiteHandler handler = new SiteHandler(thing);
            logger.debug("createThing(): PART_THING_TYPE: Creating an '{}' type Thing - {}", thingTypeUID,
                    handler.getThing().getUID());
            return handler;
        }

        logger.debug("createThing(): returned null !!!");
        return null;
    }

    private void registerRiscoCloudDiscoveryService(RiscoCloudBridgeHandler bridgeHandler) {
        logger.debug(
                "registerRiscoCloudDiscoveryService(): Bridge Handler - {}, Class Name - {}, Status - {}, validConfig - {}",
                bridgeHandler, DiscoveryService.class.getName(), bridgeHandler.getThing().getStatus(),
                bridgeHandler.isValidConfig());
        RiscoCloudDiscoveryService discoveryService = new RiscoCloudDiscoveryService(bridgeHandler);
        bridgeHandler.getDiscoveryServiceRegs().put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
        logger.debug(
                "registerRiscoCloudDiscoveryService(): Bridge Handler - {}, Class Name - {}, Discovery Service - {}",
                bridgeHandler, DiscoveryService.class.getName(), discoveryService);
    }

}
