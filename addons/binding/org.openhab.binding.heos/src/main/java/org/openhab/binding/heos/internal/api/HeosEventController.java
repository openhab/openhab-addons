/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.api;

import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import org.openhab.binding.heos.internal.resources.HeosCommands;
import org.openhab.binding.heos.internal.resources.HeosResponse;
import org.openhab.binding.heos.internal.resources.HeosSystemEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosEventController} is responsible for handling event, which are
 * received by the HEOS system.
 *
 * @author Johannes Einig - Initial contribution
 */

public class HeosEventController extends HeosSystemEventListener {
    private HeosResponse response = null;
    private HeosSystem system = null;
    private HeosCommands command = null;
    private String eventType = null;
    private String eventCommand = null;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public HeosEventController(HeosResponse response, HeosCommands command, HeosSystem system) {
        this.response = response;
        this.system = system;
        this.command = command;
    }

    public void handleEvent(int client) {
        if (client == 0) {
            logger.debug("HEOS send response: {}", response.getRawResponseMessage());
        } else if (client == 1) {
            logger.debug("HEOS event response: {}", response.getRawResponseMessage());
        }

        if (response.getEvent().getResult().equals(FAIL)) {
            String errorCode = response.getEvent().getErrorCode();
            String errorMessage = response.getEvent().getErrorMessage();

            logger.warn("HEOS System response failure with error code '{}' and message '{}'", errorCode, errorMessage);

            return;
        } else {
            this.eventType = response.getEvent().getEventType();
            this.eventCommand = response.getEvent().getCommandType();

            switch (eventType) {
                case EVENTTYPE_EVENT:
                    eventTypeEvent();
                    break;
                case EVENTTYPE_PLAYER:
                    eventTypePlayer();
                    break;
                case EVENTTYPE_SYSTEM:
                    eventTypeSystem();
                    break;
                case EVENTTYPE_BROWSE:
                    eventTypeBrowse();
                    break;
                case EVENTTYPE_GROUP:
                    eventTypeGroup();
                    break;
            }
        }
    }

    private void eventTypeEvent() {
        switch (eventCommand) {
            case PLAYER_NOW_PLAYING_PROGRESS:
                playerProgressChanged();
                break;
            case PLAYERS_CHANGED:
                fireBridgeEvent(EVENTTYPE_EVENT, SUCCESS, eventCommand);
                break;
            case PLAYER_NOW_PLAYING_CHANGED:
                mediaStateChanged();
                break;
            case PLAYER_STATE_CHANGED:
                playerStateChanged();
                break;
            case PLAYER_QUEUE_CHANGED:
                break;
            case SOURCES_CHANGED:
                break;
            case PLAYER_VOLUME_CHANGED:
                volumeChanged();
                break;
            case GROUPS_CHANGED:
                fireBridgeEvent(EVENTTYPE_EVENT, SUCCESS, eventCommand);
                break;
            case USER_CHANGED:
                userChanged();
                break;
            case SHUFFLE_MODE_CHANGED:
                shuffleModeChanged();
                break;
            case REPEAT_MODE_CHANGED:
                repeatModeChanged();
                break;

        }
    }

    private void eventTypePlayer() {
        switch (eventCommand) {
            case GET_NOW_PLAYING_MEDIA:
                getMediaState();
                break;
            case GET_PLAYER_INFO:
                break;
            case GET_PLAY_STATE:
                playerStateChanged();
                break;
            case GET_VOLUME:
                break;
            case GET_MUTE:
                break;
            case GET_QUEUE:
                break;
            case SET_PLAY_STATE:
                break;
            case SET_VOLUME:
                break;
        }
    }

    private void eventTypeBrowse() {
        switch (eventCommand) {
            case GET_MUSIC_SOURCES:
                break;
            case BROWSE:
                break;
        }
    }

    private void eventTypeSystem() {
        switch (eventCommand) {
            case SING_IN:
                signIn();
                break;
        }
    }

    private void eventTypeGroup() {
        // not implemented yet
    }

    private void playerStateChanged() {
        String pid = response.getPid();
        String event = HEOS_STATE;
        String command = response.getEvent().getMessagesMap().get(HEOS_STATE);
        fireStateEvent(pid, event, command);
    }

    private void playerProgressChanged() {
        String pos = response.getEvent().getMessagesMap().get(HEOS_CUR_POS);
        String duration = response.getEvent().getMessagesMap().get(HEOS_DURATION);
        String pid = response.getPid();

        fireStateEvent(pid, HEOS_CUR_POS, pos);
        fireStateEvent(pid, HEOS_DURATION, duration);
    }

    private void volumeChanged() {
        String pid = response.getPid();
        String event = HEOS_VOLUME;
        String command = response.getEvent().getMessagesMap().get(HEOS_LEVEL);
        fireStateEvent(pid, event, command);
        event = HEOS_MUTE;
        command = response.getEvent().getMessagesMap().get(HEOS_MUTE);
        fireStateEvent(pid, event, command);
    }

    private void mediaStateChanged() {
        String pid = response.getPid();
        system.send(command.getNowPlayingMedia(pid));
        fireMediaEvent(pid, response.getPayload().getPayloadList().get(0));
    }

    private void getMediaState() {
        String pid = response.getPid();
        fireMediaEvent(pid, response.getPayload().getPayloadList().get(0));
    }

    private void signIn() {
        if (response.getEvent().getMessagesMap().get(COM_UNDER_PROCESS).equals(FALSE)) {
            fireBridgeEvent(EVENTTYPE_SYSTEM, SUCCESS, SING_IN);
        }
    }

    private void shuffleModeChanged() {
        fireStateEvent(response.getPid(), SHUFFLE_MODE_CHANGED, response.getEvent().getMessagesMap().get(HEOS_SHUFFLE));
    }

    private void repeatModeChanged() {
        fireStateEvent(response.getPid(), REPEAT_MODE_CHANGED,
                response.getEvent().getMessagesMap().get(HEOS_REPEAT_MODE));
    }

    private void userChanged() {
        fireBridgeEvent(EVENTTYPE_SYSTEM, SUCCESS, USER_CHANGED);
    }

    public void connectionToSystemLost() {
        fireBridgeEvent(EVENTTYPE_EVENT, FAIL, CONNECTION_LOST);
    }

    public void connectionToSystemRestored() {
        fireBridgeEvent(EVENTTYPE_EVENT, SUCCESS, CONNECTION_RESTORED);
    }
}
