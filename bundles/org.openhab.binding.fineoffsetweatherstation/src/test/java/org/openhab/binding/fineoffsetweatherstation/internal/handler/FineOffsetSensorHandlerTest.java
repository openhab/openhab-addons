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
package org.openhab.binding.fineoffsetweatherstation.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_MOISTURE;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.THING_TYPE_SENSOR;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.MeasureType;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Sensor;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Tests the dynamic measurement-channel handling of {@link FineOffsetSensorHandler}.
 *
 * @author Andreas Berger - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class FineOffsetSensorHandlerTest {

    private static final ThingUID SENSOR_UID = new ThingUID(THING_TYPE_SENSOR, "testSensor");

    // must match FineOffsetSensorHandler.MISSING_MEASURAND_REMOVAL_THRESHOLD
    private static final int REMOVAL_THRESHOLD = 10;

    private @Mock @NonNullByDefault({}) Thing thingMock;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callbackMock;
    private @Mock @NonNullByDefault({}) ChannelTypeRegistry channelTypeRegistryMock;

    private @NonNullByDefault({}) FineOffsetSensorHandler handler;

    private final MeasuredValue moisture = measuredValue("moisture-soil-channel", CHANNEL_TYPE_MOISTURE);
    private final MeasuredValue soilTemp = measuredValue("temperature-soil-channel", CHANNEL_TYPE_MOISTURE);

    @BeforeEach
    public void setUp() {
        when(thingMock.getUID()).thenReturn(SENSOR_UID);
        when(thingMock.getThingTypeUID()).thenReturn(THING_TYPE_SENSOR);
        when(thingMock.getConfiguration()).thenReturn(new Configuration());
        when(thingMock.getProperties()).thenReturn(java.util.Map.of());
        when(thingMock.getChannels()).thenReturn(List.of());

        handler = new FineOffsetSensorHandler(thingMock, channelTypeRegistryMock);
        handler.setCallback(callbackMock);
    }

    private MeasuredValue measuredValue(String channelPrefix, ChannelTypeUID channelTypeUID) {
        return new MeasuredValue(MeasureType.PERCENTAGE, channelPrefix, null, channelTypeUID, new DecimalType(1),
                channelPrefix, Sensor.WH51);
    }

    private List<String> currentChannelIds() {
        return handler.getThing().getChannels().stream().map(Channel::getUID).map(uid -> uid.getId())
                .collect(Collectors.toList());
    }

    @Test
    void createsAChannelPerMeasurandPrefix() {
        handler.updateMeasuredValues(List.of(moisture, soilTemp));
        assertThat(currentChannelIds(), containsInAnyOrder("moisture-soil-channel", "temperature-soil-channel"));
    }

    @Test
    void keepsChannelAcrossTransientGapBelowThreshold() {
        handler.updateMeasuredValues(List.of(moisture, soilTemp));
        for (int i = 0; i < REMOVAL_THRESHOLD - 1; i++) {
            handler.updateMeasuredValues(List.of(moisture)); // soilTemp missing
        }
        assertThat(currentChannelIds(), containsInAnyOrder("moisture-soil-channel", "temperature-soil-channel"));
    }

    @Test
    void removesChannelAfterThreshold() {
        handler.updateMeasuredValues(List.of(moisture, soilTemp));
        for (int i = 0; i < REMOVAL_THRESHOLD; i++) {
            handler.updateMeasuredValues(List.of(moisture)); // soilTemp missing
        }
        assertThat(currentChannelIds(), containsInAnyOrder("moisture-soil-channel"));
        assertThat(currentChannelIds(), not(hasItem("temperature-soil-channel")));
    }

    @Test
    void emptyUpdateNeverRemovesStaticChannels() {
        // a sensor that produces no measurements this poll must not have its static signal/battery channels touched
        handler.updateMeasuredValues(List.of());
        // no dynamic channels were ever created, so nothing changes and no exception is thrown
        assertThat(currentChannelIds(), containsInAnyOrder());
    }
}
