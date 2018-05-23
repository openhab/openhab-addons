/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yeelight.internal;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.yeelight.handler.YeelightCeilingHandler;
import org.openhab.binding.yeelight.handler.YeelightColorHandler;
import org.openhab.binding.yeelight.handler.YeelightStripeHandler;
import org.openhab.binding.yeelight.handler.YeelightWhiteHandler;

import static org.openhab.binding.yeelight.YeelightBindingConstants.*;

/**
 * The {@link YeelightHandlerFactory} is responsible for returning supported things and handlers for the devices.
 *
 * @author Coaster Li - Initial contribution
 */
public class YeelightHandlerFactory extends BaseThingHandlerFactory {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();
    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_CEILING);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_DOLPHIN);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_WONDER);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_STRIPE);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_DOLPHIN)) {
            return new YeelightWhiteHandler(thing);
        }
        else if (thingTypeUID.equals(THING_TYPE_WONDER)) {
            return new YeelightColorHandler(thing);
        }
        else if (thingTypeUID.equals(THING_TYPE_STRIPE)) {
            return new YeelightStripeHandler(thing);
        }
        else if (thingTypeUID.equals(THING_TYPE_CEILING)) {
            return new YeelightCeilingHandler(thing);
        }
        else {
            return null;
        }
    }
}
