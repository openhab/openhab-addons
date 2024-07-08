/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.airgradient.internal.handler;

import static org.eclipse.jdt.annotation.Checks.requireNonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.airgradient.internal.model.Measure;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.UnDefType;

/**
 * @author Jørgen Austvik - Initial contribution
 */
@SuppressWarnings({ "null" })
@NonNullByDefault
public class AirGradientLocationHandlerTest {

    private static final Measure TEST_MEASURE = new Measure() {
        {
            locationId = "12345";
            locationName = "Location name";
            pm01 = 2d;
            pm02 = 3d;
            pm10 = 4d;
            pm003Count = 636d;
            atmp = 19.63;
            rhum = null;
            rco2 = 455d;
            tvoc = 51.644928;
            wifi = -59d;
            timestamp = "2024-01-07T11:28:56.000Z";
            serialno = "ecda3b1a2a50";
            firmwareVersion = "12345";
            tvocIndex = 1d;
            noxIndex = 2d;
        }
    };

    private static final Measure TEST_MEASURE_V3_1_1 = new Measure() {
        {
            locationId = "12345";
            locationName = "Location name";
            timestamp = "2024-01-07T11:28:56.000Z";
            serialno = "ecda3b1a2a50";
            firmwareVersion = "3.1.1";
            atmpCompensated = 24.2;
            rhumCompensated = 36d;
            bootCount = 16l;
        }
    };

    @Nullable
    private AirGradientLocationHandler sut;

    @Nullable
    private ThingHandlerCallback callbackMock;

    @Nullable
    private Thing thing;

    @BeforeEach
    public void setUp() {
        callbackMock = Mockito.mock(ThingHandlerCallback.class);
        Mockito.when(callbackMock.isChannelLinked(any(ChannelUID.class))).thenReturn(true);
        thing = Mockito.mock(Thing.class);

        sut = new AirGradientLocationHandler(requireNonNull(thing));
        sut.setCallback(callbackMock);

        Mockito.when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE_LOCATION, "1234"));
    }

    @Test
    public void testSetMeasure() {
        sut.setCallback(callbackMock);
        sut.setMeasurment(TEST_MEASURE);

        verify(callbackMock).stateUpdated(new ChannelUID(sut.getThing().getUID(), CHANNEL_WIFI),
                new QuantityType<>("-59 dBm"));
        verify(callbackMock).stateUpdated(new ChannelUID(sut.getThing().getUID(), CHANNEL_PM_01),
                new QuantityType<>("2 µg/m³"));
        verify(callbackMock).stateUpdated(new ChannelUID(sut.getThing().getUID(), CHANNEL_PM_02),
                new QuantityType<>("3 µg/m³"));
        verify(callbackMock).stateUpdated(new ChannelUID(sut.getThing().getUID(), CHANNEL_PM_10),
                new QuantityType<>("4 µg/m³"));
        verify(callbackMock).stateUpdated(new ChannelUID(sut.getThing().getUID(), CHANNEL_PM_003_COUNT),
                new QuantityType<>("636"));
        verify(callbackMock).stateUpdated(new ChannelUID(sut.getThing().getUID(), CHANNEL_ATMP),
                new QuantityType<>("19.63 °C"));
        verify(callbackMock).stateUpdated(new ChannelUID(sut.getThing().getUID(), CHANNEL_RHUM), UnDefType.NULL);
        verify(callbackMock).stateUpdated(new ChannelUID(sut.getThing().getUID(), CHANNEL_RCO2),
                new QuantityType<>("455 ppm"));
        verify(callbackMock).stateUpdated(new ChannelUID(sut.getThing().getUID(), CHANNEL_TVOC),
                new QuantityType<>("51 ppb"));
    }

    // Firmware Version 3.1.1 has slight changes in the Json
    @Test
    public void testSetMeasureVersion3_1_1() {
        sut.setCallback(callbackMock);
        sut.setMeasurment(TEST_MEASURE_V3_1_1);

        verify(callbackMock).stateUpdated(new ChannelUID(sut.getThing().getUID(), CHANNEL_ATMP),
                new QuantityType<>("24.2 °C"));
        verify(callbackMock).stateUpdated(new ChannelUID(sut.getThing().getUID(), CHANNEL_RHUM),
                new QuantityType<>("36 %"));
        verify(callbackMock).stateUpdated(new ChannelUID(sut.getThing().getUID(), CHANNEL_UPLOADS_SINCE_BOOT),
                new QuantityType<>("16"));
    }
}
