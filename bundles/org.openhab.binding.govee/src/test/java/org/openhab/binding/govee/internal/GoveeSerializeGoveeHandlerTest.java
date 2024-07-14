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
package org.openhab.binding.govee.internal;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.govee.internal.model.StatusResponse;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;

import com.google.gson.Gson;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class GoveeSerializeGoveeHandlerTest {

    private static final Gson GSON = new Gson();
    private final String invalidValueJsonString = "{\"msg\": {\"cmd\": \"devStatus\", \"data\": {\"onOff\": 0, \"brightness\": 100, \"color\": {\"r\": 1, \"g\": 10, \"b\": 0}, \"colorTemInKelvin\": 9070}}}";

    private static final Configuration CONFIG = createConfig(true);
    private static final Configuration BAD_CONFIG = createConfig(false);

    private static Configuration createConfig(boolean returnValid) {
        final Configuration config = new Configuration();
        if (returnValid) {
            config.put("hostname", "1.2.3.4");
        }
        return config;
    }

    private static Thing mockThing(boolean withConfiguration) {
        final Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID(GoveeBindingConstants.THING_TYPE_LIGHT, "govee-test-thing"));
        when(thing.getConfiguration()).thenReturn(withConfiguration ? CONFIG : BAD_CONFIG);

        final List<Channel> channelList = Arrays.asList(
                mockChannel(thing.getUID(), GoveeBindingConstants.CHANNEL_COLOR), //
                mockChannel(thing.getUID(), GoveeBindingConstants.CHANNEL_COLOR_TEMPERATURE), //
                mockChannel(thing.getUID(), GoveeBindingConstants.CHANNEL_COLOR_TEMPERATURE_ABS));

        when(thing.getChannels()).thenReturn(channelList);
        return thing;
    }

    private static Channel mockChannel(final ThingUID thingId, final String channelId) {
        final Channel channel = Mockito.mock(Channel.class);
        when(channel.getUID()).thenReturn(new ChannelUID(thingId, channelId));
        return channel;
    }

    private static GoveeHandlerMock createAndInitHandler(final ThingHandlerCallback callback, final Thing thing) {
        CommunicationManager communicationManager = mock(CommunicationManager.class);
        final GoveeHandlerMock handler = spy(new GoveeHandlerMock(thing, communicationManager));

        handler.setCallback(callback);
        handler.initialize();

        return handler;
    }

    private static State getState(final int input, Unit<?> unit) {
        return new QuantityType<>(input, unit);
    }

    @Test
    public void testInvalidConfiguration() {
        final Thing thing = mockThing(false);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final GoveeHandlerMock handler = createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)
                    && arg.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testInvalidResponseMessage() {
        final Thing thing = mockThing(true);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final GoveeHandlerMock handler = createAndInitHandler(callback, thing);

        // inject StatusResponseMessage
        StatusResponse statusMessage = GSON.fromJson(invalidValueJsonString, StatusResponse.class);
        handler.updateDeviceState(statusMessage);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));

            verify(callback).stateUpdated(new ChannelUID(thing.getUID(), GoveeBindingConstants.CHANNEL_COLOR),
                    new HSBType(new DecimalType(114), new PercentType(100), new PercentType(0)));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), GoveeBindingConstants.CHANNEL_COLOR_TEMPERATURE),
                    new PercentType(100));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), GoveeBindingConstants.CHANNEL_COLOR_TEMPERATURE_ABS),
                    getState(2000, Units.KELVIN));
        } finally {
            handler.dispose();
        }
    }
}
