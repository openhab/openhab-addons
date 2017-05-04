/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hs110.internal;

import static org.openhab.binding.hs110.HS110BindingConstants.*;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.hs110.handler.HS110Handler;

/**
 * The {@link HS110HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Christian Fischer - Initial contribution
 */
public class HS110HandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_HS100)) {
            return new HS110Handler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_HS110)) {
            return new HS110Handler(thing);
        }

        return null;
    }
}
