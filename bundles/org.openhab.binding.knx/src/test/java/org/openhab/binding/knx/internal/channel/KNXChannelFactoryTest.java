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
package org.openhab.binding.knx.internal.channel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openhab.binding.knx.internal.KNXBindingConstants.*;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 *
 * @author Holger Friedrich - Initial Contribution
 *
 */
@NonNullByDefault
class KNXChannelFactoryTest {

    /**
     * This test checks if channels with invalid channelTypeUID lead to the intended exception.
     * Side effect is testing if KNXChannelFactory can be instantiated (this is not the case e.g. when types with
     * duplicate channel types are created)
     */
    @Test
    public void testNullChannelUidFails() {
        Channel channel = Objects.requireNonNull(mock(Channel.class));

        assertThrows(IllegalArgumentException.class, () -> KNXChannelFactory.createKnxChannel(channel));
    }

    @Test
    public void testInvalidChannelUidFails() {
        Channel channel = Objects.requireNonNull(mock(Channel.class));
        when(channel.getChannelTypeUID()).thenReturn(new ChannelTypeUID("a:b:c"));

        assertThrows(IllegalArgumentException.class, () -> KNXChannelFactory.createKnxChannel(channel));
    }

    @ParameterizedTest
    @ValueSource(strings = { CHANNEL_COLOR, CHANNEL_COLOR_CONTROL, CHANNEL_CONTACT, CHANNEL_CONTACT_CONTROL,
            CHANNEL_DATETIME, CHANNEL_DATETIME_CONTROL, CHANNEL_DIMMER, CHANNEL_DIMMER_CONTROL, CHANNEL_NUMBER,
            CHANNEL_NUMBER_CONTROL, CHANNEL_ROLLERSHUTTER, CHANNEL_ROLLERSHUTTER_CONTROL, CHANNEL_STRING,
            CHANNEL_STRING_CONTROL, CHANNEL_SWITCH, CHANNEL_SWITCH_CONTROL })
    public void testSuccess(String channelType) {
        Channel channel = Objects.requireNonNull(mock(Channel.class));
        Configuration configuration = new Configuration(
                Map.of("key1", "5.001:<1/2/3+4/5/6+1/5/6", "key2", "1.001:7/1/9+1/1/2"));
        when(channel.getChannelTypeUID()).thenReturn(new ChannelTypeUID("knx:" + channelType));
        when(channel.getConfiguration()).thenReturn(configuration);
        when(channel.getAcceptedItemType()).thenReturn("none");

        assertNotNull(KNXChannelFactory.createKnxChannel(channel));
    }
}
