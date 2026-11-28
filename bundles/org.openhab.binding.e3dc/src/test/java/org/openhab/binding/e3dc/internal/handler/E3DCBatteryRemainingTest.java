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
package org.openhab.binding.e3dc.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.State;

/**
 * Tests the capacity the battery handler publishes: the device reports the capacity above the bottom of its
 * usable window, so the handler has to show the capacity that is actually stored.
 *
 * <p>
 * {@code @SuppressWarnings("null")}: Mockito is not designed with null type annotations in mind, so combining it
 * with this {@code @NonNullByDefault} test class produces "unsafe interpretation" compiler advisories with no
 * null-safety benefit.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class E3DCBatteryRemainingTest {

    private static final long WAIT_MILLIS = 10_000;
    private static final ThingUID BRIDGE_UID = new ThingUID(E3DCBindingConstants.THING_TYPE_BRIDGE, "device");
    private static final ThingUID BATTERY_UID = new ThingUID(E3DCBindingConstants.THING_TYPE_BATTERY, BRIDGE_UID,
            "battery");
    private static final double VOLTAGE = 48.4;

    /** A battery handler whose bridge reports a connection, together with the callback it publishes to. */
    private record Fixture(E3DCBatteryHandler handler, ThingHandlerCallback callback) {
    }

    private static Fixture fixture() {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        E3DCBridgeHandler bridgeHandler = mock(E3DCBridgeHandler.class);
        when(bridgeHandler.isConnected()).thenReturn(true);
        Bridge bridge = BridgeBuilder.create(E3DCBindingConstants.THING_TYPE_BRIDGE, BRIDGE_UID).build();
        bridge.setHandler(bridgeHandler);
        when(callback.getBridge(BRIDGE_UID)).thenReturn(bridge);
        Thing thing = ThingBuilder.create(E3DCBindingConstants.THING_TYPE_BATTERY, BATTERY_UID).withBridge(BRIDGE_UID)
                .build();
        E3DCBatteryHandler handler = new E3DCBatteryHandler(thing);
        handler.setCallback(callback);
        handler.initialize();
        return new Fixture(handler, callback);
    }

    private static ChannelUID channel(String channelId) {
        return new ChannelUID(BATTERY_UID, E3DCBindingConstants.GROUP_PACK, channelId);
    }

    private static RscpValue number(int tag, double value) {
        return RscpValue.of(tag, RscpType.FLOAT32, (float) value);
    }

    private static RscpValue module(int index, double remainingCapacity) {
        return RscpValue.container(RscpTag.BAT_DCB_INFO, List.of(RscpValue.uchar8(RscpTag.BAT_DCB_INDEX, index),
                number(RscpTag.BAT_DCB_REMAINING_CAPACITY, remainingCapacity)));
    }

    private static RscpValue answer(int moduleCount, double usableRemaining, double design, double usable,
            List<RscpValue> modules) {
        List<RscpValue> children = new ArrayList<>();
        children.add(RscpValue.uchar8(RscpTag.BAT_INDEX, 0));
        children.add(RscpValue.int32(RscpTag.BAT_DCB_COUNT, moduleCount));
        children.add(number(RscpTag.BAT_MODULE_VOLTAGE, VOLTAGE));
        children.add(number(RscpTag.BAT_USABLE_REMAINING_CAPACITY, usableRemaining));
        children.add(number(RscpTag.BAT_DESIGN_CAPACITY, design));
        children.add(number(RscpTag.BAT_USABLE_CAPACITY, usable));
        children.addAll(modules);
        return RscpValue.container(0, List.of(RscpValue.container(RscpTag.BAT_DATA, children)));
    }

    private static QuantityType<?> publishedCapacity(ThingHandlerCallback callback, String channelId) {
        ArgumentCaptor<State> state = ArgumentCaptor.forClass(State.class);
        verify(callback, timeout(WAIT_MILLIS)).stateUpdated(eq(channel(channelId)), state.capture());
        assertTrue(state.getValue() instanceof QuantityType<?>, "the state has to be a quantity: " + state.getValue());
        return (QuantityType<?>) state.getValue();
    }

    /** The cell modules report their capacity, so a battery below its usable window still shows what is stored. */
    @Test
    void storedCapacityIsTheSumOfTheModules() {
        Fixture fixture = fixture();

        fixture.handler().onData(answer(2, -9.14, 252.0, 228.06, List.of(module(0, 7.3), module(1, 7.5))));

        QuantityType<?> capacity = publishedCapacity(fixture.callback(), "usable-remaining-capacity");
        assertEquals(Units.AMPERE_HOUR, capacity.getUnit());
        assertEquals(14.8, capacity.doubleValue(), 0.001);
        QuantityType<?> energy = publishedCapacity(fixture.callback(), "usable-remaining-energy");
        assertEquals(Units.KILOWATT_HOUR, energy.getUnit());
        assertEquals(14.8 * VOLTAGE / 1000.0, energy.doubleValue(), 0.001);
    }

    /** Without the cell modules the reserve is added back to the value the device reports. */
    @Test
    void storedCapacityFallsBackToTheDeviceValueAndTheReserve() {
        Fixture fixture = fixture();

        fixture.handler().onData(answer(0, -10.0, 250.0, 230.0, List.of()));

        QuantityType<?> capacity = publishedCapacity(fixture.callback(), "usable-remaining-capacity");
        assertEquals(10.0, capacity.doubleValue(), 0.001);
    }

    /** A module that did not answer makes the sum incomplete, so the reserve is used instead. */
    @Test
    void incompleteModulesFallBackToTheReserve() {
        Fixture fixture = fixture();

        fixture.handler().onData(answer(3, -9.14, 252.0, 228.06, List.of(module(0, 7.0), module(1, 7.0))));

        QuantityType<?> capacity = publishedCapacity(fixture.callback(), "usable-remaining-capacity");
        assertEquals(14.8, capacity.doubleValue(), 0.001);
    }

    /** The battery cannot hold less than nothing, whatever the device reports. */
    @Test
    void storedCapacityIsNeverNegative() {
        Fixture fixture = fixture();

        fixture.handler().onData(answer(0, -30.0, 252.0, 228.06, List.of()));

        QuantityType<?> capacity = publishedCapacity(fixture.callback(), "usable-remaining-capacity");
        assertEquals(0.0, capacity.doubleValue(), 0.001);
    }
}
