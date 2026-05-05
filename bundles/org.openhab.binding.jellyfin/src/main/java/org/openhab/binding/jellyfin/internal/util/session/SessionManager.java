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
package org.openhab.binding.jellyfin.internal.util.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.events.SessionEventBus;
import org.openhab.binding.jellyfin.internal.gen.current.model.SessionInfoDto;
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
     * Canonical (full) device IDs from currently configured client Things.
     * Used by {@link #buildDeviceDispatchMap} to route short-ID sessions (new Android app format)
     * to handlers that are still subscribed on the legacy full ID, bridging the gap before
     * {@link org.openhab.binding.jellyfin.internal.discovery.ClientDiscoveryService} updates the config.
     */
    private final AtomicReference<Set<String>> knownCanonicalDeviceIds = new AtomicReference<>(Set.of());

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
     * Updates the set of known canonical (full) device IDs from configured client Things.
     *
     * <p>
     * This should be called by {@code ServerHandler} whenever client Things are initialized or
     * updated, so that {@link #buildDeviceDispatchMap} can bridge the gap between a short device ID
     * reported by the server and the full device ID still stored in the Thing's {@code serialNumber}
     * configuration.
     *
     * @param ids the set of full device IDs currently stored in configured client Things
     */
    public void updateKnownDeviceIds(Collection<String> ids) {
        knownCanonicalDeviceIds.set(Set.copyOf(ids));
        logger.debug("Known canonical device IDs updated: {} ID(s)", ids.size());
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

        // Merge sessions from the same physical device that report under prefix-related device IDs
        // (e.g. an Android app background service uses a truncated device ID while the main session
        // uses the full device ID — both represent the same device).
        Map<String, SessionInfoDto> deviceDispatch = buildDeviceDispatchMap(newSessions.values());

        for (Map.Entry<String, SessionInfoDto> entry : deviceDispatch.entrySet()) {
            String deviceId = entry.getKey();
            currentDeviceIds.add(deviceId);
            eventBus.publishSessionUpdate(deviceId, entry.getValue());
            logger.debug("Published session update for device: {}", deviceId);
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
     * Builds a dispatch map keyed by canonical device ID, merging sessions that belong
     * to the same physical device but report under prefix-related device IDs.
     *
     * <p>
     * Some clients (e.g. Jellyfin Android) create multiple sessions: one for the main
     * app (full device ID) and one for a background service (a prefix of the full ID).
     * Discovery deduplication already handles this for thing creation; here we ensure the
     * handler registered on the canonical (longer) device ID receives the session that
     * actually contains playback data (NowPlayingItem / PositionTicks).
     *
     * <p>
     * Rule: when one device ID is a prefix of another, treat them as the same device.
     * Dispatch only the longer (canonical) ID. If the shorter ID's session has
     * NowPlayingItem but the longer ID's session does not, promote the shorter session
     * to the canonical key so the handler sees active playback.
     *
     * @param allSessions all sessions received from the server in this update
     * @return map from canonical device ID to the best session for that device
     */
    private Map<String, SessionInfoDto> buildDeviceDispatchMap(Collection<SessionInfoDto> allSessions) {
        // Collect unique sessions by device ID (last one wins for duplicate device IDs)
        Map<String, SessionInfoDto> byDeviceId = new LinkedHashMap<>();
        for (SessionInfoDto session : allSessions) {
            String deviceId = session.getDeviceId();
            if (deviceId != null && !deviceId.isBlank()) {
                byDeviceId.put(deviceId, session);
            }
        }

        Map<String, SessionInfoDto> result = new LinkedHashMap<>(byDeviceId);

        for (String shortId : new ArrayList<>(byDeviceId.keySet())) {
            for (String longId : new ArrayList<>(byDeviceId.keySet())) {
                if (shortId.equals(longId) || !longId.startsWith(shortId)) {
                    continue;
                }
                // shortId is a prefix of longId → same physical device; longId is the canonical key.
                SessionInfoDto shortSession = byDeviceId.get(shortId);
                SessionInfoDto longSession = byDeviceId.get(longId);
                if (shortSession != null && longSession != null && shortSession.getNowPlayingItem() != null
                        && longSession.getNowPlayingItem() == null) {
                    // Promote the shorter session (which has playback data) to the canonical ID
                    logger.debug(
                            "Merging session for device '{}' (prefix of '{}') into canonical ID — NowPlayingItem present",
                            shortId, longId);
                    result.put(longId, shortSession);
                }
                // Remove the shorter ID from dispatch; it is represented by the canonical ID
                result.remove(shortId);
            }
        }

        // Also route short IDs to known canonical (full) IDs from configured Things.
        // This covers the transition window where the Android app now sends only a short ANDROID_ID
        // but the configured Thing still has the legacy full ID (ANDROID_ID + userId hex) as
        // serialNumber. Without this, the ClientHandler subscribed on the full ID would never
        // receive session updates and would go OFFLINE.
        Set<String> known = knownCanonicalDeviceIds.get();
        if (!known.isEmpty()) {
            for (String shortId : new ArrayList<>(result.keySet())) {
                for (String knownFullId : known) {
                    if (knownFullId.startsWith(shortId) && !knownFullId.equals(shortId)) {
                        SessionInfoDto session = result.get(shortId);
                        if (session != null) {
                            logger.debug(
                                    "Dispatching short device ID '{}' to known canonical ID '{}' (pending config migration)",
                                    shortId, knownFullId);
                            result.put(knownFullId, session);
                            result.remove(shortId);
                        }
                        break;
                    }
                }
            }
        }

        return result;
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
        knownCanonicalDeviceIds.set(Set.of());
    }
}
