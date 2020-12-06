/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.broadlinkthermostat.internal;

import static org.openhab.binding.broadlinkthermostat.internal.BroadlinkThermostatBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.broadlinkthermostat.internal.handler.BroadlinkThermostatHandler;
import org.openhab.binding.broadlinkthermostat.internal.handler.FloureonThermostatHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BroadlinkThermostatHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@Component(configurationPid = "binding.broadlinkthermostat", service = ThingHandlerFactory.class)
@NonNullByDefault
public class BroadlinkThermostatHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(BroadlinkThermostatHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.unmodifiableSet(Stream
            .of(FLOUREON_THERMOSTAT_THING_TYPE, HYSEN_THERMOSTAT_THING_TYPE, UNKNOWN_BROADLINKTHERMOSTAT_THING_TYPE)
            .collect(Collectors.toSet()));
    private static final Map<String, BroadlinkThermostatHandler> BROADLINK_THERMOSTAT_THING_HANDLERS = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (FLOUREON_THERMOSTAT_THING_TYPE.equals(thingTypeUID) || HYSEN_THERMOSTAT_THING_TYPE.equals(thingTypeUID)) {
            return new FloureonThermostatHandler(thing);
        }
        logger.warn("No handler for {} available", thingTypeUID);
        return null;
    }

    @Override
    public void unregisterHandler(Thing thing) {
        super.unregisterHandler(thing);
        BROADLINK_THERMOSTAT_THING_HANDLERS.remove(thing.getUID().toString());
    }
}
