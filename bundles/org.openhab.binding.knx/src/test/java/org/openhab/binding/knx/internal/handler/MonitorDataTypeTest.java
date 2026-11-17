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
package org.openhab.binding.knx.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.channel.KNXChannel;
import org.openhab.binding.knx.internal.channel.KNXChannelFactory;
import org.openhab.binding.knx.internal.monitor.DataType;
import org.openhab.binding.knx.internal.monitor.ProjectLookup;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.ChannelTypeUID;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Checks where the group monitor learns the data type of a group address from.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class MonitorDataTypeTest {
    private static final GroupAddress TEMPERATURE = new GroupAddress(3, 1, 10);
    private static final GroupAddress LIGHT = new GroupAddress(3, 1, 11);
    private static final GroupAddress LIGHT_STATUS = new GroupAddress(3, 1, 12);

    private static KNXChannel channel(String type, String id, String ga) {
        Channel channel = mock(Channel.class);
        when(channel.getChannelTypeUID()).thenReturn(new ChannelTypeUID("knx", type));
        when(channel.getUID()).thenReturn(new ChannelUID("knx:device:bridge:thing:" + id));
        when(channel.getConfiguration()).thenReturn(new Configuration(Map.of("ga", ga)));
        return KNXChannelFactory.createKnxChannel(channel);
    }

    @Test
    void theChannelsTypeIsUsed() {
        List<KNXChannel> channels = List.of(channel("number", "temperature", "9.001:3/1/10"),
                channel("switch", "light", "3/1/11+<3/1/12"));

        assertEquals(new DataType("9.001", DecimalType.class),
                DeviceThingHandler.monitorDataType(channels, TEMPERATURE));
        assertEquals(new DataType("1.001", OnOffType.class), DeviceThingHandler.monitorDataType(channels, LIGHT));
        assertEquals(new DataType("1.001", OnOffType.class),
                DeviceThingHandler.monitorDataType(channels, LIGHT_STATUS));
    }

    @Test
    void anAddressNoChannelUsesHasNoType() {
        List<KNXChannel> channels = List.of(channel("number", "temperature", "9.001:3/1/10"));

        assertNull(DeviceThingHandler.monitorDataType(channels, LIGHT));
    }

    @Test
    void aChannelWithATypeTheBindingDoesNotSupportIsSkipped() {
        List<KNXChannel> channels = List.of(channel("number", "odd", "999.999:3/1/10"),
                channel("number", "temperature", "9.001:3/1/10"));

        assertEquals(new DataType("9.001", DecimalType.class),
                DeviceThingHandler.monitorDataType(channels, TEMPERATURE));
    }

    @Test
    void aListenerThatSaysNothingHasNoType() {
        GroupAddressListener listener = mock(GroupAddressListener.class, CALLS_REAL_METHODS);

        assertNull(listener.monitorDataType(TEMPERATURE));
    }

    @Test
    void theProjectSeamAnswersNothingForNow() {
        assertNull(ProjectLookup.NONE.groupAddressName(TEMPERATURE));
        assertNull(ProjectLookup.NONE.dataType(TEMPERATURE));
        assertNull(ProjectLookup.NONE.deviceName(new IndividualAddress(1, 1, 1)));
    }
}
