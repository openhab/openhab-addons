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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_MOISTURE;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_TEMPERATURE;
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
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
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

    private static final int REMOVAL_THRESHOLD = DynamicChannelReconciler.MISSING_VALUE_REMOVAL_THRESHOLD;

    private @Mock @NonNullByDefault({}) Thing thingMock;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callbackMock;
    private @Mock @NonNullByDefault({}) ChannelTypeRegistry channelTypeRegistryMock;

    private @NonNullByDefault({}) FineOffsetSensorHandler handler;

    private final MeasuredValue moisture = measuredValue("moisture-soil-channel", CHANNEL_TYPE_MOISTURE);
    private final MeasuredValue soilTemp = measuredValue("temperature-soil-channel", CHANNEL_TYPE_TEMPERATURE);

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
    void emptyUpdateOnFreshHandlerIsSafe() {
        // a sensor that produces no measurements must not throw and must leave the channel list empty
        handler.updateMeasuredValues(List.of());
        assertThat(currentChannelIds(), empty());
    }

    @Test
    void managedChannelRemovalPreservesStaticChannels() {
        // Seed the mock thing with a static "signal" channel so that editThing() picks it up from the start.
        Channel staticSignal = ChannelBuilder.create(new ChannelUID(SENSOR_UID, "signal")).build();
        when(thingMock.getChannels()).thenReturn(List.of(staticSignal));

        // Create a managed channel via the first measurement update.
        handler.updateMeasuredValues(List.of(moisture));
        assertThat(currentChannelIds(), containsInAnyOrder("signal", "moisture-soil-channel"));

        // Drive the managed channel past the removal threshold without reporting it.
        for (int i = 0; i < REMOVAL_THRESHOLD; i++) {
            handler.updateMeasuredValues(List.of());
        }

        // The managed channel is gone; the static signal channel is untouched.
        assertThat(currentChannelIds(), not(hasItem("moisture-soil-channel")));
        assertThat(currentChannelIds(), hasItem("signal"));
    }

    @Test
    void firstMeasurementStateIsPostedImmediately() {
        // When a channel is created for the first time, its initial state must be posted right away —
        // not deferred until the next poll.
        handler.updateMeasuredValues(List.of(moisture));

        ChannelUID expectedUID = new ChannelUID(SENSOR_UID, "moisture-soil-channel");
        verify(callbackMock).stateUpdated(expectedUID, new DecimalType(1));
    }
}
