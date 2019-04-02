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
package org.openhab.binding.digiplex.internal;

import static org.openhab.binding.digiplex.DigiplexBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.digiplex.handler.DigiplexAreaHandler;
import org.openhab.binding.digiplex.handler.DigiplexBridgeHandler;
import org.openhab.binding.digiplex.handler.DigiplexZoneHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link DigiplexHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Robert Michalak - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.digiplex")
@NonNullByDefault
public class DigiplexHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ZONE)) {
            return new DigiplexZoneHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_AREA)) {
            return new DigiplexAreaHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            DigiplexBridgeHandler bridge = new DigiplexBridgeHandler((Bridge) thing);
            return bridge;
        }

        return null;
    }

}
