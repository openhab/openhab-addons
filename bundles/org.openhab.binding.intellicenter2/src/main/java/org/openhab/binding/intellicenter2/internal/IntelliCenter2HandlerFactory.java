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
package org.openhab.binding.intellicenter2.internal;

import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.BRIDGE_TYPE_BRIDGE;
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.THING_TYPE_FEATURE;
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.THING_TYPE_LIGHT;
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.THING_TYPE_POOL;
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.THING_TYPE_PUMP;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.intellicenter2.internal.handler.IntelliCenter2BridgeHandler;
import org.openhab.binding.intellicenter2.internal.handler.IntelliCenter2FeatureHandler;
import org.openhab.binding.intellicenter2.internal.handler.IntelliCenter2LightHandler;
import org.openhab.binding.intellicenter2.internal.handler.IntelliCenter2PoolHandler;
import org.openhab.binding.intellicenter2.internal.handler.IntelliCenter2PumpHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IntelliCenter2HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.intellicenter2", service = ThingHandlerFactory.class)
public class IntelliCenter2HandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(IntelliCenter2HandlerFactory.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(BRIDGE_TYPE_BRIDGE, THING_TYPE_POOL,
            THING_TYPE_FEATURE, THING_TYPE_LIGHT, THING_TYPE_PUMP);

    public IntelliCenter2HandlerFactory() {
        super();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.debug("Creating handler for {}", thingTypeUID);
        if (thingTypeUID.equals(BRIDGE_TYPE_BRIDGE)) {
            return new IntelliCenter2BridgeHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_TYPE_POOL)) {
            return new IntelliCenter2PoolHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_FEATURE)) {
            return new IntelliCenter2FeatureHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_LIGHT)) {
            return new IntelliCenter2LightHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_PUMP)) {
            return new IntelliCenter2PumpHandler(thing);
        }
        logger.error("Unable to create handler for {}", thingTypeUID);
        return null;
    }
}
