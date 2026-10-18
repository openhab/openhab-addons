/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.access;

import static org.openhab.binding.unifi.internal.access.UnifiAccessBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.access.handler.UnifiAccessBridgeHandler;
import org.openhab.binding.unifi.internal.access.handler.UnifiAccessDeviceHandler;
import org.openhab.binding.unifi.internal.access.handler.UnifiAccessDoorHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link UnifiAccessHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.unifiaccess", service = ThingHandlerFactory.class)
public class UnifiAccessHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(BRIDGE_THING_TYPE, DOOR_THING_TYPE,
            DEVICE_THING_TYPE, BRIDGE_THING_TYPE_LEGACY, DOOR_THING_TYPE_LEGACY, DEVICE_THING_TYPE_LEGACY);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        // Dispatch on the thing-type id so one handler serves both the canonical unifi:*
        // and the legacy unifiaccess:* binding IDs.
        switch (thing.getThingTypeUID().getId()) {
            case "bridge":
                return new UnifiAccessBridgeHandler((Bridge) thing);
            case "door":
                return new UnifiAccessDoorHandler(thing);
            case "device":
                return new UnifiAccessDeviceHandler(thing);
            default:
                return null;
        }
    }
}
