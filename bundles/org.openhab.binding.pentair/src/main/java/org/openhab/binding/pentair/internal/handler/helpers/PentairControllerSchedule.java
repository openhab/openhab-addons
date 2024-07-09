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
package org.openhab.binding.pentair.internal.handler.helpers;

import static org.openhab.binding.pentair.internal.PentairBindingConstants.GROUP_CONTROLLER_SCHEDULE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pentair.internal.actions.PentairControllerActions;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;

/**
 * The {@link PentairControllerSchdule } class stores the schedule details for a given controller schedule.
 *
 * @author Jeff James - initial contribution
 *
 */
@NonNullByDefault
public class PentairControllerSchedule {
    public static final int ID = 0 + +PentairStandardPacket.STARTOFDATA;
    private static final int CIRCUIT = 1 + PentairStandardPacket.STARTOFDATA;
    private static final int STARTH = 2 + PentairStandardPacket.STARTOFDATA;
    private static final int STARTM = 3 + PentairStandardPacket.STARTOFDATA;
    private static final int ENDH = 4 + PentairStandardPacket.STARTOFDATA;
    private static final int ENDM = 5 + PentairStandardPacket.STARTOFDATA;
    private static final int DAYS = 6 + PentairStandardPacket.STARTOFDATA;

    private static final String REGEX_SCHEDULE = "^(NONE|NORMAL|EGGTIMER|ONCEONLY),(\\\\d+),(\\\\d+):(\\\\d+),(\\\\d+):(\\\\d+),([SMTWRFY]+)";
    private static final Pattern PATTERN_SCHEDULE = Pattern.compile(REGEX_SCHEDULE);

    private boolean dirty;

    public enum ScheduleType {
        NONE("None"),
        NORMAL("Normal"),
        EGGTIMER("Egg Timer"),
        ONCEONLY("Once Only"),
        UNKNOWN("Unknown");

        private final String name;

        private ScheduleType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public int id;
    public int circuit;
    public ScheduleType type = ScheduleType.UNKNOWN;
    public int start;
    public int end;
    public int days;

    public PentairControllerSchedule() {
        super();
    }

    public PentairControllerSchedule(PentairStandardPacket p) {
        super();
        parsePacket(p);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean d) {
        this.dirty = d;
    }

    public void parsePacket(PentairStandardPacket p) {
        this.id = p.getByte(ID);
        this.circuit = p.getByte(CIRCUIT);
        this.days = p.getByte(DAYS);

        if (p.getByte(STARTH) == 25) {
            this.type = ScheduleType.EGGTIMER;
            this.start = 0;
            this.end = p.getByte(ENDH) * 60 + p.getByte(ENDM);
        } else if (p.getByte(ENDH) == 26) {
            this.type = ScheduleType.ONCEONLY;
            this.start = p.getByte(STARTH) * 60 + p.getByte(STARTM);
            this.end = 0;
        } else if (circuit == 0) {
            this.type = ScheduleType.NONE;
            this.start = 0;
            this.end = 0;
        } else {
            this.type = ScheduleType.NORMAL;
            this.start = p.getByte(STARTH) * 60 + p.getByte(STARTM);
            this.end = p.getByte(ENDH) * 60 + p.getByte(ENDM);
        }
    }

    public String getScheduleTypeStr() {
        return type.name();
    }

    public boolean setScheduleCircuit(int c) {
        if (circuit == c) {
            return true;
        }

        if (c > 18 || c <= 0) {
            return false;
        }

        this.circuit = c;
        this.dirty = true;

        return true;
    }

    public boolean setScheduleStart(int min) {
        if (min == start) {
            return true;
        }

        if (min > 1440 || min < 0) {
            return false;
        }

        this.start = min;
        this.dirty = true;

        return true;
    }

    public boolean setScheduleEnd(int min) {
        if (min == end) {
            return true;
        }

        if (min > 1440 || min < 0) {
            return false;
        }

        this.end = min;
        this.dirty = true;

        return true;
    }

    public boolean setScheduleType(ScheduleType type) {
        if (this.type == type) {
            return true;
        }

        this.type = type;
        this.dirty = true;

        return true;
    }

    public boolean setScheduleType(String typestring) {
        ScheduleType scheduleType;

        try {
            scheduleType = ScheduleType.valueOf(typestring);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return setScheduleType(scheduleType);
    }

    public boolean setDays(String d) {
        final String dow = "SMTWRFY";

        days = 0;
        for (int i = 0; i <= 6; i++) {
            if (d.indexOf(dow.charAt(i)) >= 0) {
                days |= 1 << i;
            }
        }

        dirty = true;

        return true;
    }

    public @Nullable PentairStandardPacket getWritePacket(int controllerid, int preamble) {
        byte[] packet = { (byte) 0xA5, (byte) preamble, (byte) controllerid, (byte) 0x00 /* source */,
                (byte) PentairControllerActions.ControllerCommand.SAVE_SCHEDULE.send, (byte) 7, (byte) id,
                (byte) circuit, (byte) (start / 60), (byte) (start % 60), (byte) (end / 60), (byte) (end % 60),
                (byte) days };
        PentairStandardPacket p = new PentairStandardPacket(packet);

        switch (type) {
            case NONE:
                p.setByte(STARTH, (byte) 0);
                p.setByte(STARTM, (byte) 0);
                p.setByte(ENDH, (byte) 0);
                p.setByte(ENDM, (byte) 0);
                p.setByte(CIRCUIT, (byte) 0);
                p.setByte(DAYS, (byte) 0);
                break;

            case NORMAL:
                break;

            case ONCEONLY:
                p.setByte(ENDH, (byte) 26);
                p.setByte(ENDM, (byte) 0);
                break;
            case EGGTIMER:
                p.setByte(STARTH, (byte) 25);
                p.setByte(STARTM, (byte) 0);
                p.setByte(DAYS, (byte) 0);
                break;
            case UNKNOWN:
                return null;
        }

        return p;
    }

    public String getDays() {
        final String dow = "SMTWRFY";
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
        Matcher m = PATTERN_SCHEDULE.matcher(schedulestr);

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

        ScheduleType t;
        try {
            t = ScheduleType.valueOf(m.group(1));
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (!setScheduleType(t)) {
            return false;
        }

        dirty = true;

        return true;
    }

    public String getGroupID() {
        String groupID = GROUP_CONTROLLER_SCHEDULE + Integer.toString(id);

        return groupID;
    }
}
