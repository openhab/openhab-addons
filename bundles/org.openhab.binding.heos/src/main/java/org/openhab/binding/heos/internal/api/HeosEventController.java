/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.openhab.binding.heos.internal.resources.HeosCommands;
import org.openhab.binding.heos.internal.resources.HeosResponseDecoder;
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
    private final Logger logger = LoggerFactory.getLogger(HeosEventController.class);

    private HeosResponseDecoder heosDecoder;
    private HeosSystem system;
    private HeosCommands command;
    private String eventType;
    private String eventCommand;

    public HeosEventController(HeosResponseDecoder heosDecoder, HeosCommands command, HeosSystem system) {
        this.heosDecoder = heosDecoder;
        this.system = system;
        this.command = command;
    }

    public void handleEvent(int client) {
        if (client == 0) {
            logger.debug("HEOS send response: {}", heosDecoder.getRawResponseMessage());
        } else if (client == 1) {
            logger.debug("HEOS event response: {}", heosDecoder.getRawResponseMessage());
        }

        if (heosDecoder.getSendResult().equals(FAIL)) {
            String errorCode = heosDecoder.getErrorCode();
            String errorMessage = heosDecoder.getErrorMessage();

            logger.debug("HEOS System response failure with error code '{}' and message '{}'", errorCode, errorMessage);
        } else {
            this.eventType = heosDecoder.getEventType();
            this.eventCommand = heosDecoder.getCommandType();

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
                volumeChanged();
                break;
            case GET_MUTE:
                muteChanged();
                break;
            case GET_PLAY_MODE:
                playModeChanged();
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
        switch (eventCommand) {
            case GET_VOLUME:
                volumeChanged();
                break;
            case GET_MUTE:
                muteChanged();
                break;
        }
    }

    private void playerStateChanged() {
        String pid = heosDecoder.getPid();
        String event = STATE;
        String command = heosDecoder.getPlayState();
        fireStateEvent(pid, event, command);
    }

    private void playerProgressChanged() {
        String pos = heosDecoder.getPlayerCurrentPosition();
        String duration = heosDecoder.getPlayerDuration();
        String pid = heosDecoder.getPid();

        int intPosition = Integer.valueOf(pos) / 1000;
        int intDuration = Integer.valueOf(duration) / 1000;

        fireStateEvent(pid, CUR_POS, String.valueOf(intPosition));
        fireStateEvent(pid, DURATION, String.valueOf(intDuration));
    }

    private void volumeChanged() {
        String pid = heosDecoder.getPid();
        String event = VOLUME;
        String command = heosDecoder.getPlayerVolume();
        fireStateEvent(pid, event, command);
        event = MUTE;
        command = heosDecoder.getPlayerMuteState();
        fireStateEvent(pid, event, command);
    }

    private void muteChanged() {
        String pid = heosDecoder.getPid();
        String event = MUTE;
        String command = heosDecoder.getPlayerMuteState();
        fireStateEvent(pid, event, command);
    }

    private void playModeChanged() {
        shuffleModeChanged();
        repeatModeChanged();
    }

    private void mediaStateChanged() {
        String pid = heosDecoder.getPid();
        system.send(command.getNowPlayingMedia(pid));
        fireMediaEvent(pid, heosDecoder.getNowPlayingMedia());
    }

    private void getMediaState() {
        String pid = heosDecoder.getPid();
        fireMediaEvent(pid, heosDecoder.getNowPlayingMedia());
    }

    private void signIn() {
        if (!heosDecoder.isCommandUnderProgress()) {
            fireBridgeEvent(EVENTTYPE_SYSTEM, SUCCESS, SING_IN);
        }
    }

    private void shuffleModeChanged() {
        fireStateEvent(heosDecoder.getPid(), SHUFFLE_MODE_CHANGED, heosDecoder.getShuffleMode());

    }

    private void repeatModeChanged() {
        fireStateEvent(heosDecoder.getPid(), REPEAT_MODE_CHANGED, heosDecoder.getRepeateMode());
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
