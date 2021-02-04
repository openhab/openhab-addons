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
package org.openhab.binding.airq.internal;

import static org.openhab.binding.airq.internal.airqBindingConstants.THING_TYPE_AIRQ;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
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
 * The {@link airqHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Aurelio Caliaro - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.airq", service = ThingHandlerFactory.class)
public class airqHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(airqHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_AIRQ);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_AIRQ.equals(thingTypeUID)) {
            return new airqHandler(thing);
        }
        return null;
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        logger.trace(
                "air-Q - airqHandlerFactory - createThing: start with thingTypeUID={}, configuration={}, thingUID={}, bridgeUID={}",
                thingTypeUID, configuration, thingUID, bridgeUID);
        Thing th = super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        logger.trace("air-Q - airqHandlerFactory - createThing: result Thing={}", th);
        return th;
        /*
         * if (airqBindingConstants.THING_TYPE_UID_BRIDGE.equals(thingTypeUID)) {
         * logger.warn("Create Bridge: {}", adapterID);
         * return super.createThing(thingTypeUID, configuration, thingUID, null);
         * } else {
         * if (supportsThingType(thingTypeUID)) {
         * logger.trace("Create Thing: {}", adapterID);
         * return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
         * }
         */
    }
}
