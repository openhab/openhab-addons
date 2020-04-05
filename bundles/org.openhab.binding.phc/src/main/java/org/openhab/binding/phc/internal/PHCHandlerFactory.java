/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.phc.internal;

import static org.openhab.binding.phc.internal.PHCBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.phc.internal.handler.PHCBridgeHandler;
import org.openhab.binding.phc.internal.handler.PHCHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link PHCHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jonas Hohaus - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.phc")
public class PHCHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_BRIDGE, THING_TYPE_AM, THING_TYPE_EM, THING_TYPE_JRM).collect(Collectors.toSet()));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        Thing thing;

        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
                thing = super.createThing(thingTypeUID, configuration, thingUID, null);
            } else {
                ThingUID phcThingUID = new ThingUID(thingTypeUID, configuration.get(ADDRESS).toString().toUpperCase());
                thing = super.createThing(thingTypeUID, configuration, phcThingUID, bridgeUID);
            }
        } else {
            throw new IllegalArgumentException(
                    "The thing type " + thingTypeUID + " is not supported by the phc binding.");
        }

        return thing;
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        ThingHandler handler = null;

        if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            handler = new PHCBridgeHandler((Bridge) thing);
        } else if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            handler = new PHCHandler(thing);
        }

        return handler;
    }
}
