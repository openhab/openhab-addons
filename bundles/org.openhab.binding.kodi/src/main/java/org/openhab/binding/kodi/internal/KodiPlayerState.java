/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.kodi.internal;

import org.openhab.binding.kodi.internal.KodiEventListener.KodiState;

/**
 * The {@link KodiPlayerState} is responsible for saving the state of a player.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class KodiPlayerState {
    private int savedPlaylistID;
    private int savedVolume;
    private KodiState savedState;

    public int getSavedPlaylistID() {
        return savedPlaylistID;
    }

    public void setPlaylistID(int savedPlaylistID) {
        this.savedPlaylistID = savedPlaylistID;
    }

    public int getSavedVolume() {
        return savedVolume;
    }

    public void setSavedVolume(int savedVolume) {
        this.savedVolume = savedVolume;
    }

    public KodiState getSavedState() {
        return savedState;
    }

    public void setSavedState(KodiState savedState) {
        this.savedState = savedState;
    }
}
