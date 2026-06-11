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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BooleanStateCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

/**
 * Test class for ContactStateConverter.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class ContactStateConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private BooleanStateCluster mockCluster;
    @NonNullByDefault({})
    private ContactStateConverter contactConverter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        contactConverter = new ContactStateConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    void testCreateChannels() {
        ChannelGroupUID thingUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = contactConverter.createChannels(thingUID);
        assertEquals(1, channels.size());

        Channel contactChannel = channels.keySet().stream()
                .filter(channel -> channel.getUID().toString().endsWith("#contact-statevalue")).findFirst()
                .orElseThrow();
        assertEquals("Contact", contactChannel.getAcceptedItemType());
    }

    @Test
    void testOnEventWithBooleanValueTrue() throws Exception {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "stateValue";
        message.value = true;
        contactConverter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("contact-statevalue"), eq(OpenClosedType.CLOSED));
    }

    @Test
    void testOnEventWithBooleanValueFalse() throws Exception {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "stateValue";
        message.value = false;
        contactConverter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("contact-statevalue"), eq(OpenClosedType.OPEN));
    }

    @Test
    void testInitState() throws Exception {
        mockCluster.stateValue = true;
        contactConverter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("contact-statevalue"), eq(OpenClosedType.CLOSED));
    }

    @Test
    void testInitStateNull() throws Exception {
        mockCluster.stateValue = null;
        contactConverter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("contact-statevalue"), eq(UnDefType.NULL));
    }
}
