/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.internal;

import static org.openhab.binding.astro.AstroBindingConstants.THING_TYPE_MOON;
import static org.openhab.binding.astro.AstroBindingConstants.THING_TYPE_SUN;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.astro.handler.AstroThingHandler;
import org.openhab.binding.astro.handler.MoonHandler;
import org.openhab.binding.astro.handler.SunHandler;

import com.google.common.collect.Sets;

/**
 * The {@link AstroHandlerFactory} is responsible for creating things and thing handlers.
 * 
 * @author Gerhard Riegler - Initial contribution
 */
public class AstroHandlerFactory extends BaseThingHandlerFactory {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.union(SunHandler.SUPPORTED_THING_TYPES, MoonHandler.SUPPORTED_THING_TYPES);
    private static final Map<String, AstroThingHandler> astroThingHandlers = new HashMap<String, AstroThingHandler>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        AstroThingHandler thingHandler = null;
        if (thingTypeUID.equals(THING_TYPE_SUN)) {
            thingHandler = new SunHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_MOON)) {
            thingHandler = new MoonHandler(thing);
        }
        if (thingHandler != null) {
            astroThingHandlers.put(thing.getUID().toString(), thingHandler);
        }
        return thingHandler;
    }

    @Override
    public void unregisterHandler(Thing thing) {
        super.unregisterHandler(thing);
        astroThingHandlers.remove(thing.getUID().toString());
    }

    public static AstroThingHandler getHandler(String thingUid) {
        return astroThingHandlers.get(thingUid);
    }
}
