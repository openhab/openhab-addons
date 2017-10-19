/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.factory;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.SUPPORTED_DEVICE_TYPES_UIDS;
import static org.openhab.binding.mysensors.MySensorsBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.mysensors.MySensorsBindingConstants.THING_TYPE_BRIDGE_ETH;
import static org.openhab.binding.mysensors.MySensorsBindingConstants.THING_TYPE_BRIDGE_MQTT;
import static org.openhab.binding.mysensors.MySensorsBindingConstants.THING_TYPE_BRIDGE_SER;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.mysensors.handler.MySensorsBridgeHandler;
import org.openhab.binding.mysensors.handler.MySensorsThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MySensorsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Tim Oberföll
 */
public class MySensorsHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(getClass());
    

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_DEVICE_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        logger.trace("Creating handler for thing: {}", thing.getUID());
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        ThingHandler handler = null;

        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            handler = new MySensorsThingHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_BRIDGE_SER) 
                || thingTypeUID.equals(THING_TYPE_BRIDGE_ETH)
                || thingTypeUID.equals(THING_TYPE_BRIDGE_MQTT)) {
            handler = new MySensorsBridgeHandler((Bridge) thing);
        } else {
            logger.error("Thing {} cannot be configured, is this thing supported by the binding?", thingTypeUID);
        }

        return handler;
    }
    
}
