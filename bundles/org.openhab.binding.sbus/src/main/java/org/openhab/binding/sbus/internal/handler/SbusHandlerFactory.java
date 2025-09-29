/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sbus.internal.handler;

import static org.openhab.binding.sbus.BindingConstants.*;

import java.util.Set;

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
 * The {@link SbusHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.sbus")
public class SbusHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SbusHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_UDP_BRIDGE, THING_TYPE_SWITCH,
            THING_TYPE_TEMPERATURE, THING_TYPE_RGBW, THING_TYPE_CONTACT_SENSOR, THING_TYPE_MULTI_SENSOR);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_UDP_BRIDGE)) {
            logger.debug("Creating Sbus UDP bridge handler for thing {}", thing.getUID());
            return new SbusBridgeHandler((Bridge) thing);
        }

        if (thingTypeUID.equals(THING_TYPE_SWITCH)) {
            logger.debug("Creating Sbus switch handler for thing {}", thing.getUID());
            return new SbusSwitchHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_TEMPERATURE)) {
            logger.debug("Creating Sbus temperature handler for thing {}", thing.getUID());
            return new SbusTemperatureHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_RGBW)) {
            logger.debug("Creating Sbus RGBW handler for thing {}", thing.getUID());
            return new SbusRgbwHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_CONTACT_SENSOR)) {
            logger.debug("Creating Sbus contact sensor handler for thing {}", thing.getUID());
            return new SbusContactHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_MULTI_SENSOR)) {
            logger.debug("Creating Sbus multi-sensor handler for thing {}", thing.getUID());
            return new Sbus9in1SensorsHandler(thing);
        }

        logger.debug("Unknown thing type: {}", thingTypeUID);
        return null;
    }
}
