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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The {@link HeosResponsePayload} provides the Payload message
 * of the HEOS JSON message.
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosResponsePayload {

    private List<Map<String, String>> payload = new ArrayList<>();
    private List<List<Map<String, String>>> groupMembers = new ArrayList<>();

    @Override
    public String toString() {
        return payloadToString();
    }

    private String payloadToString() {
        String returnString = "";
        for (int i = 0; i < payload.size(); i++) {
            returnString = returnString + "\n\nPayload: " + (i + 1);
            for (String key : payload.get(i).keySet()) {
                returnString = returnString + "\n" + key + ":\t " + payload.get(i).get(key);
            }
        }
        return returnString;
    }

    /**
     * This returns a list with HashMaps which contain the
     * single pairs of information.
     * Each HashMap represent a single JSON Array which was
     * received by the HEOS response. For details for the
     * expected response revere to the HEOS specification
     *
     * @return a list with HashMaps for each JSON Array
     */
    public List<Map<String, String>> getPayloadList() {
        return payload;
    }

    public void setPayload(List<Map<String, String>> payload) {
        this.payload = payload;
    }

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
        return groupMembers;
    }

    public void setPlayerList(List<List<Map<String, String>>> player) {
        this.groupMembers = player;
    }
}
