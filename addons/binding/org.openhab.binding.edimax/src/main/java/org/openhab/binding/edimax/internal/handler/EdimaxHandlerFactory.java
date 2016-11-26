/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.edimax.internal.handler;

import static org.openhab.binding.edimax.EdimaxBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

/**
 * The {@link EdimaxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Falk Harnisch - Initial contribution
 */
public class EdimaxHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(THING_TYPE_SP1101W, THING_TYPE_SP2101W));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_SP1101W.equals(thingTypeUID)) {
            return new EdimaxHandler(thing);
        } else if (THING_TYPE_SP2101W.equals(thingTypeUID)) {
            return new Edimax2101Handler(thing);
        }

        return null;
    }
}
