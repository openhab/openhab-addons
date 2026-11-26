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
package org.openhab.binding.tibber.internal;

import static org.openhab.binding.tibber.internal.TibberPulseBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tibber.internal.pulse.handler.BridgeHandler;
import org.openhab.binding.tibber.internal.pulse.handler.MeterHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link TibberPulseHandlerFactory} creates the handlers of the local Pulse thing types {@code pulse-bridge} and
 * {@code pulse-meter}. The handler of the cloud thing type {@code tibberapi} is created by
 * {@link TibberHandlerFactory}.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.tibber", service = ThingHandlerFactory.class)
public class TibberPulseHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_PULSE_BRIDGE,
            THING_TYPE_PULSE_METER);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_PULSE_BRIDGE.equals(thingTypeUID) && thing instanceof Bridge bridge) {
            return new BridgeHandler(bridge);
        }
        if (THING_TYPE_PULSE_METER.equals(thingTypeUID)) {
            return new MeterHandler(thing);
        }

        return null;
    }
}
