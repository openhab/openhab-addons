/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome.internal;

import static org.openhab.binding.evohome.EvoHomeBindingConstants.*;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.evohome.handler.EvoHomeHandler;
import org.openhab.binding.evohome.handler.EvohomeGatewayHandler;

/**
 * The {@link EvoHomeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Neil Renaud - Initial contribution
 */
public class EvoHomeHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(THING_TYPE_EVOHOME_GATEWAY);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_EVOHOME_GATEWAY)) {
            return new EvohomeGatewayHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_TYPE_EVOHOME_RADIATOR_VALVE)) {
            return new EvoHomeHandler(thing);
        }

        return null;
    }

    @Override
    public ThingHandler registerHandler(Thing thing) {
        // TODO Auto-generated method stub
        return super.registerHandler(thing);
    }
}
