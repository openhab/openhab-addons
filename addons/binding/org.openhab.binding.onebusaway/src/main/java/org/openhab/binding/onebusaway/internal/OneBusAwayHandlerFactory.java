/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onebusaway.internal;

import static org.openhab.binding.onebusaway.OneBusAwayBindingConstants.*;

import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.onebusaway.internal.handler.ApiHandler;
import org.openhab.binding.onebusaway.internal.handler.RouteHandler;
import org.openhab.binding.onebusaway.internal.handler.StopHandler;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link OneBusAwayHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Shawn Wilsher - Initial contribution
 */
public class OneBusAwayHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(ApiHandler.SUPPORTED_THING_TYPE,
            RouteHandler.SUPPORTED_THING_TYPE, StopHandler.SUPPORTED_THING_TYPE);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_API)) {
            return new ApiHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_TYPE_ROUTE)) {
            return new RouteHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_STOP)) {
            return new StopHandler((Bridge) thing);
        }

        return null;
    }
}
