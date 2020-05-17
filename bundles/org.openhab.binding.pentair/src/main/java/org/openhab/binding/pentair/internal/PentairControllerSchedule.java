/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.pentair.internal;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class for the pentair controller schedules.
 *
 * @author Jeff James - initial contribution
 *
 */
@NonNullByDefault
public class PentairControllerSchedule {
    public static final int ID = 0;
    public static final int CIRCUIT = 1;
    public static final int STARTH = 2;
    public static final int STARTM = 3;
    public static final int ENDH = 4;
    public static final int ENDM = 5;
    public static final int DAYS = 6;

    public static final int SCHEDULETYPE_NONE = 0;
    public static final int SCHEDULETYPE_NORMAL = 1;
    public static final int SCHEDULETYPE_EGGTIMER = 2;
    public static final int SCHEDULETYPE_ONCEONLY = 3;

    private boolean dirty;

    //@formatter:off
    public static final Map<Integer, @Nullable String> SCHEDULETYPE = MapUtils.mapOf(
            SCHEDULETYPE_NONE, "NONE",
            SCHEDULETYPE_NORMAL, "NORMAL",
            SCHEDULETYPE_EGGTIMER, "EGGTIMER",
            SCHEDULETYPE_ONCEONLY, "ONCEONLY");
    public static final Map<@Nullable String, Integer> SCHEDULETYPE_INV = MapUtils.invertMap(SCHEDULETYPE);
    //@formatter:on

    public int id;
    public int circuit;
    public int type;

    public int start;
    public int end;

    public int days;

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean d) {
        dirty = d;
    }

    public void parsePacket(PentairPacket p) {
        id = p.getByte(ID);
        circuit = p.getByte(CIRCUIT);
        days = p.getByte(DAYS);

        if (p.getByte(STARTH) == 25) {
            type = SCHEDULETYPE_EGGTIMER;
            start = 0;
            end = p.getByte(ENDH) * 60 + p.getByte(ENDM);
        } else if (p.getByte(ENDH) == 26) {
            type = SCHEDULETYPE_ONCEONLY;
            start = p.getByte(STARTH) * 60 + p.getByte(STARTM);
            end = 0;
        } else if (circuit == 0) {
            type = SCHEDULETYPE_NONE;
            start = 0;
            end = 0;
        } else {
            type = SCHEDULETYPE_NORMAL;
            start = p.getByte(STARTH) * 60 + p.getByte(STARTM);
            end = p.getByte(ENDH) * 60 + p.getByte(ENDM);
        }
    }

    @Nullable
    public String getScheduleTypeStr() {
        String str = SCHEDULETYPE.get(type);

        return str;
    }

    public boolean setScheduleCircuit(int c) {
        if (circuit == c) {
            return true;
        }

        if (c > 18 | c <= 0) {
            return false;
        }

        circuit = c;
        dirty = true;

        return true;
    }

    public boolean setScheduleStart(int min) {
        if (min == start) {
            return true;
        }

        if (min > 1440 | min < 0) {
            return false;
        }

        start = min;
        dirty = true;

        return true;
    }

    public boolean setScheduleEnd(int min) {
        if (min == end) {
            return true;
        }

        if (min > 1440 | min < 0) {
            return false;
        }

        end = min;
        dirty = true;

        return true;
    }

    public boolean setScheduleType(int t) {
        if (t == type) {
            return true;
        }

        if (t < 0 | t > 3) {
            return false;
        }

        type = t;
        dirty = true;

        return true;
    }

    public boolean setScheduleType(String typestring) {
        @Nullable
        Integer type;

        type = SCHEDULETYPE_INV.get(typestring);
        if (type == null) {
            return false;
        }
        return setScheduleType(type);
    }

    public boolean setDays(String d) {
        String dow = "SMTWRFY";

        days = 0;
        for (int i = 0; i <= 6; i++) {
            if (d.indexOf(dow.charAt(i)) >= 0) {
                days |= 1 << i;
            }
        }

        dirty = true;

        return true;
    }

    public PentairPacket getWritePacket(int controllerid, int preamble) {
        byte[] packet = { (byte) 0xA5, (byte) preamble, (byte) controllerid, (byte) 0x00 /* source */, (byte) 0x91,
                (byte) 7, (byte) id, (byte) circuit, (byte) (start / 60), (byte) (start % 60), (byte) (end / 60),
                (byte) (end % 60), (byte) days };
        PentairPacket p = new PentairPacket(packet);

        switch (type) {
            case SCHEDULETYPE_NONE:
                p.setByte(STARTH, (byte) 0);
                p.setByte(STARTM, (byte) 0);
                p.setByte(ENDH, (byte) 0);
                p.setByte(ENDM, (byte) 0);
                p.setByte(CIRCUIT, (byte) 0);
                p.setByte(DAYS, (byte) 0);
                break;

            case SCHEDULETYPE_NORMAL:
                break;

            case SCHEDULETYPE_ONCEONLY:
                p.setByte(ENDH, (byte) 26);
                p.setByte(ENDM, (byte) 0);
                break;
            case SCHEDULETYPE_EGGTIMER:
                p.setByte(STARTH, (byte) 25);
                p.setByte(STARTM, (byte) 0);
                p.setByte(DAYS, (byte) 0);
                break;
        }

        return p;
    }

    public String getDays() {
        String dow = "SMTWRFY";
        String str = "";

        for (int i = 0; i <= 6; i++) {
            if ((((days >> i) & 0x01)) == 0x01) {
                str += dow.charAt(i);
            }
        }

        return str;
    }

    @Override
    public String toString() {
        String str = String.format("%s,%d,%02d:%02d,%02d:%02d,%s", getScheduleTypeStr(), circuit, start / 60,
                start % 60, end / 60, end % 60, getDays());

        return str;
    }

    public boolean fromString(String str) {
        String schedulestr = str.toUpperCase();

        Pattern ptn = Pattern
                .compile("^(NONE|NORMAL|EGGTIMER|ONCEONLY),(\\d+),(\\d+):(\\d+),(\\d+):(\\d+),([SMTWRFY]+)");
        Matcher m = ptn.matcher(schedulestr);

        if (!m.find()) {
            return false;
        }

        if (!setScheduleCircuit(Integer.parseUnsignedInt(m.group(2)))) {
            return false;
        }

        int min = Integer.parseUnsignedInt(m.group(3)) * 60 + Integer.parseUnsignedInt(m.group(4));
        if (!setScheduleStart(min)) {
            return false;
        }

        min = Integer.parseUnsignedInt(m.group(5)) * 60 + Integer.parseUnsignedInt(m.group(6));
        if (!setScheduleEnd(min)) {
            return false;
        }

        if (!setDays(m.group(7))) {
            return false;
        }

        Integer t = SCHEDULETYPE_INV.get(m.group(1));
        if (t == null) {
            return false;
        }
        if (!setScheduleType(t)) {
            return false;
        }

        dirty = true;

        return true;
    }

}
