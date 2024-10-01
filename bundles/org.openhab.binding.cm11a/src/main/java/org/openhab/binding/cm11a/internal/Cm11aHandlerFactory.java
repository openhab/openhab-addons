/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.cm11a.internal;

import static org.openhab.binding.cm11a.internal.CM11ABindingConstants.*;

import java.util.Set;

import org.openhab.binding.cm11a.internal.handler.Cm11aApplianceHandler;
import org.openhab.binding.cm11a.internal.handler.Cm11aBridgeHandler;
import org.openhab.binding.cm11a.internal.handler.Cm11aLampHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Cm11aHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bob Raker - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.cm11a")
public class Cm11aHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(Cm11aHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_SWITCH, THING_TYPE_DIMMER);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return THING_TYPE_CM11A.equals(thingTypeUID) || SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.trace("**** Cm11AHandlerFactory.createHandler for {}", thingTypeUID.getAsString());

        if (thingTypeUID.equals(THING_TYPE_CM11A)) {
            return new Cm11aBridgeHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_TYPE_SWITCH)) {
            return new Cm11aApplianceHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_DIMMER)) {
            return new Cm11aLampHandler(thing);
        }

        return null;
    }
}
