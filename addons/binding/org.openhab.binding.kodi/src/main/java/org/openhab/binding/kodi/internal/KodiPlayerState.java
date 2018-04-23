/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.internal;

import org.openhab.binding.kodi.internal.KodiEventListener.KodiState;

/**
 * The {@link KodiPlayerState} is responsible for saving the state of a player.
 *
 * @author Christoph Weitkamp - Improvements for playing audio notifications
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
