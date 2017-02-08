/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.internal;

import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.milight.MilightBindingConstants;
import org.openhab.binding.milight.handler.MilightBridgeHandler;
import org.openhab.binding.milight.handler.MilightLedHandler;

import com.google.common.collect.Sets;

/**
 * The {@link MilightHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Graeff - Initial contribution
 */
public class MilightHandlerFactory extends BaseThingHandlerFactory {
    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets
            .union(MilightBindingConstants.BRIDGE_THING_TYPES_UIDS, MilightBindingConstants.SUPPORTED_THING_TYPES_UIDS);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(MilightBindingConstants.BRIDGE_THING_TYPE)) {
            return new MilightBridgeHandler((Bridge) thing);
        } else if (MilightBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new MilightLedHandler(thing);
        }

        return null;
    }
}
