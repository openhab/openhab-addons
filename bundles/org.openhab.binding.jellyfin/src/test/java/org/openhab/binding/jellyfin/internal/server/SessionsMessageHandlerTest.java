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
package org.openhab.binding.jellyfin.internal.server;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.api.ApiClientWrapper;
import org.openhab.binding.jellyfin.internal.events.SessionEventBus;
import org.openhab.binding.jellyfin.internal.gen.current.model.ForceKeepAliveMessage;
import org.openhab.binding.jellyfin.internal.gen.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.gen.current.model.SessionMessageType;
import org.openhab.binding.jellyfin.internal.gen.current.model.SessionsMessage;
import org.openhab.binding.jellyfin.internal.util.session.SessionManager;

/**
 * Tests for {@link SessionsMessageHandler}.
 *
 * @author Patrik Gfeller - Initial contribution
 */
class SessionsMessageHandlerTest {

    private ApiClientWrapper apiClient;
    private SessionEventBus eventBus;
    private SessionManager sessionManager;
    private SessionsMessageHandler handler;

    @BeforeEach
    void setUp() {
        apiClient = new ApiClientWrapper();
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
        session.setUserId(UUID.randomUUID());

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
        session.setUserId(UUID.randomUUID());

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

    @Test
    void handleSessionsMessage_nullUserIdSession_isNotDispatched() throws Exception {
        // Arrange: a session with userId = "00000000-0000-0000-0000-000000000000" (server-owned)
        String serverDeviceId = "openHAB-server-device";
        SessionInfoDto serverSession = new SessionInfoDto();
        serverSession.setId("server-session-1");
        serverSession.setDeviceId(serverDeviceId);
        serverSession.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        serverSession.setClient("openHAB");

        // A normal user session that should be dispatched
        String userDeviceId = "normal-device";
        SessionInfoDto userSession = new SessionInfoDto();
        userSession.setId("user-session-1");
        userSession.setDeviceId(userDeviceId);
        userSession.setUserId(UUID.randomUUID());

        SessionsMessage message = new SessionsMessage(SessionMessageType.SESSIONS);
        message.setMessageId(UUID.randomUUID());
        message.setData(List.of(serverSession, userSession));

        String json = apiClient.getObjectMapper().writeValueAsString(message);

        AtomicInteger serverUpdates = new AtomicInteger();
        AtomicInteger userUpdates = new AtomicInteger();
        eventBus.subscribe(serverDeviceId, s -> serverUpdates.incrementAndGet());
        eventBus.subscribe(userDeviceId, s -> userUpdates.incrementAndGet());

        handler.handleMessage(json);

        assertEquals(0, serverUpdates.get(), "Server-owned null-UUID session must not be dispatched");
        assertEquals(1, userUpdates.get(), "Normal user session must be dispatched");
    }

    @Test
    void handleSessionsMessage_javaNull_userId_isNotDispatched() throws Exception {
        // Arrange: a session with no userId set (Java null) — defensive check
        String serverDeviceId = "null-user-device";
        SessionInfoDto session = new SessionInfoDto();
        session.setId("null-user-session");
        session.setDeviceId(serverDeviceId);
        // userId deliberately left null

        SessionsMessage message = new SessionsMessage(SessionMessageType.SESSIONS);
        message.setMessageId(UUID.randomUUID());
        message.setData(List.of(session));

        String json = apiClient.getObjectMapper().writeValueAsString(message);

        AtomicInteger updates = new AtomicInteger();
        eventBus.subscribe(serverDeviceId, s -> updates.incrementAndGet());

        handler.handleMessage(json);

        assertEquals(0, updates.get(), "Session with Java-null userId must not be dispatched");
    }
}
