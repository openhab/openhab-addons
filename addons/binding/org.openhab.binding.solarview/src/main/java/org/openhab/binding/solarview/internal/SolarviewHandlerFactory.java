/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solarview.internal;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.solarview.handler.SolarviewBridgeHandler;
import org.openhab.binding.solarview.handler.SolarviewHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolarviewHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Guenther Schreiner - Initial contribution
 */

public class SolarviewHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SolarviewHandlerFactory.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        logger.trace("supportsThingType({}) called.", thingTypeUID);
        return SolarviewBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || SolarviewHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ThingHandler createHandler(Thing thing) {
        logger.trace("createHandler({}) called.", thing.getLabel());

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID != null) {
            logger.debug("Trying to create a handler for ThingType '{}'", thingTypeUID);

            // Handle Bridge creation as 1st choice
            if (SolarviewBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
                logger.debug("Creating a SolarviewBridgeHandler for thing '{}'.", thing.getUID());
                logger.debug("Creating a SolarviewBridgeHandler for thing '{}'.", thingTypeUID);
                SolarviewBridgeHandler handler = new SolarviewBridgeHandler((Bridge) thing);
                return handler;
            }

            else if (SolarviewHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
                logger.debug("Creating a SolarviewHandler for thing '{}'.", thing.getUID());
                return new SolarviewHandler(thing);
            } else {
                logger.warn("ThingHandler not found for {}.", thingTypeUID);
                return null;
            }
        }
        return null;
    }

}
/**
 * end-of-SolarviewHandlerFactory.java
 */
