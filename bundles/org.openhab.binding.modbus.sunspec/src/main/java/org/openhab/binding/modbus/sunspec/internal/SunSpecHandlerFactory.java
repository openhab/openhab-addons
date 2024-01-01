/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.modbus.sunspec.internal;

import static org.openhab.binding.modbus.sunspec.internal.SunSpecConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.sunspec.internal.handler.InverterHandler;
import org.openhab.binding.modbus.sunspec.internal.handler.MeterHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SunSpecHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Nagy Attila Gábor - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.sunspec", service = ThingHandlerFactory.class)
public class SunSpecHandlerFactory extends BaseThingHandlerFactory {

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(SunSpecHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.containsValue(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_INVERTER_SINGLE_PHASE)
                || thingTypeUID.equals(THING_TYPE_INVERTER_SPLIT_PHASE)
                || thingTypeUID.equals(THING_TYPE_INVERTER_THREE_PHASE)) {
            logger.debug("New InverterHandler created");
            return new InverterHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_METER_SINGLE_PHASE)
                || thingTypeUID.equals(THING_TYPE_METER_SPLIT_PHASE) || thingTypeUID.equals(THING_TYPE_METER_WYE_PHASE)
                || thingTypeUID.equals(THING_TYPE_METER_DELTA_PHASE)) {
            logger.debug("New MeterHandler created");
            return new MeterHandler(thing);
        }

        return null;
    }
}
