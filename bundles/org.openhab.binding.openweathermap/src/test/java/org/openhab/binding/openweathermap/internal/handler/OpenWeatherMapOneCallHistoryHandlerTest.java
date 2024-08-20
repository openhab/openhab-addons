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
package org.openhab.binding.openweathermap.internal.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.openweathermap.internal.OpenWeatherMapBindingConstants.*;
import static org.openhab.binding.openweathermap.internal.TestObjectsUtil.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.openweathermap.internal.DataUtil;
import org.openhab.binding.openweathermap.internal.TestObjectsUtil;
import org.openhab.binding.openweathermap.internal.connection.OpenWeatherMapConnection;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapOneCallHistAPIData;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapOneCallHistoryHandlerTest {

    private ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

    private static Thing mockThing(Configuration configuration) {
        final Thing thing = TestObjectsUtil.mockThing(configuration);

        final List<Channel> channelList = Arrays.asList(
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_STATION_LOCATION), //
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_TIME_STAMP), //
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_SUNRISE), //
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_SUNSET), //
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_CONDITION), //
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_CONDITION_ID), //
                // CHANNEL_CONDITION_ICON was left out of this test
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_CONDITION_ICON_ID), //
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_TEMPERATURE), //
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_APPARENT_TEMPERATURE), //
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_PRESSURE), //
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_HUMIDITY), //
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_DEW_POINT), //
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_WIND_SPEED), //
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_WIND_DIRECTION), //
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_GUST_SPEED), //
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_CLOUDINESS), //
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_UVINDEX), //
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_RAIN), //
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_SNOW), //
                mockChannel(thing.getUID(), CHANNEL_GROUP_ONECALL_HISTORY + "#" + CHANNEL_VISIBILITY) //
        );

        when(thing.getChannels()).thenReturn(channelList);
        return thing;
    }

    private static OpenWeatherMapOneCallHistoryHandler createAndInitHandler(final ThingHandlerCallback callback,
            final Thing thing) {
        TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
        when(timeZoneProvider.getTimeZone()).thenReturn(ZoneId.of("UTC"));
        final OpenWeatherMapOneCallHistoryHandler handler = spy(
                new OpenWeatherMapOneCallHistoryHandler(thing, timeZoneProvider));

        when(callback.isChannelLinked(any())).thenReturn(true);

        handler.setCallback(callback);
        handler.initialize();

        return handler;
    }

    private static void assertGroupChannelStateSet(ThingHandlerCallback callback, ThingUID uid, String channel,
            State state) {
        verify(callback).stateUpdated(new ChannelUID(uid, CHANNEL_GROUP_ONECALL_HISTORY + "#" + channel), state);
    }

    @Test
    public void testInvalidConfiguration() {
        // Arrange
        final Configuration configuration = createConfig(false, null);
        final Thing thing = mockThing(configuration);
        final OpenWeatherMapOneCallHistoryHandler handler = createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)
                    && arg.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testCurrentWithResponseMessageV30() throws IOException {
        // Arrange
        final Configuration configuration = createConfig(true, "3.0");
        final Thing thing = mockThing(configuration);
        final OpenWeatherMapOneCallHistoryHandler handler = createAndInitHandler(callback, thing);

        OpenWeatherMapOneCallHistAPIData data = DataUtil.fromJson("history_v3_0.json",
                OpenWeatherMapOneCallHistAPIData.class);
        OpenWeatherMapConnection connectionMock = mock(OpenWeatherMapConnection.class);
        when(connectionMock.getOneCallHistAPIData(handler.location,
                ((BigDecimal) configuration.get(CONFIG_HISTORY_DAYS)).intValue())).thenReturn(data);

        // Act
        handler.updateData(connectionMock);

        // Assert
        ThingUID uid = thing.getUID();
        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback, atLeast(2)).statusUpdated(eq(thing),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

            assertGroupChannelStateSet(callback, uid, CHANNEL_STATION_LOCATION, new PointType("52.2297,21.0122"));
            assertGroupChannelStateSet(callback, uid, CHANNEL_TIME_STAMP,
                    new DateTimeType("2022-02-26T15:22:56.000+0000"));
            assertGroupChannelStateSet(callback, uid, CHANNEL_SUNRISE,
                    new DateTimeType("2022-02-26T05:29:21.000+0000"));
            assertGroupChannelStateSet(callback, uid, CHANNEL_SUNSET, new DateTimeType("2022-02-26T16:08:47.000+0000"));
            assertGroupChannelStateSet(callback, uid, CHANNEL_CONDITION, new StringType("clear sky"));
            assertGroupChannelStateSet(callback, uid, CHANNEL_CONDITION_ID, new StringType("800"));
            // CHANNEL_CONDITION_ICON was left out of this test
            assertGroupChannelStateSet(callback, uid, CHANNEL_CONDITION_ICON_ID, new StringType("01d"));
            assertGroupChannelStateSet(callback, uid, CHANNEL_TEMPERATURE, getState(279.13, SIUnits.CELSIUS));
            assertGroupChannelStateSet(callback, uid, CHANNEL_APPARENT_TEMPERATURE, getState(276.44, SIUnits.CELSIUS));
            assertGroupChannelStateSet(callback, uid, CHANNEL_PRESSURE, getState(102900, SIUnits.PASCAL));
            assertGroupChannelStateSet(callback, uid, CHANNEL_HUMIDITY, getState(64, Units.PERCENT));
            assertGroupChannelStateSet(callback, uid, CHANNEL_DEW_POINT, getState(272.88, SIUnits.CELSIUS));
            assertGroupChannelStateSet(callback, uid, CHANNEL_WIND_SPEED, getState(3.6, Units.METRE_PER_SECOND));
            assertGroupChannelStateSet(callback, uid, CHANNEL_WIND_DIRECTION, getState(340, Units.DEGREE_ANGLE));
            assertGroupChannelStateSet(callback, uid, CHANNEL_GUST_SPEED, getState(0, Units.METRE_PER_SECOND));
            assertGroupChannelStateSet(callback, uid, CHANNEL_CLOUDINESS, getState(0, Units.PERCENT));
            assertGroupChannelStateSet(callback, uid, CHANNEL_UVINDEX, new DecimalType(0.06));
            assertGroupChannelStateSet(callback, uid, CHANNEL_RAIN, getState(0.0, SIUnits.METRE));
            assertGroupChannelStateSet(callback, uid, CHANNEL_SNOW, getState(0.0, SIUnits.METRE));
            assertGroupChannelStateSet(callback, uid, CHANNEL_VISIBILITY, getState(10000, SIUnits.METRE));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testCurrentWithResponseMessageV25() throws IOException {
        // Arrange
        final Configuration configuration = createConfig(true, "3.0");
        final Thing thing = mockThing(configuration);
        final OpenWeatherMapOneCallHistoryHandler handler = createAndInitHandler(callback, thing);

        OpenWeatherMapOneCallHistAPIData data = DataUtil.fromJson("history_v2_5.json",
                OpenWeatherMapOneCallHistAPIData.class);
        OpenWeatherMapConnection connectionMock = mock(OpenWeatherMapConnection.class);
        when(connectionMock.getOneCallHistAPIData(handler.location,
                ((BigDecimal) configuration.get(CONFIG_HISTORY_DAYS)).intValue())).thenReturn(data);

        // Act
        handler.updateData(connectionMock);

        // Assert
        ThingUID uid = thing.getUID();
        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback, atLeast(2)).statusUpdated(eq(thing),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

            assertGroupChannelStateSet(callback, uid, CHANNEL_STATION_LOCATION, new PointType("60.99,30.9"));
            assertGroupChannelStateSet(callback, uid, CHANNEL_TIME_STAMP,
                    new DateTimeType("2020-04-09T21:33:47.000+0000"));
            assertGroupChannelStateSet(callback, uid, CHANNEL_SUNRISE,
                    new DateTimeType("2020-04-10T02:57:04.000+0000"));
            assertGroupChannelStateSet(callback, uid, CHANNEL_SUNSET, new DateTimeType("2020-04-10T17:04:57.000+0000"));
            assertGroupChannelStateSet(callback, uid, CHANNEL_CONDITION, new StringType("clear sky"));
            assertGroupChannelStateSet(callback, uid, CHANNEL_CONDITION_ID, new StringType("800"));
            // CHANNEL_CONDITION_ICON was left out of this test
            assertGroupChannelStateSet(callback, uid, CHANNEL_CONDITION_ICON_ID, new StringType("01n"));
            assertGroupChannelStateSet(callback, uid, CHANNEL_TEMPERATURE, getState(274.31, SIUnits.CELSIUS));
            assertGroupChannelStateSet(callback, uid, CHANNEL_APPARENT_TEMPERATURE, getState(269.79, SIUnits.CELSIUS));
            assertGroupChannelStateSet(callback, uid, CHANNEL_PRESSURE, getState(100600, SIUnits.PASCAL));
            assertGroupChannelStateSet(callback, uid, CHANNEL_HUMIDITY, getState(72, Units.PERCENT));
            assertGroupChannelStateSet(callback, uid, CHANNEL_DEW_POINT, getState(270.21, SIUnits.CELSIUS));
            assertGroupChannelStateSet(callback, uid, CHANNEL_WIND_SPEED, getState(3, Units.METRE_PER_SECOND));
            assertGroupChannelStateSet(callback, uid, CHANNEL_WIND_DIRECTION, getState(260, Units.DEGREE_ANGLE));
            assertGroupChannelStateSet(callback, uid, CHANNEL_GUST_SPEED, getState(0, Units.METRE_PER_SECOND));
            assertGroupChannelStateSet(callback, uid, CHANNEL_CLOUDINESS, getState(0, Units.PERCENT));
            assertGroupChannelStateSet(callback, uid, CHANNEL_UVINDEX, new DecimalType(0.0));
            assertGroupChannelStateSet(callback, uid, CHANNEL_RAIN, getState(0.0, SIUnits.METRE));
            assertGroupChannelStateSet(callback, uid, CHANNEL_SNOW, getState(0.0, SIUnits.METRE));
            assertGroupChannelStateSet(callback, uid, CHANNEL_VISIBILITY, getState(10000, SIUnits.METRE));
        } finally {
            handler.dispose();
        }
    }
}
