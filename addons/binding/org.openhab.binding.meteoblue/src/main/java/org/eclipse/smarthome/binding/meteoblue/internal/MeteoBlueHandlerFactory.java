/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.meteoblue.internal;

import static org.eclipse.smarthome.binding.meteoblue.MeteoBlueBindingConstants.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.binding.meteoblue.MeteoBlueBindingConstants;
import org.eclipse.smarthome.binding.meteoblue.handler.MeteoBlueBridgeHandler;
import org.eclipse.smarthome.binding.meteoblue.handler.MeteoBlueHandler;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link MeteoBlueHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Chris Carman - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true)
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
