/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hdanywhere.internal;

import static org.openhab.binding.hdanywhere.internal.HDanywhereBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.binding.hdanywhere.internal.handler.Mhub4K431Handler;
import org.openhab.binding.hdanywhere.internal.handler.MultiroomPlusHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link HDanywhereHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Karel Goderis - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.hdanywhere")
public class HDanywhereHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_MULTIROOMPLUS, THING_TYPE_MHUB4K431).collect(Collectors.toSet()));

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
