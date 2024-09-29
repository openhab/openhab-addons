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
package org.openhab.binding.plugwise.internal;

import static org.openhab.binding.plugwise.internal.PlugwiseBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plugwise.internal.handler.PlugwiseRelayDeviceHandler;
import org.openhab.binding.plugwise.internal.handler.PlugwiseScanHandler;
import org.openhab.binding.plugwise.internal.handler.PlugwiseSenseHandler;
import org.openhab.binding.plugwise.internal.handler.PlugwiseStickHandler;
import org.openhab.binding.plugwise.internal.handler.PlugwiseSwitchHandler;
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
 * The {@link PlugwiseHandlerFactory} is responsible for creating Plugwise things and thing handlers.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.plugwise")
public class PlugwiseHandlerFactory extends BaseThingHandlerFactory {

    private final SerialPortManager serialPortManager;

    @Activate
    public PlugwiseHandlerFactory(final @Reference SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_STICK)) {
            return new PlugwiseStickHandler((Bridge) thing, serialPortManager);
        } else if (thingTypeUID.equals(THING_TYPE_CIRCLE) || thingTypeUID.equals(THING_TYPE_CIRCLE_PLUS)
                || thingTypeUID.equals(THING_TYPE_STEALTH)) {
            return new PlugwiseRelayDeviceHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SCAN)) {
            return new PlugwiseScanHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SENSE)) {
            return new PlugwiseSenseHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SWITCH)) {
            return new PlugwiseSwitchHandler(thing);
        }

        return null;
    }
}
