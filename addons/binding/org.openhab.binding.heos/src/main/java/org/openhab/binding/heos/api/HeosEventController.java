/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.api;

import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.resources.HeosCommands;
import org.openhab.binding.heos.internal.resources.HeosResponse;
import org.openhab.binding.heos.internal.resources.MyEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosEventController} is responsible for handling event, which are
 * received by the HEOS system.
 *
 * @author Johannes Einig - Initial contribution
 */

public class HeosEventController extends MyEventListener {

    private HeosResponse response = null;
    private HeosSystem system = null;
    private HeosCommands command = null;
    private String eventType = null;
    private String eventCommand = null;

    private Logger logger = LoggerFactory.getLogger(HeosBridgeHandler.class);

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

                case "event":
                    eventTypeEvent();
                    break;
                case "player":
                    eventTypePlayer();
                    break;
                case "system":
                    eventTypeSystem();
                    break;
                case "browse":
                    eventTypeBrowse();
                    break;
                case "group":
                    eventTypeGroup();
                    break;

            }

        }
    }

    private void eventTypeEvent() {

        switch (eventCommand) {

            case "player_now_playing_progress":
                playerProgressChanged();
                break;
            case "players_changed":
                fireBridgeEvent("event", null, eventCommand);
                break;
            case "player_now_playing_changed":
                mediaStateChanged();
                break;
            case "player_state_changed":
                playerStateChanged();
                break;
            case "player_queue_changed":
                break;
            case "sources_changed":
                break;
            case "player_volume_changed":
                volumeChanged();
                break;
            case "groups_changed":
                fireBridgeEvent("event", null, eventCommand);
                break;
            case "user_changed":
                userChanged();
                break;

        }
    }

    private void eventTypePlayer() {

        switch (eventCommand) {

            case "get_now_playing_media":
                getMediaState();
                break;
            case "get_player_info":
                break;
            case "get_play_state":
                playerStateChanged();
                break;
            case "get_volume":
                break;
            case "get_mute":
                break;
            case "get_queue":
                break;
            case "set_play_state":
                break;
            case "set_volume":
                break;

        }
    }

    private void eventTypeBrowse() {

        switch (eventCommand) {

            case "get_music_sources":
                break;
            case "browse":
                break;

        }
    }

    private void eventTypeSystem() {
        switch (eventCommand) {

            case COM_SING_IN:
                signIn();
                break;
        }

    }

    private void eventTypeGroup() {
        // not implemented yet
    }

    private void playerStateChanged() {

        String pid = response.getPid();
        String event = "state";
        String command = response.getEvent().getMessagesMap().get("state");
        fireStateEvent(pid, event, command);
    }

    private void playerProgressChanged() {
        String pos = response.getEvent().getMessagesMap().get("cur_pos");
        String duration = response.getEvent().getMessagesMap().get("duration");
        String pid = response.getPid();

        fireStateEvent(pid, "curPos", pos);
        fireStateEvent(pid, "duration", duration);

    }

    private void volumeChanged() {
        String pid = response.getPid();
        String event = "volume";
        String command = response.getEvent().getMessagesMap().get("level");
        fireStateEvent(pid, event, command);
        event = "mute";
        command = response.getEvent().getMessagesMap().get("mute");
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
            fireBridgeEvent(EVENT_SYSTEM, SUCCESS, COM_SING_IN);
        }
    }

    private void userChanged() {
        fireBridgeEvent(EVENT_SYSTEM, SUCCESS, COM_USER_CHANGED);
    }

    public void connectionToSystemLost() {
        fireBridgeEvent(EVENT_EVENT, FAIL, CONNECTION_LOST);
    }

    public void connectionToSystemRestored() {
        fireBridgeEvent(EVENT_EVENT, SUCCESS, CONNECTION_RESTORED);
    }

}
