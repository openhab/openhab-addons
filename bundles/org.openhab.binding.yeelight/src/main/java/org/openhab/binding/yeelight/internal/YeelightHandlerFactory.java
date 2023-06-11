/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.yeelight.internal;

import static org.openhab.binding.yeelight.internal.YeelightBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.openhab.binding.yeelight.internal.handler.YeelightCeilingHandler;
import org.openhab.binding.yeelight.internal.handler.YeelightCeilingWithAmbientHandler;
import org.openhab.binding.yeelight.internal.handler.YeelightCeilingWithNightHandler;
import org.openhab.binding.yeelight.internal.handler.YeelightColorHandler;
import org.openhab.binding.yeelight.internal.handler.YeelightStripeHandler;
import org.openhab.binding.yeelight.internal.handler.YeelightWhiteHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link YeelightHandlerFactory} is responsible for returning supported things and handlers for the devices.
 *
 * @author Coaster Li - Initial contribution
 * @author Nikita Pogudalov - Added YeelightCeilingWithNightHandler for Ceiling 1
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.yeelight")
public class YeelightHandlerFactory extends BaseThingHandlerFactory {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();

    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_CEILING);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_CEILING1);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_CEILING3);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_CEILING4);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_DOLPHIN);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_CTBULB);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_WONDER);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_STRIPE);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_DESKLAMP);
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
        } else if (thingTypeUID.equals(THING_TYPE_CEILING) || thingTypeUID.equals(THING_TYPE_DESKLAMP)) {
            return new YeelightCeilingHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_CEILING1) || thingTypeUID.equals(THING_TYPE_CEILING3)) {
            return new YeelightCeilingWithNightHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_CEILING4)) {
            return new YeelightCeilingWithAmbientHandler(thing);
        } else {
            return null;
        }
    }
}
