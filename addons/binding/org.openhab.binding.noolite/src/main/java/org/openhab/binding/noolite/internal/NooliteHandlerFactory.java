/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.noolite.internal;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.noolite.NooliteBindingConstants;
import org.openhab.binding.noolite.handler.NooliteHandler;
import org.openhab.binding.noolite.handler.NooliteMTRF64BridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NooliteHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Petr Shatsillo - Initial contribution
 */
public class NooliteHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(NooliteHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return NooliteBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(NooliteBindingConstants.THING_TYPE_BRIDGEMTRF64)) {
            NooliteMTRF64BridgeHandler handler = new NooliteMTRF64BridgeHandler((Bridge) thing);
            return handler;
        }

        if (thingTypeUID.equals(NooliteBindingConstants.THING_TYPE_DEVICE)) {
            return new NooliteHandler(thing);
        }

        return null;
    }
}
