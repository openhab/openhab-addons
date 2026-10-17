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
package org.openhab.binding.hasslink.internal.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.api.dto.EntityStateCompressedEvent;
import org.openhab.binding.hasslink.internal.api.dto.HomeAssistantConfig;

import com.google.gson.JsonObject;

/**
 * The {@link HomeAssistantConnectionListener} is notified of connection lifecycle events and incoming
 * entity data by the {@link HomeAssistantWebSocketClient}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public interface HomeAssistantConnectionListener {

    /**
     * Called once the WebSocket connection has completed the authentication handshake ({@code auth_ok}).
     */
    void onAuthenticated(String haVersion);

    /**
     * Called when Home Assistant rejects the supplied access token ({@code auth_invalid}).
     *
     * @param message the reason reported by Home Assistant
     */
    void onAuthenticationFailed(String message);

    /**
     * Called when the WebSocket connection is closed, whether cleanly or unexpectedly.
     *
     * @param reason the close reason
     */
    void onConnectionClosed(String reason);

    /**
     * Called when a communication error occurs on the WebSocket connection.
     *
     * @param message a description of the error
     */
    void onConnectionError(String message);

    /**
     * Called with the initial configuration snapshot from Home Assistant.
     *
     * @param config the configuration snapshot
     * @param rawConfig the raw JSON configuration object
     */
    void onConfigSnapshot(HomeAssistantConfig config, JsonObject rawConfig);

    /**
     * Called with the initial snapshot of exposed entities, in response to
     * {@code openhab_bridge/get_entities}.
     *
     * @param entities the snapshot of entity states
     */
    void onEntitiesSnapshot(List<EntityState> entities);

    /**
     * Called with a device registry snapshot.
     *
     * @param devices device registry entries as supplied by Home Assistant
     */
    void onDeviceRegistrySnapshot(List<JsonObject> devices);

    /**
     * Called with an entity registry snapshot.
     *
     * @param entities entity registry entries as supplied by Home Assistant
     */
    void onEntityRegistrySnapshot(List<JsonObject> entities);

    /**
     * Called whenever a subscribed {@code state_changed} event is received.
     *
     * @param state the new entity state
     */
    void onEntityStateChanged(EntityState state);

    /**
     * Called when a compressed entity state update or diff event is received
     * from Home Assistant via the WebSocket {@code subscribe_entities} stream.
     *
     * @param compressedEvent the compressed event payload containing entity additions,
     *            state deltas, or removals
     */
    void onCompressedEvent(EntityStateCompressedEvent compressedEvent);
}
