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
package org.openhab.binding.souliss.handler;

import java.net.DatagramSocket;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 *
 *         This class implements the base Souliss Action Message. All Action Messages derives from
 *         this class
 *
 * @author Tonino Fazio - @since 1.7.0
 */

@NonNullByDefault
public abstract class SoulissGenericActionMessage extends BaseThingHandler {

    /**
     * Result callback interface.
     */

    /*
     * public interface typicalCommonMethods {
     *
     * void setState(PrimitiveType state);
     *
     * // PrimitiveType getState();
     *
     * // DateTimeType getLastUpdateTime();
     *
     * // void setLastUpdateTime(String string);
     * }
     */

    Thing thingGenActMsg;

    private String sTopicNumber = "";
    private String sTopicVariant = "";

    private String timestamp = "";
    private final Logger logger = LoggerFactory.getLogger(SoulissGenericActionMessage.class);

    public SoulissGenericActionMessage(Thing pThing) {
        super(pThing);
        thingGenActMsg = pThing;

        try {
            sTopicNumber = thingGenActMsg.getUID().toString().split(":")[2]
                    .split(SoulissBindingConstants.UUID_NODE_SLOT_SEPARATOR)[0];
            sTopicVariant = thingGenActMsg.getUID().toString().split(":")[2]
                    .split(SoulissBindingConstants.UUID_NODE_SLOT_SEPARATOR)[1];
        } catch (Exception e) {
            logger.debug("Item Definition Error. Use ex:'souliss:t11:nodeNumber-slotNumber'");
        }
    }

    /**
     * @return the Topic Number
     */
    public String getTopicNumber() {
        return sTopicNumber;
    }

    /**
     * @param the Topic Variant
     */
    public String getTopicVariant() {
        return sTopicVariant;
    }

    public DateTimeType getLastUpdateTime() {
        if (timestamp != null) {
            return DateTimeType.valueOf(timestamp);
        } else {
            return null;
        }
    }

    public void setUpdateTimeNow() {
        timestamp = getTimestamp();
    }

    /**
     * Create a time stamp as "yyyy-MM-dd'T'HH:mm:ssz"
     *
     * @return String timestamp
     */
    private static String getTimestamp() {
        // Pattern : yyyy-MM-dd'T'HH:mm:ssz
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");
        Date n = new Date();
        return sdf.format(n.getTime());
    }

    @Override
    public void thingUpdated(Thing thing) {
        this.thingGenActMsg = thing;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.BRIDGE_OFFLINE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Nullable
    public DatagramSocket getDatagramSocket() {
        if (getBridge() != null) {
            if (getBridge().getHandler() != null) {
                return ((SoulissGatewayHandler) getBridge().getHandler()).datagramSocketDefaultPort;
            }
        }
        return null;
    }
}
