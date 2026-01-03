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
package org.openhab.binding.jellyfin.internal.util.session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.events.SessionEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages Jellyfin client session state and coordinates session update events.
 * 
 * <p>
 * This class encapsulates session lifecycle management previously scattered
 * throughout ServerHandler. It maintains the session map, tracks device IDs,
 * and publishes session updates to the event bus.
 * 
 * <p>
 * SOLID Principles:
 * <ul>
 * <li>Single Responsibility: Only manages session state</li>
 * <li>Dependency Inversion: Depends on SessionEventBus abstraction</li>
 * </ul>
 * 
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class SessionManager {
    private final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private final SessionEventBus eventBus;
    private final Map<String, SessionInfoDto> sessions = new HashMap<>();
    private final Set<String> previousDeviceIds = new HashSet<>();

    /**
     * Creates a new session manager.
     * 
     * @param eventBus The event bus for publishing session updates
     */
    public SessionManager(SessionEventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Gets the current session map (read-only access).
     * 
     * @return Map of session ID to session info
     */
    public Map<String, SessionInfoDto> getSessions() {
        return new HashMap<>(sessions);
    }

    /**
     * Updates the session map and publishes events for changed sessions.
     * 
     * <p>
     * This method:
     * <ul>
     * <li>Replaces current sessions with new session data</li>
     * <li>Publishes session updates to event bus for active devices</li>
     * <li>Publishes null sessions for devices that went offline</li>
     * </ul>
     * 
     * @param newSessions The new session map (typically from ClientListUpdater)
     */
    public void updateSessions(Map<String, SessionInfoDto> newSessions) {
        // Track which devices are currently active
        Set<String> currentDeviceIds = new HashSet<>();

        // Update sessions and publish events for active devices
        sessions.clear();
        sessions.putAll(newSessions);

        for (SessionInfoDto session : newSessions.values()) {
            String deviceId = session.getDeviceId();
            if (deviceId != null && !deviceId.isBlank()) {
                currentDeviceIds.add(deviceId);
                eventBus.publishSessionUpdate(deviceId, session);
                logger.debug("Published session update for device: {}", deviceId);
            }
        }

        // Detect devices that went offline (were active, now gone)
        Set<String> offlineDevices = new HashSet<>(previousDeviceIds);
        offlineDevices.removeAll(currentDeviceIds);

        for (String deviceId : offlineDevices) {
            eventBus.publishSessionUpdate(deviceId, null);
            logger.debug("Published offline notification for device: {}", deviceId);
        }

        // Update tracking set for next iteration
        previousDeviceIds.clear();
        previousDeviceIds.addAll(currentDeviceIds);
    }

    /**
     * Clears all session state.
     * 
     * <p>
     * Should be called during ServerHandler.dispose().
     */
    public void clear() {
        sessions.clear();
        previousDeviceIds.clear();
    }
}
