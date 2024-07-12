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
package org.openhab.binding.hapero.internal.handler;

import static org.openhab.binding.hapero.internal.HaperoBindingConstants.*;

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
 * The {@link HaperoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Daniel Walter - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.hapero", service = ThingHandlerFactory.class)
public class HaperoHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE, THING_TYPE_FURNACE,
            THING_TYPE_BUFFER, THING_TYPE_BOILER, THING_TYPE_HEATING);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new HaperoBridgeHandler((Bridge) thing);
        } else if (THING_TYPE_FURNACE.equals(thingTypeUID)) {
            return new HaperoFurnaceHandler(thing);
        } else if (THING_TYPE_BUFFER.equals(thingTypeUID)) {
            return new HaperoTankHandler(thing);
        } else if (THING_TYPE_BOILER.equals(thingTypeUID)) {
            return new HaperoTankHandler(thing);
        } else if (THING_TYPE_HEATING.equals(thingTypeUID)) {
            return new HaperoHeatingHandler(thing);
        } else {
            return null;
        }
    }
}
