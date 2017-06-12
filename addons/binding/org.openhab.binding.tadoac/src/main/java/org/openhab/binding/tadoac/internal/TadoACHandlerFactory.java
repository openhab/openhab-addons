/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tadoac.internal;

import static org.openhab.binding.tadoac.TadoACBindingConstants.THING_TYPE_AIRCONDITIONER;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.tadoac.handler.TadoACHandler;

/**
 * The {@link TadoACHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jonas Fleck - Initial contribution
 */
public class TadoACHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(THING_TYPE_AIRCONDITIONER);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_AIRCONDITIONER)) {
            return new TadoACHandler(thing);
        }

        return null;
    }
}
