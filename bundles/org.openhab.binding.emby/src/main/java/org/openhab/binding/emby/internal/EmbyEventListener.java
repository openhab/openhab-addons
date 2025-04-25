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
import org.openhab.binding.emby.internal.protocol.EmbyConnection;

/**
 * Interface which has to be implemented by a class in order to get status
 * updates from a {@link EmbyConnection}
 *
 * @author Zachary Christiansen - Initial Contribution
 */
@NonNullByDefault
public interface EmbyEventListener extends EventListener {
    public enum EmbyState {
        PLAY,
        PAUSE,
        END,
        STOP,
        REWIND,
        FASTFORWARD
    }

    public enum EmbyPlaylistState {
        ADD,
        ADDED,
        INSERT,
        REMOVE,
        REMOVED,
        CLEAR
    }

    void updateConnectionState(boolean connected);

    void updateScreenSaverState(boolean screenSaveActive);

    void updatePlayerState(EmbyState state);

    void updateMuted(boolean muted);

    void updateTitle(String title);

    void updateShowTitle(String title);

    void updateMediaType(String mediaType);

    void updateCurrentTime(long currentTime);

    void updateDuration(long duration);

    void updatePrimaryImageURL(String imageURL);

    public void handleEvent(EmbyPlayStateModel playstate, String hostname, int embyport);
}
