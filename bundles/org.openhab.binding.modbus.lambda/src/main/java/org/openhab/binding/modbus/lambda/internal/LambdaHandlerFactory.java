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
package org.openhab.binding.modbus.lambda.internal;

import static org.openhab.binding.modbus.lambda.internal.LambdaBindingConstants.*;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.lambda.internal.handler.BoilerHandler;
import org.openhab.binding.modbus.lambda.internal.handler.BufferHandler;
import org.openhab.binding.modbus.lambda.internal.handler.GeneralHandler;
import org.openhab.binding.modbus.lambda.internal.handler.HeatingCircuitHandler;
import org.openhab.binding.modbus.lambda.internal.handler.HeatpumpHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LambdaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 */
@NonNullByDefault
@Component(configurationPid = "binding.lambda", service = ThingHandlerFactory.class)
public class LambdaHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(LambdaHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_GENERAL, THING_TYPE_HEAT_PUMP,
            THING_TYPE_BOILER, THING_TYPE_BUFFER, THING_TYPE_HEATING_CIRCUIT);

    private static final Map<ThingTypeUID, Function<Thing, ThingHandler>> HANDLER_FACTORY_MAP = Map.of(
            THING_TYPE_HEAT_PUMP, HeatpumpHandler::new, THING_TYPE_GENERAL, GeneralHandler::new, THING_TYPE_BUFFER,
            BufferHandler::new, THING_TYPE_BOILER, BoilerHandler::new, THING_TYPE_HEATING_CIRCUIT,
            HeatingCircuitHandler::new);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.debug("LambdaHandlerFactory thingTypeUID: {}", thingTypeUID);

        Function<Thing, ThingHandler> factory = HANDLER_FACTORY_MAP.get(thingTypeUID);
        return factory != null ? factory.apply(thing) : null;
    }
}
