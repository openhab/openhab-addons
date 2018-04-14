/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.s7.internal;

import static org.openhab.binding.s7.S7BindingConstants.THING_TYPE_SERVER;

import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.s7.handler.S7BridgeHandler;
import org.openhab.binding.s7.handler.S7ThingHandler;

import com.google.common.collect.Sets;

/**
 * The {@link S7HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Laurent Sibilla - Initial contribution
 */
public class S7HandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets
            .union(S7BridgeHandler.SUPPORTED_THING_TYPES, S7ThingHandler.SUPPORTED_THING_TYPES);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_SERVER)) {
            return new S7BridgeHandler((Bridge) thing);
        } else if (S7ThingHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new S7ThingHandler(thing);
        }

        return null;
    }
}
