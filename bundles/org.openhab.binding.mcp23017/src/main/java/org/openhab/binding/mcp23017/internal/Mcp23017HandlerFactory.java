/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mcp23017.internal;

import static org.openhab.binding.mcp23017.internal.Mcp23017BindingConstants.THING_TYPE_MCP23017;

import java.util.Collections;
import java.util.Set;

import org.openhab.binding.mcp23017.internal.handler.Mcp23017Handler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Mcp23017HandlerFactory} is responsible for creating thing
 * handlers.
 *
 * @author Anatol Ogorek - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.mcp23017")
public class Mcp23017HandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_MCP23017);
    private final Logger logger = LoggerFactory.getLogger(Mcp23017HandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.debug("Trying to create handler for {}", thingTypeUID.getAsString());
        if (thingTypeUID.equals(THING_TYPE_MCP23017)) {
            logger.debug("Handler match for {}", thingTypeUID.getAsString());
            return new Mcp23017Handler(thing);
        }
        logger.debug("No handler match for {}", thingTypeUID.getAsString());
        return null;
    }
}
