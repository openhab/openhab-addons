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
package org.openhab.binding.lgthinq.internal;

import static org.openhab.binding.lgthinq.internal.LGThinqBindingConstants.THING_TYPE_AIR_CONDITIONER;
import static org.openhab.binding.lgthinq.internal.handler.LGThinqBridgeHandler.THING_TYPE_BRIDGE;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.handler.LGThinqBridgeHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGThinqHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
@Component(service = { ThingHandlerFactory.class }, configurationPid = "binding.lgthinq")
public class LGThinqHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_AIR_CONDITIONER,
            THING_TYPE_BRIDGE);
    private final Logger logger = LoggerFactory.getLogger(LGThinqHandlerFactory.class);
    private final LGThinqDeviceDynStateDescriptionProvider stateDescriptionProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_AIR_CONDITIONER.equals(thingTypeUID)) {
            return new LGThinqAirConditionerHandler(thing, stateDescriptionProvider);
        } else if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new LGThinqBridgeHandler((Bridge) thing);
        }
        logger.error("Thing not supported by this Factory: {}", thingTypeUID.getId());
        return null;
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else if (LGThinqBindingConstants.THING_TYPE_AIR_CONDITIONER.equals(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        }
        return null;
    }

    @Activate
    public LGThinqHandlerFactory(final @Reference LGThinqDeviceDynStateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider = stateDescriptionProvider;
    }
}
