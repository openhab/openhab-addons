/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tankerkoenig.internal;

import static org.openhab.binding.tankerkoenig.TankerkoenigBindingConstants.*;

import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.tankerkoenig.TankerkoenigBindingConstants;
import org.openhab.binding.tankerkoenig.handler.StationHandler;
import org.openhab.binding.tankerkoenig.handler.WebserviceHandler;

import com.google.common.collect.Sets;

/**
 * The {@link TankerkoenigHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dennis Dollinger - Initial contribution
 */
public class TankerkoenigHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.union(BRIDGE_THING_TYPES_UIDS,
            TankerkoenigBindingConstants.SUPPORTED_THING_TYPES_UIDS);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(BRIDGE_THING_TYPE)) {
            WebserviceHandler handler = new WebserviceHandler((Bridge) thing);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE_TANKSTELLE)) {
            return new StationHandler(thing);
        }
        return null;
    }
}
