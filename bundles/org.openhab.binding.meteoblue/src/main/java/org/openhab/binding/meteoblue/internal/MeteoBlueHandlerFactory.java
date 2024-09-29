/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.meteoblue.internal;

import static org.openhab.binding.meteoblue.internal.MeteoBlueBindingConstants.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.binding.meteoblue.internal.handler.MeteoBlueBridgeHandler;
import org.openhab.binding.meteoblue.internal.handler.MeteoBlueHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link MeteoBlueHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Chris Carman - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.meteoblue")
public class MeteoBlueHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(BRIDGE_THING_TYPES_UIDS.stream(), MeteoBlueBindingConstants.SUPPORTED_THING_TYPES_UIDS.stream())
            .collect(Collectors.toSet());

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_WEATHER)) {
            return new MeteoBlueHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            return new MeteoBlueBridgeHandler((Bridge) thing);
        }

        return null;
    }
}
