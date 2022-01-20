/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.dali.internal;

import static org.openhab.binding.dali.internal.DaliBindingConstants.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dali.internal.handler.DaliDeviceHandler;
import org.openhab.binding.dali.internal.handler.DaliRgbHandler;
import org.openhab.binding.dali.internal.handler.DaliserverBridgeHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link DaliHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Robert Schmid - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.dali", service = ThingHandlerFactory.class)
public class DaliHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(DaliserverBridgeHandler.SUPPORTED_THING_TYPES.stream(),
                    DaliBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS.stream())
            .collect(Collectors.toSet());

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (DaliserverBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new DaliserverBridgeHandler((Bridge) thing);
        }
        if (THING_TYPE_DEVICE.equals(thingTypeUID) || THING_TYPE_GROUP.equals(thingTypeUID)) {
            return new DaliDeviceHandler(thing);
        }
        if (THING_TYPE_RGB.equals(thingTypeUID)) {
            return new DaliRgbHandler(thing);
        }

        return null;
    }
}
