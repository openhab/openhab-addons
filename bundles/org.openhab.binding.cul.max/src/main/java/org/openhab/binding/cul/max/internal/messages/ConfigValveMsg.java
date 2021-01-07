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
package org.openhab.binding.cul.max.internal.messages;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulBoostDuration;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulMsgType;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulWeekdays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Johannes Goehr (johgoe) - Initial contribution
 */
@NonNullByDefault
public class ConfigValveMsg extends BaseMsg {

    private static final int CONFIG_VALVE_PAYLOAD_LEN = 4; /* in bytes */

    private final Logger logger = LoggerFactory.getLogger(ConfigValveMsg.class);

    public static final MaxCulBoostDuration DEFAULT_BOOST_DURATION = MaxCulBoostDuration.MIN_5;
    public static final int DEFAULT_BOOST_VALVE_POSITION = 100;
    public static final int DEFAULT_DECALCIFICATION_HOUR = 11;
    public static final int DEFAULT_MAX_VALVE_SETTING = 100;
    public static final int DEFAULT_VALVE_OFFSET = 0;
    public static final MaxCulWeekdays DEFAULT_DECALCIFICATION_DAY = MaxCulWeekdays.SATURDAY;

    private MaxCulBoostDuration boostDuration = DEFAULT_BOOST_DURATION;
    private int boostValveposition = DEFAULT_BOOST_VALVE_POSITION; // 0-100
    private int decalcificationHour = DEFAULT_DECALCIFICATION_HOUR; // 0-23
    private int maxValveSetting = DEFAULT_MAX_VALVE_SETTING; // 0-100
    private int valveOffset = DEFAULT_VALVE_OFFSET; // 0-100
    private MaxCulWeekdays decalcificationDay = DEFAULT_DECALCIFICATION_DAY;

    public ConfigValveMsg(String rawMsg) throws MaxCulProtocolException {
        super(rawMsg);
        logger.debug("{} Payload Len -> {}", this.msgType, this.payload.length);
        if (this.payload.length == CONFIG_VALVE_PAYLOAD_LEN) {
            this.boostDuration = MaxCulBoostDuration.getBoostDurationFromIdx(payload[0] >> 5);
            byte bTemp = payload[0];
            bTemp &= 0x1F;
            this.boostValveposition = (int) bTemp * 5;
            this.decalcificationDay = MaxCulWeekdays.getWeekDayFromInt(payload[1] >> 5);
            byte dTemp = payload[1];
            dTemp &= 0x1F;
            this.decalcificationHour = (int) dTemp;
            this.maxValveSetting = (int) (payload[2] * 100 / 255f);
            this.valveOffset = (int) (payload[3] * 100 / 255f);
        } else {
            logger.error("Got {} message with incorrect length!", this.msgType);
        }
    }

    private byte[] buildPayload() {
        byte[] payload = new byte[CONFIG_VALVE_PAYLOAD_LEN];
        payload[1] = (byte) (boostDuration.getDurationIndex() << 5 | (int) (boostValveposition / 5f));
        payload[0] = (byte) (decalcificationDay.getDayIndexInt() << 5 | decalcificationHour);
        payload[2] = (byte) (maxValveSetting * 255 / 100f);
        payload[3] = (byte) (valveOffset * 255 / 100f);
        return payload;
    }

    /**
     * Construct with default values
     */
    public ConfigValveMsg(byte msgCount, byte msgFlag, byte groupId, String srcAddr, String dstAddr) {
        super(msgCount, msgFlag, MaxCulMsgType.CONFIG_VALVE, groupId, srcAddr, dstAddr);
        super.appendPayload(buildPayload());
    }

    public ConfigValveMsg(byte msgCount, byte msgFlag, byte groupId, String srcAddr, String dstAddr,
            MaxCulBoostDuration boostDuration, int boostValveposition, MaxCulWeekdays decalcificationDay,
            int decalcificationHour, int maxValveSetting, int valveOffset) {
        super(msgCount, msgFlag, MaxCulMsgType.CONFIG_VALVE, groupId, srcAddr, dstAddr);

        this.boostDuration = boostDuration;
        this.boostValveposition = boostValveposition;
        this.decalcificationDay = decalcificationDay;
        this.decalcificationHour = decalcificationHour;
        this.maxValveSetting = maxValveSetting;
        this.valveOffset = valveOffset;
        super.appendPayload(buildPayload());
    }

    public MaxCulBoostDuration getBoostDuration() {
        return boostDuration;
    }

    public double getBoostValveposition() {
        return boostValveposition;
    }

    public int getDecalcification() {
        return decalcificationHour;
    }

    public MaxCulWeekdays getDecalcificationDay() {
        return decalcificationDay;
    }

    public double getMaxValveSetting() {
        return maxValveSetting;
    }

    public double getValveOffset() {
        return valveOffset;
    }
}
