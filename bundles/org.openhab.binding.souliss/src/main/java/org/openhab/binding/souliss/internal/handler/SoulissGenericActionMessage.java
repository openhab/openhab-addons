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
package org.openhab.binding.souliss.internal.handler;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the base Souliss Action Message. All Action Messages derives from
 * this class
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 * @author Tonino Fazio - @since 1.7.0
 */

@NonNullByDefault
public abstract class SoulissGenericActionMessage extends BaseThingHandler {

    Thing thingGenActMsg;

    private String sTopicNumber = "";
    private String sTopicVariant = "";

    private String timestamp = "";
    private final Logger logger = LoggerFactory.getLogger(SoulissGenericActionMessage.class);

    protected SoulissGenericActionMessage(Thing pThing) {
        super(pThing);
        thingGenActMsg = pThing;

        try {
            var cfg = thingGenActMsg.getConfiguration();
            var props = cfg.getProperties();
            var pTopicNumber = props.get("number");
            var pTopicVariant = props.get("number");
            if (pTopicNumber != null) {
                sTopicNumber = pTopicNumber.toString();
            }
            if (pTopicVariant != null) {
                sTopicVariant = pTopicVariant.toString();
            }
        } catch (Exception e) {
            logger.debug("Item Definition Error. Use ex:'souliss:t11:thing_id'");
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
        return DateTimeType.valueOf(timestamp);
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
        var sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");
        var n = new Date();
        return sdf.format(n.getTime());
    }

    @Override
    public void thingUpdated(Thing thing) {
        this.thingGenActMsg = thing;
    }
}
