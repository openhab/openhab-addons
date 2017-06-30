/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast.internal;

import static org.openhab.binding.snapcast.SnapcastBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.snapcast.handler.SnapclientHandler;
import org.openhab.binding.snapcast.handler.SnapgroupHandler;
import org.openhab.binding.snapcast.handler.SnapserverHandler;

/**
 * The {@link SnapcastHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Steffen Folman Sørensen - Initial contribution
 */
public class SnapcastHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_SNAPSERVER, THING_TYPE_SNAPCLIENT, THING_TYPE_SNAPGROUP));
    // Collections.addAll(ThingTypeUID.class, THING_TYPE_SNAPSERVER, THING_TYPE_SNAPCLIENT);

    // Collections.singleton(THING_TYPE_SNAPSERVER);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_SNAPSERVER)) {
            return new SnapserverHandler((Bridge) thing);
        }
        if (thingTypeUID.equals(THING_TYPE_SNAPCLIENT)) {
            return new SnapclientHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_SNAPGROUP)) {
            return new SnapgroupHandler(thing);
        }

        return null;
    }
}
