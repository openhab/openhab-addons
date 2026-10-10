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
import java.nio.ByteBuffer;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.grundfosalpha.internal.protocol.CRC16Calculator;
import org.openhab.core.library.types.DecimalType;
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
 * @author Jacob Laursen - Cover motor current and operating counters
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class GrundfosAlpha3HandlerTest {
    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;

    @ParameterizedTest
    @CsvSource({ "0,0.000", "3600000,1.000", "444444440400,123456.789", "1364284386.704,378.968", "3601799.9996,1.000",
            "3601800,1.001", "3601800.0004,1.001" })
    void energyIsPublishedInKilowattHours(double joules, String kilowattHours) {
        when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE_ALPHA3, "dummy"));
        var handler = new GrundfosAlpha3Handler(thing);
        handler.setCallback(callback);
        var characteristic = new BluetoothCharacteristic(UUID.fromString("859cffd1-036e-432a-aa28-1a0085b87ba9"), 1);
        byte[] response = ByteBuffer.allocate(41).put(HexUtils.hexToBytes("2425F8E70A210000E80100001A"))
                .putDouble(joules).array();
        CRC16Calculator.put(response, 37);

        handler.onCharacteristicUpdate(characteristic, response);

        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_ENERGY),
                new QuantityType<>(new BigDecimal(kilowattHours), Units.KILOWATT_HOUR));
        verifyNoMoreInteractions(callback);
    }

    @Test
    void countersArePublishedAsSecondsAndStartCount() {
        when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE_ALPHA3, "dummy"));
        var handler = new GrundfosAlpha3Handler(thing);
        handler.setCallback(callback);
        var characteristic = new BluetoothCharacteristic(UUID.fromString("859cffd1-036e-432a-aa28-1a0085b87ba9"), 1);
        byte[] response = HexUtils.hexToBytes("241FF8E70A1B0000F80100001400000029000000130007C46A0000F82E000000FC0000");
        CRC16Calculator.put(response, 31);

        handler.onCharacteristicUpdate(characteristic, response);

        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_OPERATING_TIME),
                new QuantityType<>(509034, Units.SECOND));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_START_COUNT), new DecimalType(41));
        verifyNoMoreInteractions(callback);
    }

    @Test
    void electricalReadingsArePublishedInTheirMeasurementUnits() {
        when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE_ALPHA3, "dummy"));
        var handler = new GrundfosAlpha3Handler(thing);
        handler.setCallback(callback);
        var characteristic = new BluetoothCharacteristic(UUID.fromString("859cffd1-036e-432a-aa28-1a0085b87ba9"), 1);
        byte[] response = HexUtils.hexToBytes(
                "2430F8E70A2C000100010000254357878B439781803D21B00040F19C0040EA4A404536FDB4FFC00000421C000042040000017317");

        handler.onCharacteristicUpdate(characteristic, response);

        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_MOTOR_CURRENT),
                new QuantityType<>(new BigDecimal("0.039"), Units.AMPERE));
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

    @Test
    void version2CounterNotificationsPublishBothChannels() {
        when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE_ALPHA3, "dummy"));
        var handler = new GrundfosAlpha3Handler(thing);
        handler.setCallback(callback);
        var characteristic = new BluetoothCharacteristic(UUID.fromString("859cffd1-036e-432a-aa28-1a0085b87ba9"), 1);

        handler.onCharacteristicUpdate(characteristic, HexUtils.hexToBytes("2427F8E70A230000F80200001C00000041000000"));
        handler.onCharacteristicUpdate(characteristic, HexUtils.hexToBytes("000DA4BDDD00015128000DEC80000000410DA4BE"));
        verifyNoInteractions(callback);
        handler.onCharacteristicUpdate(characteristic, HexUtils.hexToBytes("0921B8"));

        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_OPERATING_TIME),
                new QuantityType<>(228900317, Units.SECOND));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_START_COUNT), new DecimalType(65));
        verifyNoMoreInteractions(callback);
    }
}
