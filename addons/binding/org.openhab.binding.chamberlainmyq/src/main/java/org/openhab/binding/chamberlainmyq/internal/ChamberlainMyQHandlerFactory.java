/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.chamberlainmyq.internal;

import static org.openhab.binding.chamberlainmyq.ChamberlainMyQBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.chamberlainmyq.handler.ChamberlainMyQDoorOpenerHandler;
import org.openhab.binding.chamberlainmyq.handler.ChamberlainMyQGatewayHandler;
import org.openhab.binding.chamberlainmyq.handler.ChamberlainMyQLightHandler;

/**
 * The {@link ChamberlainMyQHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Scott Hanson - Initial contribution
 */
public class ChamberlainMyQHandlerFactory extends BaseThingHandlerFactory {
    public static final Set<ThingTypeUID> DISCOVERABLE_DEVICE_TYPES_UIDS = Stream
            .of(THING_TYPE_DOOR_OPENER, THING_TYPE_LIGHT).collect(Collectors.toSet());

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_MYQ_BRIDGE);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)
                || DISCOVERABLE_DEVICE_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(THING_TYPE_MYQ_BRIDGE)) {
            return new ChamberlainMyQGatewayHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_TYPE_DOOR_OPENER)) {
            return new ChamberlainMyQDoorOpenerHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_LIGHT)) {
            return new ChamberlainMyQLightHandler(thing);
        }
        return null;
    }
}
