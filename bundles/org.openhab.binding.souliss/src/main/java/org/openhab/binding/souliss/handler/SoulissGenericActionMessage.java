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
package org.openhab.binding.souliss.handler;

import java.net.DatagramSocket;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the base Souliss Action Message. All Action Messages derives from
 * this class
 *
 * @author Tonino Fazio
 * @since 1.7.0
 */
public abstract class SoulissGenericActionMessage extends BaseThingHandler {

    /**
     * Result callback interface.
     */
    public interface typicalCommonMethods {

        void setState(PrimitiveType _state);

        // PrimitiveType getState();

        // DateTimeType getLastUpdateTime();

        // void setLastUpdateTime(String string);
    }

    Thing thing;

    private String sTopicNumber;
    private String sTopicVariant;

    private String timestamp;
    private static Logger logger = LoggerFactory.getLogger(SoulissGenericActionMessage.class);

    public SoulissGenericActionMessage(Thing _thing) {
        super(_thing);
        thing = _thing;

        try {
            sTopicNumber = thing.getUID().toString().split(":")[2]
                    .split(SoulissBindingConstants.UUID_NODE_SLOT_SEPARATOR)[0];
            sTopicVariant = _thing.getUID().toString().split(":")[2]
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
    public void thingUpdated(Thing _thing) {
        this.thing = _thing;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.BRIDGE_OFFLINE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @SuppressWarnings("null")
    public DatagramSocket getDatagramSocket() {
        if (getBridge() != null) {
            if (getBridge().getHandler() != null) {
                return ((SoulissGatewayHandler) getBridge().getHandler()).datagramSocket_defaultPort;
            }
        }
        return null;
    }
}