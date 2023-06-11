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
package org.openhab.binding.digiplex.internal;

import static org.openhab.binding.digiplex.internal.DigiplexBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.digiplex.internal.handler.DigiplexAreaHandler;
import org.openhab.binding.digiplex.internal.handler.DigiplexBridgeHandler;
import org.openhab.binding.digiplex.internal.handler.DigiplexZoneHandler;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link DigiplexHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Robert Michalak - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.digiplex")
@NonNullByDefault
public class DigiplexHandlerFactory extends BaseThingHandlerFactory {

    private final SerialPortManager serialPortManager;

    @Activate
    public DigiplexHandlerFactory(final @Reference SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

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
            return new DigiplexBridgeHandler((Bridge) thing, serialPortManager);
        }
        return null;
    }
}
