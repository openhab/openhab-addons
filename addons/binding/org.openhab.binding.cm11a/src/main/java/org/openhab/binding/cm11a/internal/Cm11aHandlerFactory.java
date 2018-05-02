/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.cm11a.internal;

import static org.openhab.binding.cm11a.CM11ABindingConstants.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.cm11a.handler.Cm11aApplianceHandler;
import org.openhab.binding.cm11a.handler.Cm11aBridgeHandler;
import org.openhab.binding.cm11a.handler.Cm11aLampHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Cm11aHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bob Raker - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.cm11a")
public class Cm11aHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(Cm11aHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(THING_TYPE_SWITCH, THING_TYPE_DIMMER)));

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
