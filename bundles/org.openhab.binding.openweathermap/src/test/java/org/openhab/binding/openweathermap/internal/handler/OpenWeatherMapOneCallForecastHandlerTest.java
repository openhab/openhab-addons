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
package org.openhab.binding.openweathermap.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.openweathermap.internal.OpenWeatherMapBindingConstants.CHANNEL_DEW_POINT;
import static org.openhab.binding.openweathermap.internal.OpenWeatherMapBindingConstants.CHANNEL_GROUP_FORECAST;
import static org.openhab.binding.openweathermap.internal.OpenWeatherMapBindingConstants.CHANNEL_TEMPERATURE;
import static org.openhab.binding.openweathermap.internal.OpenWeatherMapBindingConstants.CONFIG_LOCATION;
import static org.openhab.binding.openweathermap.internal.TestObjectsUtil.mockChannel;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.openweathermap.internal.DataUtil;
import org.openhab.binding.openweathermap.internal.TestObjectsUtil;
import org.openhab.binding.openweathermap.internal.connection.OpenWeatherMapConnection;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapJsonHourlyForecastData;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapOneCallAPIData;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;

/**
 * Unit tests for {@link OpenWeatherMapOneCallForecastHandler} using real API responses
 * (Munich, lat=48.1374, lon=11.5755, {@code units=metric}).
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapOneCallForecastHandlerTest {

    private static final long DT_ONECALL_FIRST = 1_782_849_600L;
    private static final long DT_ONECALL_LAST = 1_783_018_800L;
    private static final long DT_FORECAST5_FIRST_NEW = 1_783_026_000L;
    private static final long DT_FORECAST5_LAST = 1_783_274_400L;

    private static final double TEMP_ONECALL_FIRST = 22.23;
    private static final double TEMP_ONECALL_LAST = 19.04;
    private static final double TEMP_FORECAST5_FIRST_NEW = 18.21;
    private static final double TEMP_FORECAST5_LAST = 20.44;

    private static final double DEW_POINT_ONECALL_FIRST = 18.42;

    private static final int ONECALL_SLOT_COUNT = 48;
    private static final int FORECAST5_NEW_SLOT_COUNT = 24;
    private static final int TOTAL_MERGED_SIZE = ONECALL_SLOT_COUNT + FORECAST5_NEW_SLOT_COUNT;

    private ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

    private static Configuration createConfig() {
        Configuration config = new Configuration();
        config.put(CONFIG_LOCATION, "48.1374,11.5755");
        return config;
    }

    private static Thing mockThing(Configuration configuration, String... forecastChannelIds) {
        Thing thing = TestObjectsUtil.mockThing(configuration);
        ThingUID uid = thing.getUID();
        List<Channel> channels = Arrays.stream(forecastChannelIds)
                .map(id -> mockChannel(uid, CHANNEL_GROUP_FORECAST + "#" + id)).toList();
        when(thing.getChannels()).thenReturn(channels);
        return thing;
    }

    private static OpenWeatherMapOneCallForecastHandler createAndInitHandler(ThingHandlerCallback callback,
            Thing thing) {
        OpenWeatherMapOneCallForecastHandler handler = spy(new OpenWeatherMapOneCallForecastHandler(thing));
        when(callback.isChannelLinked(any())).thenReturn(true);
        handler.setCallback(callback);
        handler.initialize();
        return handler;
    }

    /**
     * Verifies the merge produces 48 OneCall hourly slots followed by the 24 non-overlapping
     * Forecast5 slots (72 entries total).
     */
    @Test
    public void testMergeProducesCorrectTimeSeriesSize() throws IOException {
        // Arrange
        Configuration config = createConfig();
        Thing thing = mockThing(config, CHANNEL_TEMPERATURE);
        OpenWeatherMapOneCallForecastHandler handler = createAndInitHandler(callback, thing);

        OpenWeatherMapOneCallAPIData oneCallData = DataUtil.fromJson("onecall_v3_onecall.json",
                OpenWeatherMapOneCallAPIData.class);
        OpenWeatherMapJsonHourlyForecastData forecast5Data = DataUtil.fromJson("onecall_v3_5day.json",
                OpenWeatherMapJsonHourlyForecastData.class);

        OpenWeatherMapConnection connection = mock(OpenWeatherMapConnection.class);
        when(connection.getOneCallAPIData(any(), eq(true), eq(false), eq(true), eq(true))).thenReturn(oneCallData);
        when(connection.getHourlyForecastData(any(), eq(40))).thenReturn(forecast5Data);

        // Act
        handler.updateData(connection);

        // Assert
        ChannelUID temperatureChannel = new ChannelUID(thing.getUID(),
                CHANNEL_GROUP_FORECAST + "#" + CHANNEL_TEMPERATURE);
        ArgumentCaptor<TimeSeries> tsCaptor = ArgumentCaptor.forClass(TimeSeries.class);
        try {
            verify(callback).statusUpdated(eq(thing), argThat(info -> ThingStatus.ONLINE.equals(info.getStatus())));
            verify(callback).sendTimeSeries(eq(temperatureChannel), tsCaptor.capture());

            TimeSeries timeSeries = tsCaptor.getValue();
            List<TimeSeries.Entry> entries = timeSeries.getStates().toList();

            assertThat("total merged entries", entries, hasSize(TOTAL_MERGED_SIZE));

            assertThat("first entry timestamp", entries.get(0).timestamp(),
                    is(Instant.ofEpochSecond(DT_ONECALL_FIRST)));
            assertThat("first entry temperature", entries.get(0).state(),
                    is(new QuantityType<>(TEMP_ONECALL_FIRST, SIUnits.CELSIUS)));

            assertThat("last OneCall entry timestamp", entries.get(ONECALL_SLOT_COUNT - 1).timestamp(),
                    is(Instant.ofEpochSecond(DT_ONECALL_LAST)));
            assertThat("last OneCall entry temperature", entries.get(ONECALL_SLOT_COUNT - 1).state(),
                    is(new QuantityType<>(TEMP_ONECALL_LAST, SIUnits.CELSIUS)));

            assertThat("first new Forecast5 entry timestamp", entries.get(ONECALL_SLOT_COUNT).timestamp(),
                    is(Instant.ofEpochSecond(DT_FORECAST5_FIRST_NEW)));
            assertThat("first new Forecast5 entry temperature", entries.get(ONECALL_SLOT_COUNT).state(),
                    is(new QuantityType<>(TEMP_FORECAST5_FIRST_NEW, SIUnits.CELSIUS)));

            assertThat("last Forecast5 entry timestamp", entries.get(TOTAL_MERGED_SIZE - 1).timestamp(),
                    is(Instant.ofEpochSecond(DT_FORECAST5_LAST)));
            assertThat("last Forecast5 entry temperature", entries.get(TOTAL_MERGED_SIZE - 1).state(),
                    is(new QuantityType<>(TEMP_FORECAST5_LAST, SIUnits.CELSIUS)));
        } finally {
            handler.dispose();
        }
    }

    /**
     * {@code dew-point} must carry real values for the OneCall portion
     * and {@link UnDefType#UNDEF} for the Forecast5 portion (Forecast5 does not provide dew-point
     * in the channel mapping — see {@code getForecast5State}).
     */
    @Test
    public void testDewPointIsUndefForForecast5Portion() throws IOException {
        // Arrange
        Configuration config = createConfig();
        Thing thing = mockThing(config, CHANNEL_DEW_POINT);
        OpenWeatherMapOneCallForecastHandler handler = createAndInitHandler(callback, thing);

        OpenWeatherMapOneCallAPIData oneCallData = DataUtil.fromJson("onecall_v3_onecall.json",
                OpenWeatherMapOneCallAPIData.class);
        OpenWeatherMapJsonHourlyForecastData forecast5Data = DataUtil.fromJson("onecall_v3_5day.json",
                OpenWeatherMapJsonHourlyForecastData.class);

        OpenWeatherMapConnection connection = mock(OpenWeatherMapConnection.class);
        when(connection.getOneCallAPIData(any(), eq(true), eq(false), eq(true), eq(true))).thenReturn(oneCallData);
        when(connection.getHourlyForecastData(any(), eq(40))).thenReturn(forecast5Data);

        // Act
        handler.updateData(connection);

        // Assert
        ChannelUID dewPointChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_FORECAST + "#" + CHANNEL_DEW_POINT);
        ArgumentCaptor<TimeSeries> tsCaptor = ArgumentCaptor.forClass(TimeSeries.class);
        try {
            verify(callback).sendTimeSeries(eq(dewPointChannel), tsCaptor.capture());
            TimeSeries timeSeries = tsCaptor.getValue();
            List<TimeSeries.Entry> entries = timeSeries.getStates().toList();

            assertThat("total merged dew-point entries", entries, hasSize(TOTAL_MERGED_SIZE));

            assertThat("OneCall dew-point is a QuantityType", entries.get(0).state(),
                    is(instanceOf(QuantityType.class)));
            assertThat("OneCall dew-point value (slot 1)", entries.get(0).state(),
                    is(new QuantityType<>(DEW_POINT_ONECALL_FIRST, SIUnits.CELSIUS)));

            for (int i = ONECALL_SLOT_COUNT; i < TOTAL_MERGED_SIZE; i++) {
                assertThat("Forecast5 dew-point entry " + i + " must be UNDEF", entries.get(i).state(),
                        is(UnDefType.UNDEF));
            }
        } finally {
            handler.dispose();
        }
    }
}
