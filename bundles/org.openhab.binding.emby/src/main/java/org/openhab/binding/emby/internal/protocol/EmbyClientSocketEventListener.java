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
package org.openhab.binding.emby.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.emby.internal.model.EmbyPlayStateModel;

/**
 * Listener interface for receiving events from the {@code EmbyClientSocket}.
 * Implement this interface to handle connection lifecycle events and playback state updates
 * received from an Emby server.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public interface EmbyClientSocketEventListener {

    /**
     * Called when a playback state update is received from the Emby server.
     * Implementers should handle the new {@link EmbyPlayStateModel} accordingly.
     *
     * @param playstate the updated playback state information
     */
    void handleEvent(EmbyPlayStateModel playstate);

    /**
     * Called when the connection to the Emby server has been successfully opened.
     * Implementers can perform initialization or resource allocation here.
     */
    void onConnectionOpened();

    /**
     * Called when the connection to the Emby server has been closed.
     * Implementers should handle cleanup or reconnection logic here.
     */
    void onConnectionClosed();
}
