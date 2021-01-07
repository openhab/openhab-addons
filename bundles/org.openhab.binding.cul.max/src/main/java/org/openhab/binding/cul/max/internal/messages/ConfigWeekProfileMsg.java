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

import org.openhab.binding.cul.max.internal.messages.constants.MaxCulMsgType;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulWeekdays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Johannes Goehr (johgoe) - Initial contribution
 */
public class ConfigWeekProfileMsg extends BaseMsg {

    final static private int CONFIG_WEEK_PROFILE_LONG_PAYLOAD_LEN = 15; /* in bytes */
    final static private int CONFIG_WEEK_PROFILE_SHORT_PAYLOAD_LEN = 13; /* in bytes */

    private static final Logger logger = LoggerFactory.getLogger(ConfigWeekProfileMsg.class);

    public static final MaxCulWeekProfile DEFAULT_WEEK_PROFILE;

    private MaxCulWeekProfilePart weekProfile;

    private boolean secondHalf;

    static {
        DEFAULT_WEEK_PROFILE = new MaxCulWeekProfile();
        {
            MaxCulWeekProfilePart saturday = new MaxCulWeekProfilePart();
            saturday.setDay(MaxCulWeekdays.SATURDAY);
            MaxCulWeekProfileControlPoint satMorning = new MaxCulWeekProfileControlPoint();
            satMorning.setHour(6);
            satMorning.setMin(0);
            satMorning.setTemperature(17.0f);
            saturday.addControlPoint(satMorning);
            MaxCulWeekProfileControlPoint satNoon = new MaxCulWeekProfileControlPoint();
            satNoon.setHour(22);
            satNoon.setMin(0);
            satNoon.setTemperature(21.0f);
            saturday.addControlPoint(satNoon);
            DEFAULT_WEEK_PROFILE.addWeekProfilePart(saturday);
        }
        {
            MaxCulWeekProfilePart sunday = new MaxCulWeekProfilePart();
            sunday.setDay(MaxCulWeekdays.SUNDAY);
            MaxCulWeekProfileControlPoint sunMorning = new MaxCulWeekProfileControlPoint();
            sunMorning.setHour(6);
            sunMorning.setMin(0);
            sunMorning.setTemperature(17.0f);
            sunday.addControlPoint(sunMorning);
            MaxCulWeekProfileControlPoint sunNoon = new MaxCulWeekProfileControlPoint();
            sunNoon.setHour(22);
            sunNoon.setMin(0);
            sunNoon.setTemperature(21.0f);
            sunday.addControlPoint(sunNoon);
            DEFAULT_WEEK_PROFILE.addWeekProfilePart(sunday);
        }
        {
            MaxCulWeekProfilePart monday = new MaxCulWeekProfilePart();
            monday.setDay(MaxCulWeekdays.MONDAY);
            MaxCulWeekProfileControlPoint monMorning = new MaxCulWeekProfileControlPoint();
            monMorning.setHour(6);
            monMorning.setMin(0);
            monMorning.setTemperature(17.0f);
            monday.addControlPoint(monMorning);
            MaxCulWeekProfileControlPoint monLateMorning = new MaxCulWeekProfileControlPoint();
            monLateMorning.setHour(9);
            monLateMorning.setMin(0);
            monLateMorning.setTemperature(21.0f);
            monday.addControlPoint(monLateMorning);
            MaxCulWeekProfileControlPoint monNoon = new MaxCulWeekProfileControlPoint();
            monNoon.setHour(17);
            monNoon.setMin(0);
            monNoon.setTemperature(17.0f);
            monday.addControlPoint(monNoon);
            MaxCulWeekProfileControlPoint monEvening = new MaxCulWeekProfileControlPoint();
            monEvening.setHour(23);
            monEvening.setMin(0);
            monEvening.setTemperature(21.0f);
            monday.addControlPoint(monEvening);
            DEFAULT_WEEK_PROFILE.addWeekProfilePart(monday);
        }
        {
            MaxCulWeekProfilePart tuesday = new MaxCulWeekProfilePart();
            tuesday.setDay(MaxCulWeekdays.TUESDAY);
            MaxCulWeekProfileControlPoint tueMorning = new MaxCulWeekProfileControlPoint();
            tueMorning.setHour(6);
            tueMorning.setMin(0);
            tueMorning.setTemperature(17.0f);
            tuesday.addControlPoint(tueMorning);
            MaxCulWeekProfileControlPoint tueLateMorning = new MaxCulWeekProfileControlPoint();
            tueLateMorning.setHour(9);
            tueLateMorning.setMin(0);
            tueLateMorning.setTemperature(21.0f);
            tuesday.addControlPoint(tueLateMorning);
            MaxCulWeekProfileControlPoint tueNoon = new MaxCulWeekProfileControlPoint();
            tueNoon.setHour(17);
            tueNoon.setMin(0);
            tueNoon.setTemperature(17.0f);
            tuesday.addControlPoint(tueNoon);
            MaxCulWeekProfileControlPoint tueEvening = new MaxCulWeekProfileControlPoint();
            tueEvening.setHour(23);
            tueEvening.setMin(0);
            tueEvening.setTemperature(21.0f);
            tuesday.addControlPoint(tueEvening);
            DEFAULT_WEEK_PROFILE.addWeekProfilePart(tuesday);
        }
        {
            MaxCulWeekProfilePart wendsday = new MaxCulWeekProfilePart();
            wendsday.setDay(MaxCulWeekdays.WENDSDAY);
            MaxCulWeekProfileControlPoint wenMorning = new MaxCulWeekProfileControlPoint();
            wenMorning.setHour(6);
            wenMorning.setMin(0);
            wenMorning.setTemperature(17.0f);
            wendsday.addControlPoint(wenMorning);
            MaxCulWeekProfileControlPoint wenLateMorning = new MaxCulWeekProfileControlPoint();
            wenLateMorning.setHour(9);
            wenLateMorning.setMin(0);
            wenLateMorning.setTemperature(21.0f);
            wendsday.addControlPoint(wenLateMorning);
            MaxCulWeekProfileControlPoint wenNoon = new MaxCulWeekProfileControlPoint();
            wenNoon.setHour(17);
            wenNoon.setMin(0);
            wenNoon.setTemperature(17.0f);
            wendsday.addControlPoint(wenNoon);
            MaxCulWeekProfileControlPoint wenEvening = new MaxCulWeekProfileControlPoint();
            wenEvening.setHour(23);
            wenEvening.setMin(0);
            wenEvening.setTemperature(21.0f);
            wendsday.addControlPoint(wenEvening);
            DEFAULT_WEEK_PROFILE.addWeekProfilePart(wendsday);
        }
        {
            MaxCulWeekProfilePart thursday = new MaxCulWeekProfilePart();
            thursday.setDay(MaxCulWeekdays.THURSDAY);
            MaxCulWeekProfileControlPoint thuMorning = new MaxCulWeekProfileControlPoint();
            thuMorning.setHour(6);
            thuMorning.setMin(0);
            thuMorning.setTemperature(17.0f);
            thursday.addControlPoint(thuMorning);
            MaxCulWeekProfileControlPoint thuLateMorning = new MaxCulWeekProfileControlPoint();
            thuLateMorning.setHour(9);
            thuLateMorning.setMin(0);
            thuLateMorning.setTemperature(21.0f);
            thursday.addControlPoint(thuLateMorning);
            MaxCulWeekProfileControlPoint thuNoon = new MaxCulWeekProfileControlPoint();
            thuNoon.setHour(17);
            thuNoon.setMin(0);
            thuNoon.setTemperature(17.0f);
            thursday.addControlPoint(thuNoon);
            MaxCulWeekProfileControlPoint thuEvening = new MaxCulWeekProfileControlPoint();
            thuEvening.setHour(23);
            thuEvening.setMin(0);
            thuEvening.setTemperature(21.0f);
            thursday.addControlPoint(thuEvening);
            DEFAULT_WEEK_PROFILE.addWeekProfilePart(thursday);
        }
        {
            MaxCulWeekProfilePart friday = new MaxCulWeekProfilePart();
            friday.setDay(MaxCulWeekdays.FRIDAY);
            MaxCulWeekProfileControlPoint friMorning = new MaxCulWeekProfileControlPoint();
            friMorning.setHour(6);
            friMorning.setMin(0);
            friMorning.setTemperature(17.0f);
            friday.addControlPoint(friMorning);
            MaxCulWeekProfileControlPoint friLateMorning = new MaxCulWeekProfileControlPoint();
            friLateMorning.setHour(9);
            friLateMorning.setMin(0);
            friLateMorning.setTemperature(21.0f);
            friday.addControlPoint(friLateMorning);
            MaxCulWeekProfileControlPoint friNoon = new MaxCulWeekProfileControlPoint();
            friNoon.setHour(17);
            friNoon.setMin(0);
            friNoon.setTemperature(17.0f);
            friday.addControlPoint(friNoon);
            MaxCulWeekProfileControlPoint friEvening = new MaxCulWeekProfileControlPoint();
            friEvening.setHour(23);
            friEvening.setMin(0);
            friEvening.setTemperature(21.0f);
            friday.addControlPoint(friEvening);
            DEFAULT_WEEK_PROFILE.addWeekProfilePart(friday);
        }

    }

