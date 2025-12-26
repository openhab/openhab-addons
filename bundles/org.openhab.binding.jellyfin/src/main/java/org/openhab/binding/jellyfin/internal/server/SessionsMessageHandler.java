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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionMessageType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionsMessage;
import org.openhab.binding.jellyfin.internal.util.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * WebSocket message handler that processes Jellyfin Sessions messages.
 *
 * Responsibilities:
 * - Parse incoming JSON using ApiClient's ObjectMapper (preserves custom deserializers)
 * - Handle MessageType "Sessions" and update session state via SessionManager
 * - Ignore other message types (logged at debug per backward-compat decision)
 * - Throw on parse failure to surface upstream (task handles lifecycle)
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class SessionsMessageHandler implements WebSocketMessageHandler {

    private static final int LOG_TRUNCATE_LENGTH = 200;

    private final Logger logger = LoggerFactory.getLogger(SessionsMessageHandler.class);
    private final ObjectMapper objectMapper;
    private final SessionManager sessionManager;

    public SessionsMessageHandler(ApiClient apiClient, SessionManager sessionManager) {
        this.objectMapper = apiClient.getObjectMapper();
        this.sessionManager = sessionManager;
    }

    @Override
    public void handleMessage(String message) {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("WebSocket message must not be null or empty");
        }

        String truncated = message.length() > LOG_TRUNCATE_LENGTH ? message.substring(0, LOG_TRUNCATE_LENGTH) + "..."
                : message;
        logger.debug("WebSocket message received (length={}): {}", Integer.valueOf(message.length()), truncated);

        try {
            JsonNode node = objectMapper.readTree(message);
            JsonNode typeNode = node.get("MessageType");
            String type = typeNode != null ? typeNode.asText() : null;

            if (!SessionMessageType.SESSIONS.toString().equals(type)) {
                logger.debug("Ignoring non-session WebSocket message: {}", type);
                return;
            }

            SessionsMessage sessionsMessage = objectMapper.treeToValue(node, SessionsMessage.class);
            handleSessionsMessage(sessionsMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse WebSocket message payload", e);
        }
    }

    private void handleSessionsMessage(SessionsMessage sessionsMessage) {
        List<SessionInfoDto> data = sessionsMessage.getData();
        Map<String, SessionInfoDto> newSessions = new HashMap<>();

        if (data != null) {
            for (SessionInfoDto session : data) {
                String sessionId = session.getId();
                if (sessionId == null || sessionId.isBlank()) {
                    logger.debug("Skipping session without id: {}", session);
                    continue;
                }
                newSessions.put(sessionId, session);
            }
        }

        sessionManager.updateSessions(newSessions);
    }
}
