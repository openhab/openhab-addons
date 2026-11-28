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
package org.openhab.binding.knx.internal.channel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.KNXBindingConstants;
import org.openhab.binding.knx.internal.dpt.DPTUtil;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Type;

import io.calimero.GroupAddress;
import io.calimero.KNXException;
import io.calimero.dptxlator.DPT;
import io.calimero.dptxlator.TranslatorTypes;
import io.calimero.dptxlator.TranslatorTypes.MainType;

/**
 * Checks the trigger channel class: the factory knows it, and the base class's check of a configured data type
 * against the accepted types holds for every data type the binding maps.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class TypeTelegramTriggerTest {

    private static Channel channel(String ga) {
        Channel channel = mock(Channel.class);
        when(channel.getChannelTypeUID())
                .thenReturn(new ChannelTypeUID("knx", KNXBindingConstants.CHANNEL_TELEGRAM_TRIGGER));
        when(channel.getUID()).thenReturn(new ChannelUID("knx:device:bridge:thing:trigger"));
        when(channel.getConfiguration()).thenReturn(new Configuration(Map.of("ga", ga)));
        return channel;
    }

    @Test
    void theFactoryMakesATriggerChannelThatIsNoControlChannel() {
        KNXChannel knxChannel = KNXChannelFactory.createKnxChannel(channel("9.001:3/1/10+3/1/11"));

        assertTrue(knxChannel.isTrigger());
        assertFalse(knxChannel.isControl());
        assertEquals(List.of(new GroupAddress(3, 1, 10), new GroupAddress(3, 1, 11)),
                knxChannel.getAllGroupAddresses());
    }

    @Test
    void otherChannelTypesAreNoTriggerChannels() {
        Channel channel = mock(Channel.class);
        when(channel.getChannelTypeUID()).thenReturn(new ChannelTypeUID("knx", "switch"));
        when(channel.getUID()).thenReturn(new ChannelUID("knx:device:bridge:thing:light"));
        when(channel.getConfiguration()).thenReturn(new Configuration(Map.of("ga", "3/1/10")));

        assertFalse(KNXChannelFactory.createKnxChannel(channel).isTrigger());
    }

    @Test
    void theTypeIsKnownToTheFactoryAndNotAControlChannelType() {
        assertTrue(TypeTelegramTrigger.SUPPORTED_CHANNEL_TYPES.contains(KNXBindingConstants.CHANNEL_TELEGRAM_TRIGGER));
        assertFalse(KNXBindingConstants.CONTROL_CHANNEL_TYPES.contains(KNXBindingConstants.CHANNEL_TELEGRAM_TRIGGER));
    }

    @Test
    void withoutADataTypeTheDefaultIsEmpty() {
        KNXChannel knxChannel = KNXChannelFactory.createKnxChannel(channel("3/1/10"));

        assertEquals("", Objects.requireNonNull(knxChannel.getListenSpec(new GroupAddress(3, 1, 10))).getDPT());
    }

    @Test
    void theBaseConstructorGetsAFirstEntryThatIsNoSpecialType() {
        assertFalse(TypeTelegramTrigger.ACCEPTED_TYPES.isEmpty());
        assertEquals(Type.class, KNXChannelFactory.createKnxChannel(channel("9.001:3/1/10")).preferredType());
    }

    /** The base class warns when the configured data type allows none of the accepted types. */
    @Test
    void everyDataTypeTheBindingMapsAllowsAnAcceptedType() throws KNXException {
        List<String> dpts = new ArrayList<>();
        for (MainType main : TranslatorTypes.getAllMainTypes().values()) {
            for (DPT dpt : main.getSubTypes().values()) {
                dpts.add(dpt.getID());
            }
        }
        dpts.add("0.000");
        assertTrue(dpts.size() > 300, "the library has many data types, found " + dpts.size());

        int mapped = 0;
        for (String dpt : dpts) {
            Set<Class<? extends Type>> allowed = DPTUtil.getAllowedTypes(dpt);
            if (allowed.isEmpty()) {
                continue;
            }
            mapped++;
            assertTrue(TypeTelegramTrigger.ACCEPTED_TYPES.stream().anyMatch(allowed::contains),
                    dpt + " allows " + allowed + ", none of them accepted");
        }
        assertTrue(mapped > 200, "the binding maps many data types, found " + mapped);
    }
}