    public ConfigWeekProfileMsg(String rawMsg) {
        super(rawMsg);
        logger.debug("{} Payload Len -> {}", this.msgType, this.payload.length);
        if (this.payload.length == CONFIG_WEEK_PROFILE_LONG_PAYLOAD_LEN) {
            secondHalf = false;
            weekProfile = parseWeekProfile();

        } else if (this.payload.length == CONFIG_WEEK_PROFILE_SHORT_PAYLOAD_LEN) {
            secondHalf = true;
            weekProfile = parseWeekProfile();

        } else {
            logger.error("Got {} message with incorrect length!", this.msgType);
        }
    }

    public ConfigWeekProfileMsg(byte msgCount, byte msgFlag, byte groupId, String srcAddr, String dstAddr,
            MaxCulWeekProfilePart weekProfile, boolean secondHalf) {
        super(msgCount, msgFlag, MaxCulMsgType.CONFIG_WEEK_PROFILE, groupId, srcAddr, dstAddr);
        this.weekProfile = weekProfile;
        this.secondHalf = secondHalf;
        super.appendPayload(buildPayload());
    }

    private byte[] buildPayload() {
        int day = weekProfile.getDay().getDayIndexInt();
        int start;
        int end;
        if (secondHalf) {
            start = 0;
            end = 6;
            payload = new byte[CONFIG_WEEK_PROFILE_SHORT_PAYLOAD_LEN];
            payload[0] = 1 << 4;
            payload[0] |= day;
        } else {
            start = 0;
            end = 7;
            payload = new byte[CONFIG_WEEK_PROFILE_LONG_PAYLOAD_LEN];
            payload[0] = 0 << 4;
            payload[0] |= day;
        }
        for (int i = start; i < end; i++) {
            if ((secondHalf ? i + 7 : i) >= weekProfile.getControlPoints().size()) {
                int emptyValue = 0x4520;
                payload[(i * 2) + 2] = (byte) (emptyValue & 0x00FF);
                payload[(i * 2) + 1] = (byte) ((emptyValue & 0xFF00) >> 8);
                continue;
            }
            MaxCulWeekProfileControlPoint controlPoint = weekProfile.getControlPoints().get(secondHalf ? i + 7 : i);
            int value = ((int) (controlPoint.getTemperature() * 2) << 9)
                    | (int) ((controlPoint.getHour() * 60 + controlPoint.getMin()) / 5f);
            payload[(i * 2) + 2] = (byte) (value & 0x00FF);
            payload[(i * 2) + 1] = (byte) ((value & 0xFF00) >> 8);
        }
        return payload;
    }

