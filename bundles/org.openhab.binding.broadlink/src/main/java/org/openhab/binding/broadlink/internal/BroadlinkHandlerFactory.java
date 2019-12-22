/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.internal;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.broadlink.internal.handler.A1EnvironmentalSensorHandler;
import org.openhab.binding.broadlink.internal.handler.BroadlinkHandler;
import org.openhab.binding.broadlink.internal.handler.FloureonThermostatHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.openhab.binding.broadlink.internal.BroadlinkBindingConstants.*;

/**
 * The {@link BroadlinkHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@Component(configurationPid = "binding.broadlink", service = ThingHandlerFactory.class)
public class BroadlinkHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(BroadlinkHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .unmodifiableSet(Stream.of(FLOUREON_THERMOSTAT_THING_TYPE, HYSEN_THERMOSTAT_THING_TYPE, A1_ENVIRONMENTAL_SENSOR_THING_TYPE, RM_MINI_THING_TYPE, UNKNOWN_BROADLINK_THING_TYPE).collect(Collectors.toSet()));
    private static final Map<String, BroadlinkHandler> BROADLINK_THING_HANDLERS = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if(FLOUREON_THERMOSTAT_THING_TYPE.equals(thingTypeUID) || HYSEN_THERMOSTAT_THING_TYPE.equals(thingTypeUID)) {
            return new FloureonThermostatHandler(thing);
        }else if(A1_ENVIRONMENTAL_SENSOR_THING_TYPE.equals(thingTypeUID)) {
            return new A1EnvironmentalSensorHandler(thing);
        }else if(RM_MINI_THING_TYPE.equals(thingTypeUID)) {
            return new A1EnvironmentalSensorHandler(thing);
        }

        logger.warn("No handler for {} available",thingTypeUID);
        return null;
    }

    @Override
    public void unregisterHandler(Thing thing) {
        super.unregisterHandler(thing);
        BROADLINK_THING_HANDLERS.remove(thing.getUID().toString());
    }

    public static BroadlinkHandler getHandler(String thingUid) {
        return BROADLINK_THING_HANDLERS.get(thingUid);
    }

}
