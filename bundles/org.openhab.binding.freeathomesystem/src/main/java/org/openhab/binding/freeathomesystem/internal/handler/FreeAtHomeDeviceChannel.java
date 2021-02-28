/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

package org.openhab.binding.freeathomesystem.internal.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * The {@link FreeAtHomeDeviceChannel} holding the information of device channels
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class FreeAtHomeDeviceChannel {
    public static final int DATAPOINT_DIRECTION_INPUT = 1;
    public static final int DATAPOINT_DIRECTION_OUTPUT = 2;

    public String thingTypeOfChannel = "";
    public String channelTypeString = "";
    public String channelId = "";
    public String channelLabel = "";

    public List<String> datapoints = new ArrayList<>();
    public List<String> channels = new ArrayList<>();

    boolean searchForDatapoint(int direction, int neededPairingIDFunction, String channelID,
            JsonObject jsonObjectOfChannel) {
        boolean foundedId = false;
        JsonObject localDataponits = null;

        switch (direction) {
            case DATAPOINT_DIRECTION_INPUT: {
                localDataponits = jsonObjectOfChannel.getAsJsonObject("inputs");
                break;
            }
            case DATAPOINT_DIRECTION_OUTPUT: {
                localDataponits = jsonObjectOfChannel.getAsJsonObject("outputs");
                break;
            }
            default: {
                localDataponits = jsonObjectOfChannel.getAsJsonObject("outputs");
                break;
            }

        }

        Set<String> keys = localDataponits.keySet();

        Iterator<String> iter = keys.iterator();

        // Scan datapoints for pairingID IDs
        while (iter.hasNext() && (foundedId == false)) {
            String nextPairingID = iter.next();

            JsonObject pairingIDObject = localDataponits.getAsJsonObject(nextPairingID);

            int pairingIDFunction = pairingIDObject.get("pairingID").getAsInt();

            if (pairingIDFunction == neededPairingIDFunction) {

                channels.add(channelID);
                datapoints.add(nextPairingID);

                foundedId = true;
            }
        }

        // not founded id add dummy
        if (false == foundedId) {
            channels.add(channelID);
            datapoints.add("----");
        }

        return foundedId;
    }

    public int numberOfDatapoints() {
        return datapoints.size();
    }
}
