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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.openweathermap.internal.DataUtil;
import org.openhab.binding.openweathermap.internal.OpenWeatherMapBindingConstants;
import org.openhab.binding.openweathermap.internal.TestObjectsUtil;
import org.openhab.binding.openweathermap.internal.connection.OpenWeatherMapConnection;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapOneCallHistAPIData;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapOneCallHistoryHandlerTest {

    private ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

    private static Thing mockThing(Configuration configuration) {
        final Thing thing = TestObjectsUtil.mockThing(configuration);

        final List<Channel> channelList = Arrays.asList(
                TestObjectsUtil.mockChannel(thing.getUID(),
                        OpenWeatherMapBindingConstants.CHANNEL_GROUP_ONECALL_HISTORY + "#"
                                + OpenWeatherMapBindingConstants.CHANNEL_WIND_SPEED), //
                TestObjectsUtil.mockChannel(thing.getUID(),
                        OpenWeatherMapBindingConstants.CHANNEL_GROUP_ONECALL_HISTORY + "#"
                                + OpenWeatherMapBindingConstants.CHANNEL_DEW_POINT), //
                TestObjectsUtil.mockChannel(thing.getUID(), OpenWeatherMapBindingConstants.CHANNEL_GROUP_ONECALL_HISTORY
                        + "#" + OpenWeatherMapBindingConstants.CHANNEL_WIND_DIRECTION) //
        );

        when(thing.getChannels()).thenReturn(channelList);
        return thing;
    }

    private static OpenWeatherMapOneCallHistoryHandler createAndInitHandler(final ThingHandlerCallback callback,
            final Thing thing) {
        TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
        final OpenWeatherMapOneCallHistoryHandler handler = spy(
                new OpenWeatherMapOneCallHistoryHandler(thing, timeZoneProvider));

        when(callback.isChannelLinked(any())).thenReturn(true);

        handler.setCallback(callback);
        handler.initialize();

        return handler;
    }

    @Test
    public void testInvalidConfiguration() {
        // Arrange
        final Configuration configuration = TestObjectsUtil.createConfig(false, null);
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
    public void testResponseMessageV30() {
        // Arrange
        final Configuration configuration = TestObjectsUtil.createConfig(true, "3.0");
        final Thing thing = mockThing(configuration);
        final OpenWeatherMapOneCallHistoryHandler handler = createAndInitHandler(callback, thing);

        OpenWeatherMapOneCallHistAPIData data = null;
        try {
            data = DataUtil.fromJson("history_v3_0.json", OpenWeatherMapOneCallHistAPIData.class);
        } catch (IOException e) {
            // ignore
        }

        OpenWeatherMapConnection connectionMock = mock(OpenWeatherMapConnection.class);
        when(connectionMock.getOneCallHistAPIData(handler.location,
                ((BigDecimal) configuration.get(OpenWeatherMapBindingConstants.CONFIG_HISTORY_DAYS)).intValue()))
                .thenReturn(data);

        // Act
        handler.updateData(connectionMock);

        // Assert
        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback, atLeast(2)).statusUpdated(eq(thing),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(),
                            OpenWeatherMapBindingConstants.CHANNEL_GROUP_ONECALL_HISTORY + "#"
                                    + OpenWeatherMapBindingConstants.CHANNEL_WIND_SPEED),
                    TestObjectsUtil.getState(3.6, Units.METRE_PER_SECOND));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(),
                            OpenWeatherMapBindingConstants.CHANNEL_GROUP_ONECALL_HISTORY + "#"
                                    + OpenWeatherMapBindingConstants.CHANNEL_DEW_POINT),
                    TestObjectsUtil.getState(272.88, SIUnits.CELSIUS));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(),
                            OpenWeatherMapBindingConstants.CHANNEL_GROUP_ONECALL_HISTORY + "#"
                                    + OpenWeatherMapBindingConstants.CHANNEL_WIND_DIRECTION),
                    TestObjectsUtil.getState(340, Units.DEGREE_ANGLE));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testResponseMessageV25() {
        // Arrange
        final Configuration configuration = TestObjectsUtil.createConfig(true, "3.0");
        final Thing thing = mockThing(configuration);
        final OpenWeatherMapOneCallHistoryHandler handler = createAndInitHandler(callback, thing);

        OpenWeatherMapOneCallHistAPIData data = null;
        try {
            data = DataUtil.fromJson("history_v2_5.json", OpenWeatherMapOneCallHistAPIData.class);
        } catch (IOException e) {
            // ignore
        }

        OpenWeatherMapConnection connectionMock = mock(OpenWeatherMapConnection.class);
        when(connectionMock.getOneCallHistAPIData(handler.location,
                ((BigDecimal) configuration.get(OpenWeatherMapBindingConstants.CONFIG_HISTORY_DAYS)).intValue()))
                .thenReturn(data);

        // Act
        handler.updateData(connectionMock);

        // Assert
        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback, atLeast(2)).statusUpdated(eq(thing),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(),
                            OpenWeatherMapBindingConstants.CHANNEL_GROUP_ONECALL_HISTORY + "#"
                                    + OpenWeatherMapBindingConstants.CHANNEL_WIND_SPEED),
                    TestObjectsUtil.getState(3, Units.METRE_PER_SECOND));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(),
                            OpenWeatherMapBindingConstants.CHANNEL_GROUP_ONECALL_HISTORY + "#"
                                    + OpenWeatherMapBindingConstants.CHANNEL_DEW_POINT),
                    TestObjectsUtil.getState(270.21, SIUnits.CELSIUS));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(),
                            OpenWeatherMapBindingConstants.CHANNEL_GROUP_ONECALL_HISTORY + "#"
                                    + OpenWeatherMapBindingConstants.CHANNEL_WIND_DIRECTION),
                    TestObjectsUtil.getState(260, Units.DEGREE_ANGLE));
        } finally {
            handler.dispose();
        }
    }
}
