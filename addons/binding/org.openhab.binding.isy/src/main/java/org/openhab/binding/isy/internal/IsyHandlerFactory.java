/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.isy.internal;

import static org.openhab.binding.isy.IsyBindingConstants.*;

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
import org.openhab.binding.isy.discovery.IsyDiscoveryService;
import org.openhab.binding.isy.handler.IsyBridgeHandler;
import org.openhab.binding.isy.handler.IsyHandlerBuilder;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link IsyHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Craig Hamilton - Initial contribution
 */
public class IsyHandlerFactory extends BaseThingHandlerFactory {

    private static final Logger logger = LoggerFactory.getLogger(IsyHandlerFactory.class);
    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_ISYBRIDGE,
            MOTION_THING_TYPE, DIMMER_THING_TYPE, LEAKDETECTOR_THING_TYPE, SWITCH_THING_TYPE, RELAY_THING_TYPE,
            GARAGEDOORKIT_THING_TYPE, KEYPAD_LINC_6_THING_TYPE, KEYPAD_LINC_5_THING_TYPE, REMOTELINC_8_THING_TYPE,
            INLINELINC_SWITCH_THING_TYPE);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegistrations = new HashMap<ThingUID, ServiceRegistration<?>>();

    private InsteonClientProvider bridgeHandler = null;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(MOTION_THING_TYPE)) {
            return IsyHandlerBuilder.builder(thing).forChannel(CHANNEL_MOTION_MOTION, 1)
                    .forChannel(CHANNEL_MOTION_DUSK, 2).forChannel(CHANNEL_MOTION_BATTERY, 3).build();
        } else if (thingTypeUID.equals(LEAKDETECTOR_THING_TYPE)) {
            return IsyHandlerBuilder.builder(thing).forChannel(CHANNEL_LEAK_DRY, 1).forChannel(CHANNEL_LEAK_WET, 2)
                    .forChannel(CHANNEL_LEAK_HEARTBEAT, 4).build();
        } else if (thingTypeUID.equals(GARAGEDOORKIT_THING_TYPE)) {
            return IsyHandlerBuilder.builder(thing).forChannel(CHANNEL_GARAGE_SENSOR, 1)
                    .forChannel(CHANNEL_GARAGE_CONTACT, 2).build();
        } else if (thingTypeUID.equals(INLINELINC_SWITCH_THING_TYPE)) {
            return IsyHandlerBuilder.builder(thing).forChannel(CHANNEL_SWITCH, 1).build();
        } else if (thingTypeUID.equals(SWITCH_THING_TYPE)) {
            return IsyHandlerBuilder.builder(thing).forChannel(CHANNEL_SWITCH, 1).build();
        } else if (thingTypeUID.equals(DIMMER_THING_TYPE)) {
            return IsyHandlerBuilder.builder(thing).forChannel(CHANNEL_LIGHTLEVEL, 1).build();

        } else if (thingTypeUID.equals(KEYPAD_LINC_6_THING_TYPE) || thingTypeUID.equals(KEYPAD_LINC_5_THING_TYPE)) {
            return IsyHandlerBuilder.builder(thing).forChannel(CHANNEL_LIGHTLEVEL, 1)
                    .forChannel(CHANNEL_KEYPAD_LINC_A, 3).forChannel(CHANNEL_KEYPAD_LINC_B, 4)
                    .forChannel(CHANNEL_KEYPAD_LINC_C, 5).forChannel(CHANNEL_KEYPAD_LINC_D, 6).build();
        } else if (thingTypeUID.equals(REMOTELINC_8_THING_TYPE)) {
            return IsyHandlerBuilder.builder(thing).forChannel(CHANNEL_LIGHTLEVEL, 1)
                    .forChannel(CHANNEL_KEYPAD_LINC_A, 2).forChannel(CHANNEL_KEYPAD_LINC_B, 1)
                    .forChannel(CHANNEL_KEYPAD_LINC_C, 4).forChannel(CHANNEL_KEYPAD_LINC_D, 3)
                    .forChannel(CHANNEL_KEYPAD_LINC_E, 6).forChannel(CHANNEL_KEYPAD_LINC_F, 5)
                    .forChannel(CHANNEL_KEYPAD_LINC_G, 8).forChannel(CHANNEL_KEYPAD_LINC_H, 7).build();
        } else if (thingTypeUID.equals(THING_TYPE_ISYBRIDGE)) {
            IsyBridgeHandler handler = new IsyBridgeHandler((Bridge) thing);
            this.bridgeHandler = handler;
            registerIsyBridgeDiscoveryService(handler);
            return handler;
        }

        throw new IllegalArgumentException("No handler found for thing: " + thing);
    }

    /**
     * Register the Thing Discovery Service for a bridge.
     *
     * @param isyBridgeBridgeHandler
     */
    private void registerIsyBridgeDiscoveryService(IsyBridgeHandler isyBridgeBridgeHandler) {
        IsyDiscoveryService discoveryService = new IsyDiscoveryService(isyBridgeBridgeHandler);
        discoveryService.activate();

        ServiceRegistration<?> discoveryServiceRegistration = bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>());
        discoveryServiceRegistrations.put(isyBridgeBridgeHandler.getThing().getUID(), discoveryServiceRegistration);

        logger.debug(
                "registerIsyBridgeDiscoveryService(): Bridge Handler - {}, Class Name - {}, Discovery Service - {}",
                isyBridgeBridgeHandler, DiscoveryService.class.getName(), discoveryService);
    }
}
