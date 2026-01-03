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
package org.openhab.binding.jellyfin.internal.events;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.SessionInfoDto;

/**
 * Listener interface for receiving Jellyfin session update notifications.
 * Implementations are notified when session state changes for a specific device.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
@FunctionalInterface
public interface SessionEventListener {
    /**
     * Called when a session update occurs for a subscribed device.
     * 
     * @param session The updated session information, or null if session ended/offline
     */
    void onSessionUpdate(@Nullable SessionInfoDto session);
}
