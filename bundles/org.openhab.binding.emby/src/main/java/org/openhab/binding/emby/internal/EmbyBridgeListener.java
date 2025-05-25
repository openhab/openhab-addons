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
package org.openhab.binding.emby.internal;

import java.util.EventListener;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.emby.internal.model.EmbyPlayStateModel;

/**
 * Interface which has to be implemented by a class in order to get status
 * updates from a {@link EmbyConnection}
 *
 * @author Zachary Christiansen - Initial Contribution
 */
@NonNullByDefault
public interface EmbyBridgeListener extends EventListener {

    /**
     * Callback invoked when the connection state to the Emby server changes.
     *
     * @param connected {@code true} if the binding is currently connected to the Emby server,
     *            {@code false} otherwise.
     */
    void updateConnectionState(boolean connected);

    /**
     * Callback invoked when a playback event is received from the Emby server.
     *
     * @param playstate the {@link EmbyPlayStateModel} containing details about the current playback state
     * @param hostname the hostname or IP address of the Emby server that sent the event
     * @param embyport the port number on which the Emby server is running
     */
    void handleEvent(EmbyPlayStateModel playstate, String hostname, int embyport);
}
