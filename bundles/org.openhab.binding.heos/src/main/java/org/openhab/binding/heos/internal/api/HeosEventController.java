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
package org.openhab.binding.heos.internal.api;

import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.heos.internal.json.dto.HeosCommunicationAttribute;
import org.openhab.binding.heos.internal.json.dto.HeosEvent;
import org.openhab.binding.heos.internal.json.dto.HeosEventObject;
import org.openhab.binding.heos.internal.json.dto.HeosResponseObject;
import org.openhab.binding.heos.internal.json.payload.Media;
import org.openhab.binding.heos.internal.resources.HeosCommands;
import org.openhab.binding.heos.internal.resources.HeosSystemEventListener;
import org.openhab.binding.heos.internal.resources.Telnet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosEventController} is responsible for handling event, which are
 * received by the HEOS system.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosEventController extends HeosSystemEventListener {
    private final Logger logger = LoggerFactory.getLogger(HeosEventController.class);

    private final HeosSystem system;

    private long lastEventTime;

    public HeosEventController(HeosSystem system) {
        this.system = system;
        lastEventTime = System.currentTimeMillis();
    }

    public void handleEvent(HeosEventObject eventObject) {
        HeosEvent command = eventObject.command;
        lastEventTime = System.currentTimeMillis();

        logger.debug("Handling event: {}", eventObject);

        if (command == null) {
            return;
        }

        switch (command) {
            case PLAYER_NOW_PLAYING_PROGRESS:
            case PLAYER_STATE_CHANGED:
            case PLAYER_VOLUME_CHANGED:
            case SHUFFLE_MODE_CHANGED:
            case REPEAT_MODE_CHANGED:
            case PLAYER_PLAYBACK_ERROR:
            case GROUP_VOLUME_CHANGED:
            case PLAYER_QUEUE_CHANGED:
            case SOURCES_CHANGED:
                fireStateEvent(eventObject);
                break;

            case USER_CHANGED:
                fireBridgeEvent(EVENT_TYPE_SYSTEM, true, command);
                break;

            case PLAYER_NOW_PLAYING_CHANGED:
                String pid = eventObject.getAttribute(HeosCommunicationAttribute.PLAYER_ID);

                if (pid == null) {
                    logger.debug("HEOS did not mention which player changed, unlikely but ignore");
                    break;
                }

                try {
                    HeosResponseObject<Media> mediaResponse = system.send(HeosCommands.getNowPlayingMedia(pid),
                            Media.class);
                    Media responseMedia = mediaResponse.payload;
                    if (responseMedia != null) {
                        fireMediaEvent(pid, responseMedia);
                    }
                } catch (IOException | Telnet.ReadException e) {
                    logger.debug("Failed to retrieve current playing media, will try again next time.", e);
                }
                break;

            case GROUPS_CHANGED:
            case PLAYERS_CHANGED:
                fireBridgeEvent(EVENT_TYPE_EVENT, true, command);
                break;
        }
    }

    public void connectionToSystemLost() {
        fireBridgeEvent(EVENT_TYPE_EVENT, false, CONNECTION_LOST);
    }

    public void eventStreamTimeout() {
        fireBridgeEvent(EVENT_TYPE_EVENT, false, EVENT_STREAM_TIMEOUT);
    }

    public void systemReachable() {
        fireBridgeEvent(EVENT_TYPE_EVENT, true, CONNECTION_RESTORED);
    }

    long getLastEventTime() {
        return lastEventTime;
    }
}
