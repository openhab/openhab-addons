/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify.internal;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.lightify.handler.DeviceHandler;
import org.openhab.binding.lightify.handler.GatewayHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.lightify.internal.LightifyConstants.SUPPORTED_THING_TYPES_UIDS;

/**
 * @author Christoph Engelbert (@noctarius2k) - Initial contribution
 */
public class LightifyHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(LightifyHandlerFactory.class);

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (GatewayHandler.SUPPORTED_TYPES.contains(thing.getThingTypeUID())) {
            return new GatewayHandler((Bridge) thing);
        }
        if (DeviceHandler.SUPPORTED_TYPES.contains(thing.getThingTypeUID())) {
            return new DeviceHandler(thing);
        }
        return null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        boolean supported = SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
        logger.debug("Will handle: {}[{}]", thingTypeUID, supported);
        return supported;
    }
}
