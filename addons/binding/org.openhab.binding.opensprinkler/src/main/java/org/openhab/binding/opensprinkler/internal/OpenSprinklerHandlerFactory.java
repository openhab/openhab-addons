/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opensprinkler.internal;

import static org.openhab.binding.opensprinkler.OpenSprinklerBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.opensprinkler.handler.OpenSprinklerHTTPHandler;
import org.openhab.binding.opensprinkler.handler.OpenSprinklerPiHandler;

/**
 * The {@link OpenSprinklerHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Chris Graham - Initial contribution
 */
public class OpenSprinklerHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(OPENSPRINKLER_THING, OPENSPRINKLERPI_THING));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(OPENSPRINKLER_THING)) {
            return new OpenSprinklerHTTPHandler(thing);
        } else if (thingTypeUID.equals(OPENSPRINKLERPI_THING)) {
            return new OpenSprinklerPiHandler(thing);
        }

        return null;
    }
}
