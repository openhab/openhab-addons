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
package org.openhab.binding.meteostick.internal.handler;

import static java.util.Objects.requireNonNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.meteostick.internal.MeteostickBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.HECTO;
import static org.openhab.core.library.unit.SIUnits.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests the packet handling of the {@link MeteostickBridgeHandler}.
 *
 * @author Jens Erat - Initial contribution
 */
@NonNullByDefault
public class MeteostickBridgeHandlerTest {

    private static final ThingUID BRIDGE_UID = new ThingUID(THING_TYPE_BRIDGE, "test");

    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private @Nullable MeteostickBridgeHandler handler;

    @BeforeEach
    public void setUp() {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(BRIDGE_UID);

        handler = new MeteostickBridgeHandler(bridge, mock(SerialPortManager.class));
        handler.setCallback(callback);
    }

    @Test
    public void testComputedBarometerPacketIsPublished() {
        requireNonNull(handler).processMessage("B 19.3 982.59");

        verify(callback).stateUpdated(new ChannelUID(BRIDGE_UID, CHANNEL_INDOOR_TEMPERATURE),
                new QuantityType<>(19.3, CELSIUS));
        verify(callback).stateUpdated(new ChannelUID(BRIDGE_UID, CHANNEL_PRESSURE),
                new QuantityType<>(982.6, HECTO(PASCAL)));
    }

    @Test
    public void testRawBarometerPacketIsRejected() {
        // The stick emits the raw ("o0") format at startup: six values after the sensor identifier.
        requireNonNull(handler).processMessage("B 26436 340225 261 98271 0 0");

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testShortBarometerPacketIsRejected() {
        requireNonNull(handler).processMessage("B 19.3");

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testNonNumericBarometerPacketIsRejected() {
        requireNonNull(handler).processMessage("B abc def");

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testSensorPacketWithoutHandlerIsIgnored() {
        requireNonNull(handler).processMessage("W 5 0.44 241 -67");

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testBlankMessageIsIgnored() {
        requireNonNull(handler).processMessage("   ");
        requireNonNull(handler).processMessage("");

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testImplausibleBarometerPressureIsRejected() {
        requireNonNull(handler).processMessage("B 19.3 340225");

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testImplausibleBarometerTemperatureIsRejected() {
        requireNonNull(handler).processMessage("B 26436 982.59");

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testBarometerValuesAtRangeBoundsArePublished() {
        requireNonNull(handler).processMessage("B -100 0");

        verify(callback).stateUpdated(new ChannelUID(BRIDGE_UID, CHANNEL_INDOOR_TEMPERATURE),
                new QuantityType<>(-100, CELSIUS));
        verify(callback).stateUpdated(new ChannelUID(BRIDGE_UID, CHANNEL_PRESSURE),
                new QuantityType<>(0, HECTO(PASCAL)));
    }

    @Test
    public void testBarometerTemperatureIsRoundedToSingleDecimal() {
        requireNonNull(handler).processMessage("B 19.35 982.59");

        verify(callback).stateUpdated(new ChannelUID(BRIDGE_UID, CHANNEL_INDOOR_TEMPERATURE),
                new QuantityType<>(19.4, CELSIUS));
        verify(callback).stateUpdated(new ChannelUID(BRIDGE_UID, CHANNEL_PRESSURE),
                new QuantityType<>(982.6, HECTO(PASCAL)));
    }
}
