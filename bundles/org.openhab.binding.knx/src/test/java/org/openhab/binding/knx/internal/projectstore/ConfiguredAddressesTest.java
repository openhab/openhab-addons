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
package org.openhab.binding.knx.internal.projectstore;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.knx.internal.KNXBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.handler.DeviceThingHandler;
import org.openhab.binding.knx.internal.project.KnxConfiguredAddress;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Checks how the configured addresses of a bridge are collected over its Things.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ConfiguredAddressesTest {

    private static KnxConfiguredAddress address(String label) {
        return new KnxConfiguredAddress(65281, "1.001", false, true, label);
    }

    private static Thing thing(String id, ThingTypeUID type, @Nullable ThingHandler handler) {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("knx", "device", id));
        when(thing.getThingTypeUID()).thenReturn(type);
        when(thing.getHandler()).thenReturn(handler);
        return thing;
    }

    private static DeviceThingHandler handler(String label) {
        DeviceThingHandler handler = mock(DeviceThingHandler.class);
        when(handler.configuredAddresses()).thenReturn(List.of(address(label)));
        return handler;
    }

    @Test
    void thingsInUidOrderAndDeviceThingsWithoutHandlerCounted() {
        Bridge bridge = mock(Bridge.class);
        List<Thing> things = List.of(thing("zeta", THING_TYPE_DEVICE, handler("zeta")),
                thing("idle", THING_TYPE_DEVICE, null), thing("alpha", THING_TYPE_DEVICE, handler("alpha")),
                thing("other", new ThingTypeUID("knx", "made-up"), null));
        when(bridge.getThings()).thenReturn(things);

        ConfiguredAddresses configured = ConfiguredAddresses.of(bridge);

        assertEquals(List.of(address("alpha"), address("zeta")), configured.addresses());
        assertEquals(1, configured.thingsWithoutHandler());
    }

    @Test
    void aBridgeWithoutThingsHasNothing() {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getThings()).thenReturn(List.of());

        assertEquals(new ConfiguredAddresses(List.of(), 0), ConfiguredAddresses.of(bridge));
    }
}
