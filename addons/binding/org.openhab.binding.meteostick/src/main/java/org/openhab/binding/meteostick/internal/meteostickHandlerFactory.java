/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meteostick.internal;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.meteostick.handler.meteostickBridgeHandler;
import org.openhab.binding.meteostick.handler.meteostickSensorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link meteostickHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Chris Jackson - Initial contribution
 */
public class meteostickHandlerFactory extends BaseThingHandlerFactory {
    private Logger logger = LoggerFactory.getLogger(meteostickHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return meteostickBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                | meteostickSensorHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        logger.debug("MeteoStick thing factory: createHandler {} of type {}", thing.getThingTypeUID(), thing.getUID());

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (meteostickBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new meteostickBridgeHandler(thing);
        }

        if (meteostickSensorHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new meteostickSensorHandler(thing);
        }

        return null;
    }
}
