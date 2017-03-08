/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.internal;

import static org.openhab.binding.insteonplm.InsteonPLMBindingConstants.THING_TYPE_BRIDGE;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.insteonplm.InsteonPLMBindingConstants;
import org.openhab.binding.insteonplm.handler.InsteonPLMBridgeHandler;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.config.InsteonConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InsteonPLMHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Bennett - Initial contribution
 */
public class InsteonPLMHandlerFactory extends BaseThingHandlerFactory {
    private static Logger logger = LoggerFactory.getLogger(InsteonConfigProvider.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return InsteonPLMBindingConstants.BINDING_ID.equals(thingTypeUID.getBindingId());
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            return new InsteonPLMBridgeHandler((Bridge) thing);
        }

        return new InsteonThingHandler(thing);
    }
}
