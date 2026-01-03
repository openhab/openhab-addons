/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ForceKeepAliveMessage;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionMessageType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionsMessage;
import org.openhab.binding.jellyfin.internal.events.SessionEventBus;
import org.openhab.binding.jellyfin.internal.util.session.SessionManager;

/**
 * Tests for {@link SessionsMessageHandler}.
 *
 * @author Patrik Gfeller - Initial contribution
 */
class SessionsMessageHandlerTest {

    private ApiClient apiClient;
    private SessionEventBus eventBus;
    private SessionManager sessionManager;
    private SessionsMessageHandler handler;

    @BeforeEach
    void setUp() {
        apiClient = new ApiClient();
        apiClient.setBasePath("http://localhost:8096");
        eventBus = new SessionEventBus();
        sessionManager = new SessionManager(eventBus);
        handler = new SessionsMessageHandler(apiClient, sessionManager);
    }

    @Test
    void handleSessionsMessagePublishesUpdates() throws Exception {
        String deviceId = "device-1";
        SessionInfoDto session = new SessionInfoDto();
        session.setId("session-1");
        session.setDeviceId(deviceId);

        SessionsMessage message = new SessionsMessage(SessionMessageType.SESSIONS);
        message.setMessageId(UUID.randomUUID());
        message.setData(List.of(session));

        String json = apiClient.getObjectMapper().writeValueAsString(message);

        AtomicReference<SessionInfoDto> lastSession = new AtomicReference<>();
        eventBus.subscribe(deviceId, lastSession::set);

        handler.handleMessage(json);

        assertEquals("session-1", lastSession.get().getId());
        assertEquals(deviceId, lastSession.get().getDeviceId());
    }

    @Test
    void handleSessionsMessagePublishesOfflineWhenMissing() throws Exception {
        String deviceId = "device-2";
        SessionInfoDto session = new SessionInfoDto();
        session.setId("session-2");
        session.setDeviceId(deviceId);

        SessionsMessage first = new SessionsMessage(SessionMessageType.SESSIONS);
        first.setMessageId(UUID.randomUUID());
        first.setData(List.of(session));

        SessionsMessage second = new SessionsMessage(SessionMessageType.SESSIONS);
        second.setMessageId(UUID.randomUUID());
        second.setData(List.of());

        String firstJson = apiClient.getObjectMapper().writeValueAsString(first);
        String secondJson = apiClient.getObjectMapper().writeValueAsString(second);

        AtomicReference<SessionInfoDto> lastSession = new AtomicReference<>();
        AtomicInteger updates = new AtomicInteger();
        eventBus.subscribe(deviceId, s -> {
            updates.incrementAndGet();
            lastSession.set(s);
        });

        handler.handleMessage(firstJson);
        handler.handleMessage(secondJson);

        assertEquals(2, updates.get());
        assertNull(lastSession.get());
    }

    @Test
    void handleNonSessionsMessageIsIgnored() throws Exception {
        ForceKeepAliveMessage keepAlive = new ForceKeepAliveMessage();
        String json = apiClient.getObjectMapper().writeValueAsString(keepAlive);

        AtomicInteger updates = new AtomicInteger();
        eventBus.subscribe("any", s -> updates.incrementAndGet());

        handler.handleMessage(json);

        assertEquals(0, updates.get());
    }

    @Test
    void invalidJsonThrows() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> handler.handleMessage("not-json"));
        assertTrue(ex.getMessage().contains("Failed to parse WebSocket message payload"));
    }
}
