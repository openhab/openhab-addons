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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.knx.internal.client.KNXClient;
import org.openhab.binding.knx.internal.client.OutboundSpec;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Type;

import io.calimero.GroupAddress;
import io.calimero.datapoint.Datapoint;

/**
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class KNXActionsTest {

    private KNXClient client = mock(KNXClient.class);
    private KNXActions actions = new KNXActions();

    @BeforeEach
    void setUp() {
        client = mock(KNXClient.class);
        when(client.isConnected()).thenReturn(true);
        KNXBridgeBaseThingHandler handler = mock(KNXBridgeBaseThingHandler.class);
        when(handler.getClient()).thenReturn(client);
        actions = new KNXActions();
        actions.setThingHandler(handler);
    }

    @Test
    void parseValuePicksTheTypeTheDptAccepts() {
        assertEquals(OnOffType.ON, KNXActions.parseValue("1.001", "ON"));
        assertEquals(UpDownType.UP, KNXActions.parseValue("1.008", "UP"));
        assertEquals(DecimalType.class, typeOf("5.001", "0.5"));
        assertEquals(PercentType.class, typeOf("251.600", "50"));
        assertEquals(new DecimalType(21.5), KNXActions.parseValue("9.001", "21.5"));
        assertEquals(new QuantityType<>("21.5 °C"), KNXActions.parseValue("9.001", "21.5 °C"));
        assertNull(KNXActions.parseValue("1.001", "21.5"));
        assertNull(KNXActions.parseValue("9.001", "ON"));
        assertNull(KNXActions.parseValue("6.010", "abc"));
        assertNull(KNXActions.parseValue("not-a-dpt", "ON"));
    }

    private static @Nullable Class<?> typeOf(String dpt, String value) {
        Type type = KNXActions.parseValue(dpt, value);
        return type == null ? null : type.getClass();
    }

    @Test
    void writeGroupSendsToTheGroupAddress() throws Exception {
        assertTrue(actions.writeGroup("1/2/3", "9.001", "21.5 °C"));

        ArgumentCaptor<OutboundSpec> spec = ArgumentCaptor.forClass(OutboundSpec.class);
        verify(client).writeToKNX(spec.capture());
        assertEquals(new GroupAddress("1/2/3"), spec.getValue().getGroupAddress());
        assertEquals("9.001", spec.getValue().getDPT());
        assertEquals(new QuantityType<>("21.5 °C"), spec.getValue().getValue());
    }

    @Test
    void writeGroupRejectsBadInput() throws Exception {
        assertFalse(actions.writeGroup("1/2/3", "1.001", "21.5"));
        assertFalse(actions.writeGroup("not/a/ga", "1.001", "ON"));
        verify(client, never()).writeToKNX(any());
    }

    @Test
    void readGroupQueuesARead() throws Exception {
        assertTrue(actions.readGroup("1/2/4", "9.001"));

        ArgumentCaptor<Datapoint> dp = ArgumentCaptor.forClass(Datapoint.class);
        verify(client).readDatapoint(dp.capture());
        assertEquals(new GroupAddress("1/2/4"), dp.getValue().getMainAddress());
        assertEquals("9.001", dp.getValue().dptId().toString());
    }

    @Test
    void readGroupRejectsUnknownDpt() {
        assertFalse(actions.readGroup("1/2/4", "not-a-dpt"));
        assertFalse(actions.readGroup("1/2/4", "9"));
        verify(client, never()).readDatapoint(any());
    }

    @Test
    void nothingIsSentWhileDisconnected() throws Exception {
        when(client.isConnected()).thenReturn(false);

        assertFalse(actions.writeGroup("1/2/3", "1.001", "ON"));
        assertFalse(actions.readGroup("1/2/3", "1.001"));
        verify(client, never()).writeToKNX(any());
        verify(client, never()).readDatapoint(any());
    }
}
