/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;

/**
 * One idle period of the second version, as the device models it.
 *
 * <p>
 * A period is a named time window on selected weekdays during which the battery is either locked against
 * charging or against discharging. The channel carries the periods as text, one period per line, in the
 * form {@code name:days:mode:active:hh:mm-hh:mm} with the days as a comma separated list:
 *
 * <pre>
 * weekend:saturday,sunday:charge:true:08:00-18:00
 * </pre>
 *
 * @param name the name the device stores the period under
 * @param weekdays the weekday bits the period applies to
 * @param type the idle type, charge or discharge
 * @param active whether the period is used
 * @param start the start of the window
 * @param stop the end of the window
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public record E3DCIdlePeriod(String name, int weekdays, Type type, boolean active, LocalTime start, LocalTime stop) {

    /** The idle type of a period. */
    public enum Type {
        /** Locking the battery against charging. */
        CHARGE(0),
        /** Locking the battery against discharging. */
        DISCHARGE(1);

        private final int code;

        Type(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static @Nullable Type fromCode(int code) {
            for (Type type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            return null;
        }
    }

    /** The weekdays in the order the device counts them. */
    private static final List<String> DAY_NAMES = List.of("monday", "tuesday", "wednesday", "thursday", "friday",
            "saturday", "sunday");

    /** The weekday bit the device uses for a day, with monday as the least significant bit. */
    private static int dayBit(int day) {
        return 1 << day;
    }

    /**
     * Parses one period.
     *
     * @return the period, or {@code null} when the text is not a valid period
     */
    public static @Nullable E3DCIdlePeriod parse(String text) {
        // the time window carries two colons of its own, so the field separators are the first four
        int windowStart = indexOfSeparator(text);
        if (windowStart < 0) {
            return null;
        }
        String[] fields = text.substring(0, windowStart).split(":");
        String window = text.substring(windowStart + 1).trim();
        if (fields.length != 4) {
            return null;
        }
        String name = fields[0].trim();
        if (name.isEmpty() || name.contains("#")) {
            return null;
        }
        Integer weekdays = parseDays(fields[1].trim());
        if (weekdays == null || weekdays == 0) {
            return null;
        }
        Type type = switch (fields[2].trim().toLowerCase(Locale.ROOT)) {
            case "charge" -> Type.CHARGE;
            case "discharge" -> Type.DISCHARGE;
            default -> null;
        };
        if (type == null) {
            return null;
        }
        boolean active = switch (fields[3].trim().toLowerCase(Locale.ROOT)) {
            case "true", "on", "1" -> true;
            case "false", "off", "0" -> false;
            default -> false;
        };
        String[] times = window.split("-");
        if (times.length != 2) {
            return null;
        }
        LocalTime start = parseTime(times[0]);
        LocalTime stop = parseTime(times[1]);
        if (start == null || stop == null) {
            return null;
        }
        return new E3DCIdlePeriod(name, weekdays, type, active, start, stop);
    }

    /**
     * The position of the colon that separates the active flag from the time window, which is the fourth
     * one. Counting is needed because the window carries separators of its own.
     *
     * @return the index, or {@code -1} when the text does not have enough separators
     */
    private static int indexOfSeparator(String text) {
        int found = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ':' && ++found == 4) {
                return i;
            }
        }
        return -1;
    }

    /** The weekday bits of a comma separated day list, or {@code null} when a day is not known. */
    private static @Nullable Integer parseDays(String text) {
        if (text.isEmpty()) {
            return null;
        }
        int mask = 0;
        for (String day : text.split(",")) {
            String name = day.trim().toLowerCase(Locale.ROOT);
            int index = DAY_NAMES.indexOf(name);
            if (index < 0) {
                return null;
            }
            mask |= dayBit(index);
        }
        // an empty list is not a period; the caller rejects a zero mask
        return mask;
    }

    private static @Nullable LocalTime parseTime(String text) {
        String value = text.trim();
        if (!value.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")) {
            return null;
        }
        int separator = value.indexOf(':');
        int hour = Integer.parseInt(value.substring(0, separator));
        int minute = Integer.parseInt(value.substring(separator + 1));
        return LocalTime.of(hour, minute);
    }

    /** The weekday bits of all seven days. */
    public static final int ALL_DAYS = 0x7F;

    /** The name of the period that blocks charging, as written by the switch of the binding. */
    public static final String BLOCK_CHARGE_NAME = "OH_Block_Charge";

    /** The name of the period that blocks discharging, as written by the switch of the binding. */
    public static final String BLOCK_DISCHARGE_NAME = "OH_Block_Discharge";

    /** The start of a window that covers the whole day. */
    public static final LocalTime DAY_START = LocalTime.MIDNIGHT;

    /** The end of a window that covers the whole day; the device counts in minutes and has no 24:00. */
    public static final LocalTime DAY_END = LocalTime.of(23, 59);

    /** A period that locks the given type on all seven days, around the clock. */
    public static E3DCIdlePeriod blocker(Type type) {
        String name = type == Type.CHARGE ? BLOCK_CHARGE_NAME : BLOCK_DISCHARGE_NAME;
        return new E3DCIdlePeriod(name, ALL_DAYS, type, true, DAY_START, DAY_END);
    }

    /**
     * Whether this is the active period of the lock switch of the given type.
     *
     * <p>
     * The convenience switches belong to the binding, not to the raw periods of the device, so they only look
     * at their own entry: {@code OH_Block_Charge} or {@code OH_Block_Discharge}, which has to be active.
     */
    public boolean blocks(Type blocked) {
        return active && name.equals(blocker(blocked).name());
    }

    /** Whether the window ends after it starts, which is what the device and rscp2mqtt require. */
    public boolean hasValidWindow() {
        return start.isBefore(stop);
    }

    /** The container that carries this period in a write, with the fields as rscp2mqtt writes them. */
    public RscpValue toValue() {
        return RscpValue.container(RscpTag.EMS_IDLE_PERIOD_2,
                List.of(RscpValue.cstring(RscpTag.EMS_PERIOD_NAME, name),
                        RscpValue.uchar8(RscpTag.EMS_IDLE_PERIOD_TYPE, type.getCode()),
                        RscpValue.uchar8(RscpTag.EMS_PERIOD_WEEKDAYS, weekdays),
                        RscpValue.bool(RscpTag.EMS_PERIOD_ACTIVE, active),
                        RscpValue.uint32(RscpTag.EMS_PERIOD_START, start.toSecondOfDay()),
                        RscpValue.uint32(RscpTag.EMS_PERIOD_STOP, stop.toSecondOfDay())));
    }

    /** The periods without the one of the given name. */
    public static List<E3DCIdlePeriod> removeByName(List<E3DCIdlePeriod> periods, String name) {
        List<E3DCIdlePeriod> result = new ArrayList<>(periods);
        result.removeIf(period -> period.name().equals(name));
        return result;
    }

    /** The request that replaces the whole list of periods on the device. */
    public static List<RscpValue> toRequest(List<E3DCIdlePeriod> periods) {
        List<RscpValue> children = new ArrayList<>();
        for (E3DCIdlePeriod period : periods) {
            children.add(period.toValue());
        }
        return List.of(RscpValue.container(RscpTag.EMS_REQ_SET_IDLE_PERIODS_2, children));
    }

    /** The text form of this period, which parses back to an equal period. */
    public String toText() {
        StringBuilder days = new StringBuilder();
        for (int day = 0; day < DAY_NAMES.size(); day++) {
            if ((weekdays & dayBit(day)) != 0) {
                if (days.length() > 0) {
                    days.append(',');
                }
                days.append(DAY_NAMES.get(day));
            }
        }
        return name + ":" + days + ":" + (type == Type.CHARGE ? "charge" : "discharge") + ":" + active + ":"
                + format(start) + "-" + format(stop);
    }

    private static String format(LocalTime time) {
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

    /**
     * Parses the text of a whole period list, one period per line. An empty text is an empty list, and a
     * line that cannot be parsed makes the whole text invalid.
     *
     * @return the periods, or {@code null} when any line is not a valid period
     */
    public static @Nullable List<E3DCIdlePeriod> parseAll(String text) {
        List<E3DCIdlePeriod> periods = new ArrayList<>();
        for (String line : text.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            E3DCIdlePeriod period = parse(trimmed);
            if (period == null) {
                return null;
            }
            periods.add(period);
        }
        return periods;
    }

    /** The text of a whole period list, one period per line. */
    public static String toText(List<E3DCIdlePeriod> periods) {
        StringBuilder text = new StringBuilder();
        for (E3DCIdlePeriod period : periods) {
            if (text.length() > 0) {
                text.append('\n');
            }
            text.append(period.toText());
        }
        return text.toString();
    }

    /**
     * The text of the periods a device reported.
     *
     * <p>
     * The response of a request for the periods holds one container per period, each carrying the fields
     * with their own tags. A container that does not name a period is skipped rather than rendered as an
     * empty line.
     */
    public static String toTextFromResponse(RscpValue response) {
        return toText(fromResponse(response));
    }

    /**
     * The periods a device reported, in the order of the response. A container that does not name a period
     * is skipped.
     */
    public static List<E3DCIdlePeriod> fromResponse(RscpValue response) {
        List<E3DCIdlePeriod> periods = new ArrayList<>();
        for (RscpValue container : response.findAll(RscpTag.EMS_IDLE_PERIOD_2)) {
            E3DCIdlePeriod period = fromValue(container);
            if (period != null) {
                periods.add(period);
            }
        }
        return periods;
    }

    /** One period as the device reported it, or {@code null} when a field is missing or unreadable. */
    private static @Nullable E3DCIdlePeriod fromValue(RscpValue container) {
        String name = container.findString(RscpTag.EMS_PERIOD_NAME);
        Number typeCode = container.findNumber(RscpTag.EMS_PERIOD_TYPE);
        if (typeCode == null) {
            // reference clients read the type under the tag of the request side
            typeCode = container.findNumber(RscpTag.EMS_IDLE_PERIOD_TYPE);
        }
        Number weekdayMask = container.findNumber(RscpTag.EMS_PERIOD_WEEKDAYS);
        Number start = container.findNumber(RscpTag.EMS_PERIOD_START);
        Number stop = container.findNumber(RscpTag.EMS_PERIOD_STOP);
        if (name == null || typeCode == null || weekdayMask == null || start == null || stop == null) {
            return null;
        }
        Type type = Type.fromCode(typeCode.intValue());
        if (type == null) {
            return null;
        }
        RscpValue activeValue = container.find(RscpTag.EMS_PERIOD_ACTIVE);
        boolean active = activeValue != null && Boolean.TRUE.equals(activeValue.getValue());
        return new E3DCIdlePeriod(name, weekdayMask.intValue(), type, active,
                LocalTime.ofSecondOfDay(Math.floorMod(start.longValue(), SECONDS_PER_DAY)),
                LocalTime.ofSecondOfDay(Math.floorMod(stop.longValue(), SECONDS_PER_DAY)));
    }

    /** The seconds of a day, used to fold a reported timestamp into a time of day. */
    private static final long SECONDS_PER_DAY = 24L * 60 * 60;
}
