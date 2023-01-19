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
package org.openhab.binding.onewire.device;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.device.DS1923;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;

/**
 * Tests cases for {@link DS1923}.
 *
 * @author Jan N. Klug - Initial contribution
 * @author Michał Wójcik - Adapted to DS1923
 */
@NonNullByDefault
public class DS1923Test extends DeviceTestParent<DS1923> {
    @BeforeEach
    public void setupMocks() {
        setupMocks(THING_TYPE_MS_TX, DS1923.class);

        addChannel(CHANNEL_TEMPERATURE, "Number:Temperature");
        addChannel(CHANNEL_HUMIDITY, "Number:Dimensionless");
        addChannel(CHANNEL_ABSOLUTE_HUMIDITY, "Number:Density");
        addChannel(CHANNEL_DEWPOINT, "Number:Temperature");
    }

    @Test
    public void temperatureChannel() throws OwException {
        final DS1923 testDevice = instantiateDevice();
        final InOrder inOrder = Mockito.inOrder(mockThingHandler, mockBridgeHandler);

        Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
        Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(10.0));

        testDevice.enableChannel(CHANNEL_TEMPERATURE);
        testDevice.configureChannels();
        testDevice.refresh(mockBridgeHandler, true);

        inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId), any());
        inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_TEMPERATURE), eq(new QuantityType<>("10.0 °C")));

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void humidityChannel() throws OwException {
        final DS1923 testDevice = instantiateDevice();
        final InOrder inOrder = Mockito.inOrder(mockThingHandler, mockBridgeHandler);

        Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
        Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(10.0));

        testDevice.enableChannel(CHANNEL_HUMIDITY);
        testDevice.enableChannel(CHANNEL_ABSOLUTE_HUMIDITY);
        testDevice.enableChannel(CHANNEL_DEWPOINT);
        testDevice.configureChannels();
        testDevice.refresh(mockBridgeHandler, true);

        inOrder.verify(mockBridgeHandler, times(2)).readDecimalType(eq(testSensorId), any());
        inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_HUMIDITY), eq(new QuantityType<>("10.0 %")));
        inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_ABSOLUTE_HUMIDITY),
                eq(new QuantityType<>("0.9381970824113001000 g/m³")));
        inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_DEWPOINT),
                eq(new QuantityType<>("-20.31395053870025 °C")));

        inOrder.verifyNoMoreInteractions();
    }
}
