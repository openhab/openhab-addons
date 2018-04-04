/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.internal;

import java.util.EventListener;

import org.eclipse.smarthome.core.library.types.RawType;

/**
 * Interface which has to be implemented by a class in order to get status
 * updates from a {@link KodiConnection}
 *
 * @author Paul Frank - Initial contribution
 * @author Christoph Weitkamp - Added channels for opening PVR TV or Radio streams
 *
 */
public interface KodiEventListener extends EventListener {
    public enum KodiState {
        Play,
        Pause,
        End,
        Stop,
        Rewind,
        FastForward
    }

    void updateConnectionState(boolean connected);

    void updateScreenSaverState(boolean screenSaveActive);

    void updateVolume(int volume);

    void updatePlayerState(KodiState state);

    void updateMuted(boolean muted);

    void updateTitle(String title);

    void updateShowTitle(String title);

    void updateAlbum(String album);

    void updateArtist(String artist);

    void updateMediaType(String mediaType);

    void updatePVRChannel(final String channel);

    void updateThumbnail(RawType thumbnail);

    void updateFanart(RawType fanart);
}
