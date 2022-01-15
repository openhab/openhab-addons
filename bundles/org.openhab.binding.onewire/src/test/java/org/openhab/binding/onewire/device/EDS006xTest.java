/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.onewire.internal.device.EDS006x;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;

/**
 * Tests cases for {@link EDS006x}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class EDS006xTest extends DeviceTestParent<EDS006x> {

    @BeforeEach
    public void setupMocks() {
        setupMocks(THING_TYPE_EDS_ENV, EDS006x.class);

        addChannel(CHANNEL_TEMPERATURE, "Number:Temperature");
        addChannel(CHANNEL_HUMIDITY, "Number:Dimensionless");
        addChannel(CHANNEL_ABSOLUTE_HUMIDITY, "Number:Density");
        addChannel(CHANNEL_DEWPOINT, "Number:Temperature");
        addChannel(CHANNEL_LIGHT, "Number:Illuminance");
        addChannel(CHANNEL_PRESSURE, "Number:Pressure");
    }

    @Test
    public void temperatureChannel() throws OwException {
        final EDS006x testDevice = instantiateDevice(OwSensorType.EDS0068);
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
        final EDS006x testDevice = instantiateDevice(OwSensorType.EDS0068);
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

    @Test
    public void pressureChannel() throws OwException {
        final EDS006x testDevice = instantiateDevice(OwSensorType.EDS0068);
        final InOrder inOrder = Mockito.inOrder(mockThingHandler, mockBridgeHandler);

        Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
        Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(2.0));

        testDevice.enableChannel(CHANNEL_PRESSURE);
        testDevice.configureChannels();
        testDevice.refresh(mockBridgeHandler, true);

        inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId), any());
        inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_PRESSURE), eq(new QuantityType<>("2.0 mbar")));

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lightChannel() throws OwException {
        final EDS006x testDevice = instantiateDevice(OwSensorType.EDS0068);
        final InOrder inOrder = Mockito.inOrder(mockThingHandler, mockBridgeHandler);

        Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
        Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(100));

        testDevice.enableChannel(CHANNEL_LIGHT);
        testDevice.configureChannels();
        testDevice.refresh(mockBridgeHandler, true);

        inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId), any());
        inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_LIGHT), eq(new QuantityType<>("100 lx")));

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void noChannel() throws OwException {
        final EDS006x testDevice = instantiateDevice(OwSensorType.EDS0068);
        final InOrder inOrder = Mockito.inOrder(mockThingHandler, mockBridgeHandler);

        Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
        Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(2.0));

        testDevice.configureChannels();
        testDevice.refresh(mockBridgeHandler, true);

        inOrder.verifyNoMoreInteractions();
    }
}
