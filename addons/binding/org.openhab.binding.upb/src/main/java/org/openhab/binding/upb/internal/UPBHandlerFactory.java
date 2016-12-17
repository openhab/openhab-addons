/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upb.internal;

import static org.openhab.binding.upb.UPBBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.upb.handler.DeviceHandler;
import org.openhab.binding.upb.handler.LinkHandler;
import org.openhab.binding.upb.handler.UPBBridgeHandler;

/**
 * The {@link UPBHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Chris Van Orman - Initial contribution
 * @since 2.0.0
 */
public class UPBHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(THING_TYPE_BRIDGE, THING_TYPE_SWITCH, THING_TYPE_DIMMER, THING_TYPE_LINK));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_SWITCH.equals(thingTypeUID)) {
            return new DeviceHandler(thing);
        } else if (THING_TYPE_DIMMER.equals(thingTypeUID)) {
            return new DeviceHandler(thing);
        } else if (THING_TYPE_LINK.equals(thingTypeUID)) {
            return new LinkHandler(thing);
        } else if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new UPBBridgeHandler((Bridge) thing);
        }

        return null;
    }
}
