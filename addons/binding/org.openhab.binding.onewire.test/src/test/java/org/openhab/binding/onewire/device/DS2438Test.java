/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.device.DS2438;
import org.openhab.binding.onewire.internal.device.DS2438.LightSensorType;
import org.openhab.binding.onewire.test.AbstractDeviceTest;

/**
 * Tests cases for {@link DS2438}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class DS2438Test extends AbstractDeviceTest {

    @Before
    public void setupMocks() {
        setupMocks(THING_TYPE_MS_TH);
        deviceTestClazz = DS2438.class;

        addChannel(CHANNEL_TEMPERATURE, "Number:Temperature");
        addChannel(CHANNEL_HUMIDITY, "Number:Dimensionless");
        addChannel(CHANNEL_ABSOLUTE_HUMIDITY, "Number:Density");
        addChannel(CHANNEL_DEWPOINT, "Number:Temperature");
        addChannel(CHANNEL_VOLTAGE, "Number:Voltage");
        addChannel(CHANNEL_CURRENT, "Number:Current");
        addChannel(CHANNEL_LIGHT, "Number:Illuminance");
        addChannel(CHANNEL_SUPPLYVOLTAGE, "Number:Voltage");
    }

    @Test
    public void temperatureChannel() {
        instantiateDevice();

        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
            Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(10.0));

            testDevice.enableChannel(CHANNEL_TEMPERATURE);
            testDevice.configureChannels();
            inOrder.verify(mockThingHandler).getThing();
            testDevice.refresh(mockBridgeHandler, true);

            inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId), any());
            inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_TEMPERATURE), eq(new QuantityType<>("10.0 °C")));

            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void humidityChannel() {
        instantiateDevice();

        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
            Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(10.0));

            testDevice.enableChannel(CHANNEL_HUMIDITY);
            testDevice.enableChannel(CHANNEL_ABSOLUTE_HUMIDITY);
            testDevice.enableChannel(CHANNEL_DEWPOINT);
            testDevice.configureChannels();
            inOrder.verify(mockThingHandler).getThing();
            testDevice.refresh(mockBridgeHandler, true);

            inOrder.verify(mockBridgeHandler, times(2)).readDecimalType(eq(testSensorId), any());
            inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_HUMIDITY), eq(new QuantityType<>("10.0 %")));
            inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_ABSOLUTE_HUMIDITY),
                    eq(new QuantityType<>("0.9381970824113001000 g/m³")));
            inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_DEWPOINT),
                    eq(new QuantityType<>("-20.31395053870025 °C")));

            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void voltageChannel() {
        instantiateDevice();

        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
            Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(2.0));

            testDevice.enableChannel(CHANNEL_VOLTAGE);
            testDevice.configureChannels();
            inOrder.verify(mockThingHandler).getThing();
            testDevice.refresh(mockBridgeHandler, true);

            inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId), any());
            inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_VOLTAGE), eq(new QuantityType<>("2.0 V")));

            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void currentChannel() {
        instantiateDevice();

        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);

            Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(2.0));

            testDevice.enableChannel(CHANNEL_CURRENT);
            testDevice.configureChannels();
            inOrder.verify(mockThingHandler).getThing();
            testDevice.refresh(mockBridgeHandler, true);

            inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId), any());
            inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_CURRENT), eq(new QuantityType<>("2.0 mA")));

            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void lightChannel() {
        instantiateDevice();

        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
            Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(0.1));

            testDevice.enableChannel(CHANNEL_LIGHT);
            testDevice.configureChannels();
            inOrder.verify(mockThingHandler).getThing();
            ((DS2438) testDevice).setLightSensorType(LightSensorType.ELABNET_V1);
            testDevice.refresh(mockBridgeHandler, true);

            inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId), any());
            inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_LIGHT), eq(new QuantityType<>("97442 lx")));

            ((DS2438) testDevice).setLightSensorType(LightSensorType.ELABNET_V2);
            testDevice.refresh(mockBridgeHandler, true);

            inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId), any());
            inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_LIGHT), eq(new QuantityType<>("134 lx")));

            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void supplyVoltageChannel() {
        instantiateDevice();

        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
            Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(2.0));

            testDevice.enableChannel(CHANNEL_SUPPLYVOLTAGE);
            testDevice.configureChannels();
            inOrder.verify(mockThingHandler).getThing();
            testDevice.refresh(mockBridgeHandler, true);

            inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId), any());
            inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_SUPPLYVOLTAGE), eq(new QuantityType<>("2.0 V")));

            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void noChannel() {
        instantiateDevice();

        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
            Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(2.0));

            testDevice.configureChannels();
            inOrder.verify(mockThingHandler).getThing();
            testDevice.refresh(mockBridgeHandler, true);

            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }
}
