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
package org.openhab.binding.broadlinkthermostat.internal;

import static org.openhab.binding.broadlinkthermostat.internal.BroadlinkThermostatBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.broadlinkthermostat.internal.handler.FloureonThermostatHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link BroadlinkThermostatHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Florian Mueller - Initial contribution
 */
@Component(configurationPid = "binding.broadlinkthermostat", service = ThingHandlerFactory.class)
@NonNullByDefault
public class BroadlinkThermostatHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(FLOUREON_THERMOSTAT_THING_TYPE,
            HYSEN_THERMOSTAT_THING_TYPE, UNKNOWN_BROADLINKTHERMOSTAT_THING_TYPE);

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
        return null;
    }
}
