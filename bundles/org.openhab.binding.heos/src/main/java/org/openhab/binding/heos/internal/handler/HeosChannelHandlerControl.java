/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.heos.internal.handler;

import static org.openhab.binding.heos.internal.resources.HeosConstants.SONG;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.exception.HeosNotFoundException;
import org.openhab.binding.heos.internal.json.payload.Media;
import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.openhab.binding.heos.internal.resources.HeosMediaEventListener;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link HeosChannelHandlerControl} handles the control commands
 * coming from the implementing thing
 *
 * @author Johannes Einig - Initial contribution
 * @author Martin van Wingerden - change handling of stop/pause depending on playing item type
 */
@NonNullByDefault
public class HeosChannelHandlerControl extends BaseHeosChannelHandler implements HeosMediaEventListener {
    private final HeosEventListener eventListener;
    private final Map<String, Media> playingMediaCache = new HashMap<>();

    public HeosChannelHandlerControl(HeosEventListener eventListener, HeosBridgeHandler bridge) {
        super(bridge);
        bridge.registerMediaEventListener(this);
        this.eventListener = eventListener;
    }

    @Override
    public void handlePlayerCommand(Command command, String id, ThingUID uid) throws IOException, ReadException {
        handleCommand(command, id);
    }

    @Override
    public void handleGroupCommand(Command command, @Nullable String id, ThingUID uid,
            HeosGroupHandler heosGroupHandler) throws IOException, ReadException {
        if (id == null) {
            throw new HeosNotFoundException();
        }
        handleCommand(command, id);
    }

    @Override
    public void handleBridgeCommand(Command command, ThingUID uid) {
        // No such channel within bridge
    }

    private void handleCommand(Command command, String id) throws IOException, ReadException {
        if (command instanceof RefreshType) {
            eventListener.playerStateChangeEvent(getApi().getPlayState(id));
            return;
        }
        switch (command.toString()) {
            case "PLAY":
            case "ON":
                getApi().play(id);
                break;
            case "PAUSE":
            case "OFF":
                if (shouldPause(id)) {
                    getApi().pause(id);
                } else {
                    getApi().stop(id);
                }
                break;
            case "NEXT":
                getApi().next(id);
                break;
            case "PREVIOUS":
                getApi().previous(id);
                break;
        }
    }

    private boolean shouldPause(String id) {
        Media applicableMedia = playingMediaCache.get(id);
        if (applicableMedia == null || SONG.equals(applicableMedia.type)) {
            return true;
        } else {
            // we have a station here, just have to check which one
            switch (applicableMedia.sourceId) {
                case Media.SOURCE_TUNE_IN:
                case Media.SOURCE_I_HEART_RADIO:
                case Media.SOURCE_SIRIUS_XM:
                case Media.SOURCE_AUX:
                    return false;

                default:
                    return true;
            }
        }
    }

    @Override
    public void playerMediaChangeEvent(String pid, Media media) {
        playingMediaCache.put(pid, media);
    }
}
