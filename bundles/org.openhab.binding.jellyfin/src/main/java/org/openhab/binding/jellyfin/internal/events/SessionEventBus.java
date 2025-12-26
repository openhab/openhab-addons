/**
 * Copyright (C) 2010-2025 openHAB.org and the original author(s)
 *
 * See the NOTICE file(s) distributed with this work for additional information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrik Gfeller - Initial contribution
 */
package org.openhab.binding.jellyfin.internal.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionInfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event bus for distributing Jellyfin session updates to interested listeners.
 * Thread-safe: concurrent subscriptions and publications are supported.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class SessionEventBus {
    private final Logger logger = LoggerFactory.getLogger(SessionEventBus.class);

    private final Map<String, List<SessionEventListener>> listeners = new ConcurrentHashMap<>();

    /**
     * Subscribes a listener to receive session updates for a specific device.
     * 
     * @param deviceId The device ID to listen for
     * @param listener The listener to notify on session updates
     */
    public void subscribe(String deviceId, SessionEventListener listener) {
        listeners.computeIfAbsent(deviceId, k -> new CopyOnWriteArrayList<>()).add(listener);
        logger.debug("Listener subscribed for device ID: {}", deviceId);
    }

    /**
     * Unsubscribes a listener from receiving session updates for a specific device.
     * 
     * @param deviceId The device ID to stop listening for
     * @param listener The listener to remove
     */
    public void unsubscribe(String deviceId, SessionEventListener listener) {
        List<SessionEventListener> deviceListeners = listeners.get(deviceId);
        if (deviceListeners != null) {
            deviceListeners.remove(listener);
            if (deviceListeners.isEmpty()) {
                listeners.remove(deviceId);
            }
            logger.debug("Listener unsubscribed for device ID: {}", deviceId);
        }
    }

    /**
     * Publishes a session update to all listeners subscribed to the given device ID.
     * 
     * @param deviceId The device ID whose session has updated
     * @param session The new session state, or null if session ended
     */
    public void publishSessionUpdate(String deviceId, @Nullable SessionInfoDto session) {
        List<SessionEventListener> deviceListeners = listeners.get(deviceId);
        if (deviceListeners == null || deviceListeners.isEmpty()) {
            logger.trace("No listeners for device ID: {}", deviceId);
            return;
        }

        logger.debug("Publishing session update for device ID {} to {} listener(s)", deviceId,
                Integer.valueOf(deviceListeners.size()));

        for (SessionEventListener listener : deviceListeners) {
            try {
                listener.onSessionUpdate(session);
            } catch (Throwable t) {
                logger.warn("Listener threw during session update for device {}: {}", deviceId, t.getMessage());
                logger.debug("Listener exception", t);
            }
        }
    }

    /** Clears all listener subscriptions. */
    public void clear() {
        int count = getTotalListenerCount();
        listeners.clear();
        logger.debug("Cleared {} listener subscription(s)", Integer.valueOf(count));
    }

    /** Returns number of listeners for a device ID. */
    public int getListenerCount(String deviceId) {
        List<SessionEventListener> deviceListeners = listeners.get(deviceId);
        return deviceListeners != null ? deviceListeners.size() : 0;
    }

    /** Returns total number of active subscriptions across all device IDs. */
    public int getTotalListenerCount() {
        return listeners.values().stream().mapToInt(List::size).sum();
    }
}
