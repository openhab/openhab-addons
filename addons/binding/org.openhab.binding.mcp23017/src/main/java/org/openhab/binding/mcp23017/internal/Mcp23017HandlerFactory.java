/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mcp23017.internal;

import static org.openhab.binding.mcp23017.Mcp23017BindingConstants.THING_TYPE_MCP23017;

import java.util.List;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.mcp23017.handler.Mcp23017Handler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * The {@link Mcp23017HandlerFactory} is responsible for creating thing
 * handlers.
 *
 * @author Anatol Ogorek - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.mcp23017")
public class Mcp23017HandlerFactory extends BaseThingHandlerFactory {

    private static final List<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(THING_TYPE_MCP23017);
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
