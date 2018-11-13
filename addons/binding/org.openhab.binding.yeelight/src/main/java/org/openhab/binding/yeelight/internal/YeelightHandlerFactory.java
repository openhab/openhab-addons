/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yeelight.internal;

import static org.openhab.binding.yeelight.internal.YeelightBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.yeelight.internal.handler.YeelightCeilingHandler;
import org.openhab.binding.yeelight.internal.handler.YeelightColorHandler;
import org.openhab.binding.yeelight.internal.handler.YeelightStripeHandler;
import org.openhab.binding.yeelight.internal.handler.YeelightWhiteHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link YeelightHandlerFactory} is responsible for returning supported things and handlers for the devices.
 *
 * @author Coaster Li - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.yeelight")
public class YeelightHandlerFactory extends BaseThingHandlerFactory {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();
    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_CEILING);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_CEILING1);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_CEILING3);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_DOLPHIN);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_CTBULB);
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

        if (thingTypeUID.equals(THING_TYPE_DOLPHIN) || thingTypeUID.equals(THING_TYPE_CTBULB)) {
            return new YeelightWhiteHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_WONDER)) {
            return new YeelightColorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_STRIPE)) {
            return new YeelightStripeHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_CEILING) || thingTypeUID.equals(THING_TYPE_CEILING1)
                || thingTypeUID.equals(THING_TYPE_CEILING3)) {
            return new YeelightCeilingHandler(thing);
        } else {
            return null;
        }
    }
}