    private MaxCulWeekProfilePart parseWeekProfile() {
        MaxCulWeekProfilePart result = new MaxCulWeekProfilePart();
        int secondHalfInt = payload[0] >> 4;
        if (secondHalfInt == 1 && !this.secondHalf) {
            logger.warn("Expected CONFIG_WEEK_PROFILE_SHORT_PAYLOAD_LEN");
        }
        if (secondHalfInt == 0 && this.secondHalf) {
            logger.warn("Expected CONFIG_WEEK_PROFILE_LONG_PAYLOAD_LEN");
        }
        int day = payload[0] & 0xF;
        result.setDay(MaxCulWeekdays.getWeekDayFromInt(day));
        // Format of weekprofile: 16 bit integer (high byte first) for every control point, 13 control points for every
        // day
        // each 16 bit integer value is parsed as
        // int time = (value & 0x1FF) * 5;
        // int hour = (time / 60) % 24;
        // int minute = time % 60;
        // int temperature = ((value >> 9) & 0x3F) / 2;
        // #parse weekprofiles for each day
        for (int j = 1; j < (secondHalf ? 12 : 14); j += 2) {
            MaxCulWeekProfileControlPoint controlPoint = new MaxCulWeekProfileControlPoint();
            int currentControlPoint = (int) payload[j];
            currentControlPoint = currentControlPoint << 8;
            currentControlPoint |= (int) (payload[j + 1] & 0xFF);
            int time = (currentControlPoint & 0x1FF) * 5;
            controlPoint.setHour(time / 60 % 24);
            controlPoint.setMin(time % 60);
            controlPoint.setTemperature(((currentControlPoint >> 9) & 0x3F) / 2f);
            if (controlPoint.getHour() == 0 && controlPoint.getMin() == 0) {
                break;
            }
            result.addControlPoint(controlPoint);
        }
        return result;
    }
}
