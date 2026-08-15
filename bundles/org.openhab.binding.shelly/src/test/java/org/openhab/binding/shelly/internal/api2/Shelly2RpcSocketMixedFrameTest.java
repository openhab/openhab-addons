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
package org.openhab.binding.shelly.internal.api2;

import static org.mockito.Mockito.*;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;

/**
 * Verifies how a single {@code NotifyEvent} frame is routed to the hub handler.
 *
 * <p>
 * {@code onNotifyEvent()} iterates the whole frame itself, so the frame has to be forwarded exactly once no
 * matter how many regular events it carries - forwarding per event would make the hub process each of them
 * repeatedly. Mixed frames, carrying a third-party {@code ble.scan_result} next to regular events, only became
 * parseable at all with the polymorphic {@code data} handling, which is why they are pinned here.
 * </p>
 *
 * @author Martin Littkovsky - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault({})
class Shelly2RpcSocketMixedFrameTest {

    private @Mock ShellyThingTable thingTable;
    private @Mock WebSocketClient webSocketClient;
    private @Mock ScheduledExecutorService scheduler;
    private @Mock Shelly2RpctInterface handler;
    private @Mock Session session;

    private Shelly2RpcSocket socket;

    @BeforeEach
    void setUp() {
        socket = new Shelly2RpcSocket(thingTable, true, webSocketClient, scheduler);
        socket.addMessageHandler(handler);
    }

    private static String frame(String events) {
        return "{\"src\":\"shellyplusht-test\",\"dst\":\"ohshelly-test-1\",\"method\":\"NotifyEvent\","
                + "\"params\":{\"ts\":1700000000.0,\"events\":[" + events + "]}}";
    }

    private static String regularEvent(String name, int id) {
        return "{\"component\":\"input:" + id + "\",\"id\":" + id + ",\"event\":\"" + name + "\",\"ts\":1700000000.0}";
    }

    private static final String BLE_SCAN_EVENT = "{\"component\":\"script:1\",\"id\":1,"
            + "\"event\":\"ble.scan_result\",\"data\":[2,[[\"aa:bb:cc:dd:ee:01\",-97,\"AgEEAwMH/hT=\",\"\"]]],"
            + "\"ts\":1700000000.0}";

    @Test
    void frameWithSeveralRegularEventsIsForwardedOnce() throws ShellyApiException {
        socket.onMessage(session, frame(regularEvent("btn_down", 0) + "," + regularEvent("btn_up", 0)));

        verify(handler, times(1)).onNotifyEvent(anyString());
    }

    @Test
    void mixedFrameForwardsOnceAndKeepsTheRegularEvents() throws ShellyApiException {
        socket.onMessage(session,
                frame(BLE_SCAN_EVENT + "," + regularEvent("btn_down", 0) + "," + regularEvent("btn_up", 0)));

        // Once - not twice for the two regular events, and not at all for the scan result.
        verify(handler, times(1)).onNotifyEvent(anyString());
    }

    @Test
    void frameWithOnlyScanResultsIsNotForwarded() throws ShellyApiException {
        socket.onMessage(session, frame(BLE_SCAN_EVENT + "," + BLE_SCAN_EVENT));

        verify(handler, never()).onNotifyEvent(anyString());
    }

    @Test
    void singleRegularEventIsStillForwarded() throws ShellyApiException {
        socket.onMessage(session, frame(regularEvent("btn_down", 0)));

        verify(handler, times(1)).onNotifyEvent(anyString());
    }
}
