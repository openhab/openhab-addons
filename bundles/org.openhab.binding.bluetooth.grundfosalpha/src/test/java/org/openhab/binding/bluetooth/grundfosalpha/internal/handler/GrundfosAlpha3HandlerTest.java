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
package org.openhab.binding.bluetooth.grundfosalpha.internal.handler;

import static org.mockito.Mockito.*;
import static org.openhab.binding.bluetooth.grundfosalpha.internal.GrundfosAlphaBindingConstants.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.util.HexUtils;

/**
 * Tests for {@link GrundfosAlpha3Handler}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class GrundfosAlpha3HandlerTest {
    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;

    @Test
    void electricalReadingsArePublishedInTheirMeasurementUnits() {
        when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE_ALPHA3, "dummy"));
        var handler = new GrundfosAlpha3Handler(thing);
        handler.setCallback(callback);
        var characteristic = new BluetoothCharacteristic(UUID.fromString("859cffd1-036e-432a-aa28-1a0085b87ba9"), 1);
        byte[] response = HexUtils.hexToBytes(
                "2430F8E70A2C000100010000254357878B439781803D21B00040F19C0040EA4A404536FDB4FFC00000421C000042040000017317");

        handler.onCharacteristicUpdate(characteristic, response);

        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_VOLTAGE_AC),
                new QuantityType<>(new BigDecimal("215.5"), Units.VOLT));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_POWER),
                new QuantityType<>(new BigDecimal("7.6"), Units.WATT));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_MOTOR_SPEED),
                new QuantityType<>(2928, Units.RPM));
        verifyNoMoreInteractions(callback);
    }

    @Test
    void flowAndHeadRetainTheirPublishedUnitsAndPrecision() {
        when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE_ALPHA3, "dummy"));
        var handler = new GrundfosAlpha3Handler(thing);
        handler.setCallback(callback);
        var characteristic = new BluetoothCharacteristic(UUID.fromString("859cffd1-036e-432a-aa28-1a0085b87ba9"), 1);

        handler.onCharacteristicUpdate(characteristic,
                HexUtils.hexToBytes("2423F8E70A1F000130010000183952A66C468F48AC7FFFFFFF7FFFFFFF41FF21397FFFFFFF44A8"));

        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_FLOW_RATE),
                new QuantityType<>(new BigDecimal("0.723"), Units.CUBICMETRE_PER_HOUR));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_PUMP_HEAD),
                new QuantityType<>(new BigDecimal("1.83403"), SIUnits.METRE));
        verifyNoMoreInteractions(callback);
    }
}
