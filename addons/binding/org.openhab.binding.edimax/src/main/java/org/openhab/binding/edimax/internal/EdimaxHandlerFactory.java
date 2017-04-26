/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.edimax.internal;

import static org.openhab.binding.edimax.EdimaxBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.edimax.handler.Edimax1101Handler;
import org.openhab.binding.edimax.handler.Edimax2101Handler;

/**
 * The {@link EdimaxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Falk Harnisch - Initial contribution
 */
public class EdimaxHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(THING_TYPE_SP1101W, THING_TYPE_SP2101W));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_SP1101W)) {
            return new Edimax1101Handler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SP2101W)) {
            return new Edimax2101Handler(thing);
        }

        return null;
    }
}