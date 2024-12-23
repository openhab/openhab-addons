/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.freeathome.internal.datamodel;

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

    enum DatapointDirection {
        UNKNOWN,
        INPUT,
        OUTPUT,
        INPUTOUTPUT,
        INPUT_AS_OUTPUT
    };

    public String channelId = "";
    private String datapointId = "";

    DatapointDirection searchForDatapoint(DatapointDirection direction, int neededPairingIDFunction, String channelId,
            JsonObject jsonObjectOfChannel) {
        DatapointDirection resultingDirection = DatapointDirection.UNKNOWN;
        boolean foundId = false;
        JsonObject localDatapoints = null;

        switch (direction) {
            case INPUT: {
                localDatapoints = jsonObjectOfChannel.getAsJsonObject("inputs");
                resultingDirection = DatapointDirection.INPUT;
                break;
            }
            case INPUT_AS_OUTPUT: {
                localDatapoints = jsonObjectOfChannel.getAsJsonObject("inputs");
                resultingDirection = DatapointDirection.OUTPUT;
                break;
            }
            case OUTPUT: {
                localDatapoints = jsonObjectOfChannel.getAsJsonObject("outputs");
                resultingDirection = DatapointDirection.OUTPUT;
                break;
            }
            default: {
                localDatapoints = jsonObjectOfChannel.getAsJsonObject("outputs");
                resultingDirection = DatapointDirection.OUTPUT;
                break;
            }
        }

        Set<String> keys = localDatapoints.keySet();

        Iterator<String> iter = keys.iterator();

        // Scan datapoints for pairingID IDs
        while (iter.hasNext() && !foundId) {
            String datapointId = iter.next();

            JsonObject datapointJsonObject = localDatapoints.getAsJsonObject(datapointId);

            int pairingIDFunction = datapointJsonObject.get("pairingID").getAsInt();

            if (pairingIDFunction == neededPairingIDFunction) {
                this.channelId = channelId;
                this.datapointId = datapointId;

                logger.debug("Datapoint is found - channel {} - datapoint {}", this.channelId, this.datapointId);

                foundId = true;
            }
        }

        // id not found, add dummy
        if (!foundId) {
            this.channelId = "";
            this.datapointId = "";
            resultingDirection = DatapointDirection.UNKNOWN;

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
