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
package org.openhab.binding.hive.internal;

import static org.openhab.binding.hive.internal.HiveBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.hive.internal.handler.HiveBridgeHandler;
import org.openhab.binding.hive.internal.handler.HiveThermostatHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link HiveHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Chris Foot - Initial contribution
 */
@Component(configurationPid = "binding.hive", service = ThingHandlerFactory.class)
@NonNullByDefault
public class HiveHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.concat(HiveBindingConstants.BRIDGE_THING_TYPES_UIDS.stream(),
                    HiveBindingConstants.SUPPORTED_THING_TYPES_UIDS.stream()).collect(Collectors.toSet()));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (BRIDGE_THING_TYPE.equals(thingTypeUID)) {
            HiveBridgeHandler handler = new HiveBridgeHandler((Bridge) thing);
            return handler;
        } else if (THERMOSTAT_THING_TYPE.equals(thingTypeUID)) {
            return new HiveThermostatHandler(thing);
        }

        return null;
    }

}
