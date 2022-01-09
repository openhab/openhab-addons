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
package org.openhab.binding.adorne.internal.handler;

import static org.openhab.binding.adorne.internal.AdorneBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * The {@link AdorneHandlerFactory} is responsible for creating thing handlers.
 *
 * @author Mark Theiding - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.adorne", service = ThingHandlerFactory.class)
public class AdorneHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(AdorneHandlerFactory.class);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_HUB, THING_TYPE_SWITCH, THING_TYPE_DIMMER).collect(Collectors.toSet()));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Creates handlers for switches, dimmers and hubs.
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_SWITCH)) {
            logger.debug("Creating an AdorneSwitchHandler for thing '{}'", thing.getUID());

            return new AdorneSwitchHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_DIMMER)) {
            logger.debug("Creating an AdorneDimmerHandler for thing '{}'", thing.getUID());

            return new AdorneDimmerHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_HUB)) {
            logger.debug("Creating an AdorneHubHandler for bridge '{}'", thing.getUID());

            return new AdorneHubHandler((Bridge) thing);
        }

        return null;
    }
}
