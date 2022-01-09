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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.device.DS18x20;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;

/**
 * Tests cases for {@link DS18x20}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS18x20Test extends DeviceTestParent<DS18x20> {

    @BeforeEach
    public void setupMocks() {
        setupMocks(THING_TYPE_BASIC, DS18x20.class);

        Map<String, Object> channelConfig = new HashMap<>();
        channelConfig.put(CONFIG_IGNORE_POR, true);
        addChannel(CHANNEL_TEMPERATURE, "Number:Temperature", new Configuration(channelConfig));
    }

    @Test
    public void temperatureTest() throws OwException {
        final DS18x20 testDevice = instantiateDevice();
        final InOrder inOrder = Mockito.inOrder(mockThingHandler, mockBridgeHandler);

        Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
        Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(15.0));

        testDevice.enableChannel(CHANNEL_TEMPERATURE);
        testDevice.configureChannels();
        testDevice.refresh(mockBridgeHandler, true);

        inOrder.verify(mockBridgeHandler, times(1)).readDecimalType(eq(testSensorId), any());
        inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_TEMPERATURE), eq(new QuantityType<>("15.0 °C")));
    }

    @Test
    public void temperatureIgnorePORTest() throws OwException {
        final DS18x20 testDevice = instantiateDevice();
        final InOrder inOrder = Mockito.inOrder(mockThingHandler, mockBridgeHandler);

        Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
        Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(85.0));

        testDevice.enableChannel(CHANNEL_TEMPERATURE);
        testDevice.configureChannels();
        testDevice.refresh(mockBridgeHandler, true);

        inOrder.verify(mockBridgeHandler, times(1)).readDecimalType(eq(testSensorId), any());
        inOrder.verify(mockThingHandler, times(0)).postUpdate(eq(CHANNEL_TEMPERATURE), any());
    }
}
