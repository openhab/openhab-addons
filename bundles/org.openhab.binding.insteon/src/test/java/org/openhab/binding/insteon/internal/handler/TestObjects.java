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
package org.openhab.binding.insteon.internal.handler;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.mockito.Mockito;
import org.openhab.binding.insteon.internal.InsteonBindingLegacyConstants;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;

/**
 * TestObjects used for the different unit tests in this binding
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class TestObjects {

    private static Configuration createBridgeConfig() {
        final Configuration config = new Configuration();
        config.put("port", "1234");
        config.put("devicePollIntervalSeconds", "2");
        return config;
    }

    static Bridge mockBridge(ThingTypeUID uid, String id) {
        final Bridge thing = mock(Bridge.class);
        when(thing.getUID()).thenReturn(new ThingUID(uid, id));

        when(thing.getConfiguration()).thenReturn(createBridgeConfig());
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);

        return thing;
    }

    private static Configuration createThingConfig() {
        final Configuration config = new Configuration();
        config.put("address", "12.34.56");
        config.put("productKey", "F00.00.01");
        return config;
    }

    static Thing mockThing(ThingTypeUID uid, String id) {
        final Thing thing = mock(Thing.class);

        when(thing.getUID()).thenReturn(new ThingUID(uid, id));
        when(thing.getConfiguration()).thenReturn(createThingConfig());

        final List<Channel> channelList = Arrays
                .asList(mockChannel(thing.getUID(), InsteonBindingLegacyConstants.LIGHT_DIMMER));

        when(thing.getChannels()).thenReturn(channelList);
        return thing;
    }

    static Channel mockChannel(final ThingUID thingId, final String channelId) {
        final Channel channel = Mockito.mock(Channel.class);
        when(channel.getUID()).thenReturn(new ChannelUID(thingId, channelId));
        return channel;
    }

    static State getState(final int input) {
        return new DecimalType(input);
    }

    static State getState(final int input, Unit<?> unit) {
        return new QuantityType<>(input, unit);
    }

    static State getState(final double input, Unit<?> unit) {
        return new QuantityType<>(input, unit);
    }
}
