/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal;

import static org.openhab.binding.hyperion.HyperionBindingConstants.*;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.hyperion.handler.HyperionHandler;
import org.openhab.binding.hyperion.handler.HyperionNgHandler;

/**
 * The {@link HyperionHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Daniel Walters - Initial contribution
 */
public class HyperionHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_SERVER_V1)) {
            return new HyperionHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SERVER_NG)) {
            return new HyperionNgHandler(thing);
        }

        return null;
    }
}
