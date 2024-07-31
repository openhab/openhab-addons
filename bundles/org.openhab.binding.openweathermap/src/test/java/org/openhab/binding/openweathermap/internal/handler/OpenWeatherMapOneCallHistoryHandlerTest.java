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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.openweathermap.internal.OpenWeatherMapBindingConstants;
import org.openhab.binding.openweathermap.internal.connection.OpenWeatherMapConnection;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapOneCallHistAPIData;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.State;

import com.google.gson.Gson;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapOneCallHistoryHandlerTest {

    private static final Configuration CONFIG = createConfig(true);
    private static final Configuration BAD_CONFIG = createConfig(false);

    private static Configuration createConfig(boolean returnValid) {
        final Configuration config = new Configuration();
        if (returnValid) {
            config.put(OpenWeatherMapBindingConstants.CONFIG_LOCATION, "51.0435,7.2865");
            config.put(OpenWeatherMapBindingConstants.CONFIG_HISTORY_DAYS, "1");
        }
        return config;
    }

    private static Thing mockThing(boolean withConfiguration) {
        final Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID(OpenWeatherMapBindingConstants.BINDING_ID, "owm-test-thing"));
        when(thing.getConfiguration()).thenReturn(withConfiguration ? CONFIG : BAD_CONFIG);

        final List<Channel> channelList = Arrays.asList(
                mockChannel(thing.getUID(),
                        OpenWeatherMapBindingConstants.CHANNEL_GROUP_ONECALL_HISTORY + "#"
                                + OpenWeatherMapBindingConstants.CHANNEL_WIND_SPEED), //
                mockChannel(thing.getUID(),
                        OpenWeatherMapBindingConstants.CHANNEL_GROUP_ONECALL_HISTORY + "#"
                                + OpenWeatherMapBindingConstants.CHANNEL_DEW_POINT), //
                mockChannel(thing.getUID(), OpenWeatherMapBindingConstants.CHANNEL_GROUP_ONECALL_HISTORY + "#"
                        + OpenWeatherMapBindingConstants.CHANNEL_WIND_DIRECTION) //
        );

        when(thing.getChannels()).thenReturn(channelList);
        return thing;
    }

    private static Channel mockChannel(final ThingUID thingId, final String channelId) {
        final Channel channel = Mockito.mock(Channel.class);
        when(channel.getUID()).thenReturn(new ChannelUID(thingId, channelId));
        when(channel.getKind()).thenReturn(ChannelKind.STATE);

        return channel;
    }

    private static OpenWeatherMapOneCallHistoryHandler createAndInitHandler(final ThingHandlerCallback callback,
            final Thing thing) {
        TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
        final OpenWeatherMapOneCallHistoryHandler handler = spy(
                new OpenWeatherMapOneCallHistoryHandler(thing, timeZoneProvider));

        handler.setCallback(callback);
        handler.initialize();

        return handler;
    }

    private static State getState(final double input, Unit<?> unit) {
        return new QuantityType<>(input, unit);
    }

    private static State getState(final int input, Unit<?> unit) {
        return new QuantityType<>(input, unit);
    }

    @Test
    public void testInvalidConfiguration() {
        final Thing thing = mockThing(false);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final OpenWeatherMapOneCallHistoryHandler handler = createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)
                    && arg.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testResponseMessage() {
        final Thing thing = mockThing(true);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final OpenWeatherMapOneCallHistoryHandler handler = createAndInitHandler(callback, thing);

        when(callback.isChannelLinked(any())).thenReturn(true);

        String content = "";
        try {
            content = Files.readString(
                    Paths.get(System.getProperty("user.dir")
                            + "/src/test/java/org/openhab/binding/openweathermap/internal/handler/example_v3.json"),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        OpenWeatherMapOneCallHistAPIData data = new Gson().fromJson(content, OpenWeatherMapOneCallHistAPIData.class);

        OpenWeatherMapConnection connectionMock = mock(OpenWeatherMapConnection.class);
        when(connectionMock.getOneCallHistAPIData(handler.location, 1)).thenReturn(data);
        handler.updateData(connectionMock);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback, atLeast(2)).statusUpdated(eq(thing),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(),
                            OpenWeatherMapBindingConstants.CHANNEL_GROUP_ONECALL_CURRENT + "#"
                                    + OpenWeatherMapBindingConstants.CHANNEL_WIND_SPEED),
                    getState(1.65, Units.METRE_PER_SECOND));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(),
                            OpenWeatherMapBindingConstants.CHANNEL_GROUP_ONECALL_HISTORY + "#"
                                    + OpenWeatherMapBindingConstants.CHANNEL_DEW_POINT),
                    getState(14.35, SIUnits.CELSIUS));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(),
                            OpenWeatherMapBindingConstants.CHANNEL_GROUP_ONECALL_HISTORY + "#"
                                    + OpenWeatherMapBindingConstants.CHANNEL_WIND_DIRECTION),
                    getState(116, Units.DEGREE_ANGLE));
        } finally {
            handler.dispose();
        }
    }
}
