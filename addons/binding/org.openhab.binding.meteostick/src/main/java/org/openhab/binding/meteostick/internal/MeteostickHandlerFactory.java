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
import org.openhab.binding.meteostick.handler.MeteostickBridgeHandler;
import org.openhab.binding.meteostick.handler.MeteostickSensorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MeteostickHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Chris Jackson - Initial contribution
 */
public class MeteostickHandlerFactory extends BaseThingHandlerFactory {
    private Logger logger = LoggerFactory.getLogger(MeteostickHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return MeteostickBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                | MeteostickSensorHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        logger.debug("MeteoStick thing factory: createHandler {} of type {}", thing.getThingTypeUID(), thing.getUID());

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (MeteostickBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new MeteostickBridgeHandler(thing);
        }

        if (MeteostickSensorHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new MeteostickSensorHandler(thing);
        }

        return null;
    }
}
