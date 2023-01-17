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
package org.openhab.binding.echonetlite.internal;

import static org.openhab.binding.echonetlite.internal.EchonetLiteBindingConstants.THING_TYPE_ECHONET_BRIDGE;
import static org.openhab.binding.echonetlite.internal.EchonetLiteBindingConstants.THING_TYPE_ECHONET_DEVICE;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link EchonetLiteHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.echonetlite", service = ThingHandlerFactory.class)
public class EchonetLiteHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ECHONET_DEVICE,
            THING_TYPE_ECHONET_BRIDGE);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ECHONET_DEVICE.equals(thingTypeUID)) {
            return new EchonetLiteHandler(thing);
        } else if (THING_TYPE_ECHONET_BRIDGE.equals(thingTypeUID)) {
            return new EchonetLiteBridgeHandler((Bridge) thing);
        }

        return null;
    }
}
