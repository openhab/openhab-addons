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
 * Unit tests for {@link OpenWeatherMapOneCallV3TimeSeriesHandler} using real API responses.
 *
 * <p>
 * Fixture data captured live from the OpenWeatherMap API (Munich, lat=48.1374, lon=11.5755),
 * with temperatures converted to °C ({@code units=metric}):
 *
 * <ul>
 * <li>{@code onecall_v3_onecall.json} — One Call API 3.0 response with 48 hourly slots,
 * first slot dt={@value #DT_ONECALL_FIRST}, last slot dt={@value #DT_ONECALL_LAST}.</li>
 * <li>{@code onecall_v3_5day.json} — Forecast5 API response with 40 three-hourly slots,
 * first slot dt=1782853200. Slots with dt ≤ {@value #DT_ONECALL_LAST} overlap with OneCall
 * and are filtered (16 slots); the remaining 24 slots are appended after the cutoff, starting
 * at dt={@value #DT_FORECAST5_FIRST_NEW}.</li>
 * </ul>
 *
 * <p>
 * Merge result: {@value #ONECALL_SLOT_COUNT} OneCall slots + {@value #FORECAST5_NEW_SLOT_COUNT}
 * new Forecast5 slots = {@value #TOTAL_MERGED_SIZE} TimeSeries entries per channel.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapOneCallV3TimeSeriesHandlerTest {

    // -------------------------------------------------------------------------
    // Fixture constants derived from the real API responses
    // -------------------------------------------------------------------------

    /** dt of the first OneCall hourly slot. */
    private static final long DT_ONECALL_FIRST = 1_782_849_600L;
    /** dt of the last OneCall hourly slot (cutoff for Forecast5 filtering). */
    private static final long DT_ONECALL_LAST = 1_783_018_800L;
    /** dt of the first Forecast5 slot strictly after the OneCall cutoff. */
    private static final long DT_FORECAST5_FIRST_NEW = 1_783_026_000L;
    /** dt of the last Forecast5 slot. */
    private static final long DT_FORECAST5_LAST = 1_783_274_400L;

    /** Temperature of OneCall hourly slot 1 (dt=1782849600), in °C (metric). */
    private static final double TEMP_ONECALL_FIRST = 22.23;
    /** Temperature of OneCall last slot (dt=1783018800), in °C (metric). */
    private static final double TEMP_ONECALL_LAST = 19.04;
    /** Temperature of first new Forecast5 slot (dt=1783026000), in °C (metric). */
    private static final double TEMP_FORECAST5_FIRST_NEW = 18.21;
    /** Temperature of last Forecast5 slot (dt=1783274400), in °C (metric). */
    private static final double TEMP_FORECAST5_LAST = 20.44;

    /** Dew-point of OneCall hourly slot 1, in °C (metric). Not provided by Forecast5. */
    private static final double DEW_POINT_ONECALL_FIRST = 18.42;

    /** Number of hourly slots in the OneCall fixture. */
    private static final int ONECALL_SLOT_COUNT = 48;
    /** Number of Forecast5 slots whose dt > DT_ONECALL_LAST (i.e. new, non-overlapping). */
    private static final int FORECAST5_NEW_SLOT_COUNT = 24;
    /** Total expected TimeSeries size after merge. */
    private static final int TOTAL_MERGED_SIZE = ONECALL_SLOT_COUNT + FORECAST5_NEW_SLOT_COUNT;

    private ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

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

    private static OpenWeatherMapOneCallV3TimeSeriesHandler createAndInitHandler(ThingHandlerCallback callback,
            Thing thing) {
        OpenWeatherMapOneCallV3TimeSeriesHandler handler = spy(new OpenWeatherMapOneCallV3TimeSeriesHandler(thing));
        when(callback.isChannelLinked(any())).thenReturn(true);
        handler.setCallback(callback);
        handler.initialize();
        return handler;
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    /**
     * Verifies that the merge produces the correct total number of TimeSeries entries:
     * 48 OneCall hourly slots followed by the 24 non-overlapping Forecast5 slots (72 entries total).
     *
     * <p>
     * Verified values (from real API fixtures, {@code units=metric} → °C):
     * <ul>
     * <li>Entry 0: OneCall slot 1, dt=1782849600, temp=22.23 °C</li>
     * <li>Entry 47: OneCall last slot, dt=1783018800, temp=19.04 °C</li>
     * <li>Entry 48: first new Forecast5 slot, dt=1783026000, temp=18.21 °C</li>
     * <li>Entry 71: last Forecast5 slot, dt=1783274400, temp=20.44 °C</li>
     * </ul>
     */
    @Test
    public void testMergeProducesCorrectTimeSeriesSize() throws IOException {
        // Arrange
        Configuration config = createConfig();
        Thing thing = mockThing(config, CHANNEL_TEMPERATURE);
        OpenWeatherMapOneCallV3TimeSeriesHandler handler = createAndInitHandler(callback, thing);

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

            // OneCall part: first entry
            assertThat("first entry timestamp", entries.get(0).timestamp(),
                    is(Instant.ofEpochSecond(DT_ONECALL_FIRST)));
            assertThat("first entry temperature", entries.get(0).state(),
                    is(new QuantityType<>(TEMP_ONECALL_FIRST, SIUnits.CELSIUS)));

            // OneCall part: last entry (index 47)
            assertThat("last OneCall entry timestamp", entries.get(ONECALL_SLOT_COUNT - 1).timestamp(),
                    is(Instant.ofEpochSecond(DT_ONECALL_LAST)));
            assertThat("last OneCall entry temperature", entries.get(ONECALL_SLOT_COUNT - 1).state(),
                    is(new QuantityType<>(TEMP_ONECALL_LAST, SIUnits.CELSIUS)));

            // Forecast5 part: first new entry (index 48)
            assertThat("first new Forecast5 entry timestamp", entries.get(ONECALL_SLOT_COUNT).timestamp(),
                    is(Instant.ofEpochSecond(DT_FORECAST5_FIRST_NEW)));
            assertThat("first new Forecast5 entry temperature", entries.get(ONECALL_SLOT_COUNT).state(),
                    is(new QuantityType<>(TEMP_FORECAST5_FIRST_NEW, SIUnits.CELSIUS)));

            // Forecast5 part: last entry (index 71)
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
        OpenWeatherMapOneCallV3TimeSeriesHandler handler = createAndInitHandler(callback, thing);

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

            // OneCall slot 1: real dew_point value from fixture
            assertThat("OneCall dew-point is a QuantityType", entries.get(0).state(),
                    is(instanceOf(QuantityType.class)));
            assertThat("OneCall dew-point value (slot 1)", entries.get(0).state(),
                    is(new QuantityType<>(DEW_POINT_ONECALL_FIRST, SIUnits.CELSIUS)));

            // All Forecast5 entries (indices 48–71) must be UNDEF
            for (int i = ONECALL_SLOT_COUNT; i < TOTAL_MERGED_SIZE; i++) {
                assertThat("Forecast5 dew-point entry " + i + " must be UNDEF", entries.get(i).state(),
                        is(UnDefType.UNDEF));
            }
        } finally {
            handler.dispose();
        }
    }
}
