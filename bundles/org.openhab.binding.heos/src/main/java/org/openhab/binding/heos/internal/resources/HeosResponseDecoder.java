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
package org.openhab.binding.heos.internal.resources;

import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import java.util.List;
import java.util.Map;

/**
 * The {@link HeosResponseDecoder} provides a facade
 * to the received message from the HEOS system
 *
 * @author Johannes Einig - Initial contribution
 *
 */
public class HeosResponseDecoder {

    private HeosResponse response = new HeosResponse();
    private HeosJsonParser parser = new HeosJsonParser(response);

    public String getPlayState() {
        return getMessagesMap().get(STATE);
    }

    public String getPlayerMuteState() {
        return getMessagesMap().get(STATE);
    }

    public String getPlayerVolume() {
        return getMessagesMap().get(LEVEL);
    }

    public String getPlayerDuration() {
        return getMessagesMap().get(DURATION);
    }

    public String getPlayerCurrentPosition() {
        return getMessagesMap().get(CUR_POS);
    }

    public String getShuffleMode() {
        return getMessagesMap().get(SHUFFLE);
    }

    public String getRepeateMode() {
        return getMessagesMap().get(REPEAT_MODE);
    }

    public String getGroupMute() {
        return getMessagesMap().get(STATE);
    }

    public String getGroupVolume() {
        return getMessagesMap().get(LEVEL);
    }

    public Map<String, String> getNowPlayingMedia() {
        return getPayloadList().get(0);
    }

    public String getNowPlayingMediaArtist() {
        return getPayloadList().get(0).get(ARTIST);
    }

    public String getNowPlayingMediaSong() {
        return getPayloadList().get(0).get(SONG);
    }

    public String getNowPlayingMediaAlbum() {
        return getPayloadList().get(0).get(ALBUM);
    }

    public String getNowPlayingMediaImageUrl() {
        return getPayloadList().get(0).get(IMAGE_URL);
    }

    public String getNowPlayingMediaImageQid() {
        return getPayloadList().get(0).get(QID);
    }

    public String getNowPlayingMediaMid() {
        return getPayloadList().get(0).get(MID);
    }

    public String getNowPlayingMediaAlbumID() {
        return getPayloadList().get(0).get(ALBUM_ID);
    }

    public String getNowPlayingMediaStation() {
        return getPayloadList().get(0).get(STATION);
    }

    // HEOS Payload Results

    /**
     * This returns a list with one element for each group.
     * Each of this elements contain again a list with one
     * element for a each player which is part of the group.
     * This information is received by the get_groups command.
     * The HashMap within the last list represents the player
     * with its informations
     *
     * @return nested Lists for the groups and their members
     */
    public List<List<Map<String, String>>> getPlayerList() {
        return response.getPayload().getPlayerList();
    }

    // HEOS Event Results

    /**
     * Returns the raw message received by the
     * HEOS system.
     *
     * @return the un-decoded command from the HOES system
     */
    public String getCommand() {
        return response.getEvent().getCommand();
    }

    /**
     * Returns the result information of the HEOS message
     *
     * @return either {@link HeosConstants.SUCCESS} or {@link HeosConstants.FAIL}
     */
    public String getSendResult() {
        return response.getEvent().getResult();
    }

    /**
     * Returns the not decoded message of the JSON response
     * from the HEOS system
     *
     * @return the un-decoded message from the HEOS message
     */
    public String getUndecodedMessage() {
        return response.getEvent().getMessage();
    }

    /**
     *
     * @return the command type of the HEOS message
     */
    public String getCommandType() {
        return response.getEvent().getCommandType();
    }

    /**
     *
     * @return the event type of the HEOS message
     */
    public String getEventType() {
        return response.getEvent().getEventType();
    }

    /**
     *
     * @return the HOES system error code
     */
    public String getErrorCode() {
        return response.getEvent().getErrorCode();
    }

    /**
     *
     * @return the HEOS system error message
     */
    public String getErrorMessage() {
        return response.getEvent().getErrorMessage();
    }

    public boolean isCommandUnderProgress() {
        if (response.getEvent().getMessagesMap().get(COM_UNDER_PROCESS).equals(TRUE)) {
            return true;
        } else {
            return false;
        }
    }

    // HEOS Response Messages

    /**
     *
     * @return the undecoded message received from HEOS
     */
    public String getRawResponseMessage() {
        return response.getRawResponseMessage();
    }

    /**
     *
     * @return the Player ID from which the response was received
     */
    public String getPid() {
        return response.getPid();
    }

    public Map<String, String> getMessagesMap() {
        return response.getEvent().getMessagesMap();
    }

    public List<Map<String, String>> getPayloadList() {
        return response.getPayload().getPayloadList();
    }

    public boolean payloadListIsEmpty() {
        if (getPayloadList().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public HeosResponse getHeosResponse() {
        return response;
    }

    public HeosJsonParser getHeosJsonParser() {
        return parser;
    }

}
