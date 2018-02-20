/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hdanywhere.internal;

import static org.openhab.binding.hdanywhere.HDanywhereBindingConstants.*;

import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.hdanywhere.HDanywhereBindingConstants;
import org.openhab.binding.hdanywhere.handler.Mhub4K431Handler;
import org.openhab.binding.hdanywhere.handler.MultiroomPlusHandler;

import com.google.common.collect.Sets;

/**
 * The {@link HDanywhereHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Karel Goderis - Initial contribution
 */
public class HDanywhereHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_MULTIROOMPLUS,
            THING_TYPE_MHUB4K431);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_MULTIROOMPLUS)) {
            return new MultiroomPlusHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_MHUB4K431)) {
            return new Mhub4K431Handler(thing);
        }

        return null;
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (HDanywhereBindingConstants.THING_TYPE_MULTIROOMPLUS.equals(thingTypeUID)) {
            ThingUID matrixUID = getMultiroomPlusUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, matrixUID, null);
        }
        if (HDanywhereBindingConstants.THING_TYPE_MHUB4K431.equals(thingTypeUID)) {
            ThingUID matrixUID = getMhub4K431UID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, matrixUID, null);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the HDanywhere binding.");
    }

    private ThingUID getMultiroomPlusUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        String ipAddress = (String) configuration.get(MultiroomPlusHandler.IP_ADDRESS);

        if (thingUID == null) {
            thingUID = new ThingUID(thingTypeUID, ipAddress.replaceAll("[^A-Za-z0-9_]", "_"));
        }
        return thingUID;
    }

    private ThingUID getMhub4K431UID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        String ipAddress = (String) configuration.get(Mhub4K431Handler.IP_ADDRESS);

        if (thingUID == null) {
            thingUID = new ThingUID(thingTypeUID, ipAddress.replaceAll("[^A-Za-z0-9_]", "_"));
        }
        return thingUID;
    }
}
