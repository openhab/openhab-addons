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
package org.openhab.binding.freeathomesystem.internal.datamodel;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class FreeAtHomeDatapoint {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeDatapoint.class);

    public static final int DATAPOINT_DIRECTION_UNKNOWN = 0;
    public static final int DATAPOINT_DIRECTION_INPUT = 1;
    public static final int DATAPOINT_DIRECTION_OUTPUT = 2;
    public static final int DATAPOINT_DIRECTION_INPUTOUTPUT = 3;
    public static final int DATAPOINT_DIRECTION_INPUT_AS_OUTPUT = 4;

    public String channelId = "";
    private String datapointId = "";

    int searchForDatapoint(int direction, int neededPairingIDFunction, String channelId,
            JsonObject jsonObjectOfChannel) {
        int resultingDirection = DATAPOINT_DIRECTION_UNKNOWN;
        boolean foundId = false;
        JsonObject localDataponits = null;

        switch (direction) {
            case DATAPOINT_DIRECTION_INPUT: {
                localDataponits = jsonObjectOfChannel.getAsJsonObject("inputs");
                resultingDirection = DATAPOINT_DIRECTION_INPUT;
                break;
            }
            case DATAPOINT_DIRECTION_INPUT_AS_OUTPUT: {
                localDataponits = jsonObjectOfChannel.getAsJsonObject("inputs");
                resultingDirection = DATAPOINT_DIRECTION_OUTPUT;
                break;
            }
            case DATAPOINT_DIRECTION_OUTPUT: {
                localDataponits = jsonObjectOfChannel.getAsJsonObject("outputs");
                resultingDirection = DATAPOINT_DIRECTION_OUTPUT;
                break;
            }
            default: {
                localDataponits = jsonObjectOfChannel.getAsJsonObject("outputs");
                resultingDirection = DATAPOINT_DIRECTION_OUTPUT;
                break;
            }
        }

        Set<String> keys = localDataponits.keySet();

        Iterator<String> iter = keys.iterator();

        // Scan datapoints for pairingID IDs
        while (iter.hasNext() && !foundId) {
            String datapointId = iter.next();

            JsonObject datapointJsonObject = localDataponits.getAsJsonObject(datapointId);

            int pairingIDFunction = datapointJsonObject.get("pairingID").getAsInt();

            if (pairingIDFunction == neededPairingIDFunction) {
                this.channelId = channelId;
                this.datapointId = datapointId;

                logger.debug("Datapoint is found - channel {} - datapoint {}", this.channelId, this.datapointId);

                foundId = true;
            }
        }

        // not founded id add dummy
        if (!foundId) {
            this.channelId = "";
            this.datapointId = "";
            resultingDirection = DATAPOINT_DIRECTION_UNKNOWN;

            logger.debug("Needed datapoint is not found - channel {} - pairingId {}", channelId,
                    neededPairingIDFunction);
        }

        return resultingDirection;
    }

    public String getChannelIdforDatapoint() {
        return channelId;
    }

    public String getDatapointId() {
        return datapointId;
    }
}
