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
 * One idle period of the second version, as the device models it: a named time window on selected weekdays
 * during which the battery is locked against charging or against discharging. The channel carries the
 * periods as text, one period per line, in the form {@code name:days:mode:active:hh:mm-hh:mm} with the days
 * as a comma separated list, for example {@code weekend:saturday,sunday:charge:true:08:00-18:00}.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public record E3DCIdlePeriod(String name, int weekdays, Type type, boolean active, LocalTime start, LocalTime stop) {

    public enum Type {
        CHARGE(0),
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

    private static final List<String> DAY_NAMES = List.of("monday", "tuesday", "wednesday", "thursday", "friday",
            "saturday", "sunday");

    /** The weekday bit the device uses for a day, with monday as the least significant bit. */
    private static int dayBit(int day) {
        return 1 << day;
    }

    /** Parses one period, or returns {@code null} when the text is not a valid period. */
    public static @Nullable E3DCIdlePeriod parse(String text) {
        return parseInternal(text, null);
    }

    /**
     * Parses one period of the given direction, whose text names no direction:
     * {@code name:days:active:hh:mm-hh:mm}.
     */
    public static @Nullable E3DCIdlePeriod parse(String text, Type type) {
        return parseInternal(text, type);
    }

    private static @Nullable E3DCIdlePeriod parseInternal(String text, @Nullable Type fixedType) {
        // the time window carries two colons of its own, so the field separators are counted from the front
        int fields = fixedType == null ? 4 : 3;
        int windowStart = indexOfColon(text, fields);
        if (windowStart < 0) {
            return null;
        }
        String[] parts = text.substring(0, windowStart).split(":");
        String window = text.substring(windowStart + 1).trim();
        if (parts.length != fields) {
            return null;
        }
        String name = parts[0].trim();
        if (name.isEmpty() || name.contains("#")) {
            return null;
        }
        Integer weekdays = parseDays(parts[1].trim());
        if (weekdays == null || weekdays == 0) {
            return null;
        }
        Type type;
        String activeField;
        if (fixedType == null) {
            type = switch (parts[2].trim().toLowerCase(Locale.ROOT)) {
                case "charge" -> Type.CHARGE;
                case "discharge" -> Type.DISCHARGE;
                default -> null;
            };
            if (type == null) {
                return null;
            }
            activeField = parts[3];
        } else {
            type = fixedType;
            activeField = parts[2];
        }
        boolean active = switch (activeField.trim().toLowerCase(Locale.ROOT)) {
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

    /** The colon that separates the given number of fields from what follows, or {@code -1} when there are fewer. */
    private static int indexOfColon(String text, int count) {
        int found = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ':' && ++found == count) {
                return i;
            }
        }
        return -1;
    }

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

    public static final int ALL_DAYS = 0x7F;

    public static final String BLOCK_CHARGE_NAME = "OH_Block_Charge";

    public static final String BLOCK_DISCHARGE_NAME = "OH_Block_Discharge";

    public static final LocalTime DAY_START = LocalTime.MIDNIGHT;

    /** The end of a window that covers the whole day; the device counts in minutes and has no 24:00. */
    public static final LocalTime DAY_END = LocalTime.of(23, 59);

    public static E3DCIdlePeriod blocker(Type type) {
        String name = type == Type.CHARGE ? BLOCK_CHARGE_NAME : BLOCK_DISCHARGE_NAME;
        return new E3DCIdlePeriod(name, ALL_DAYS, type, true, DAY_START, DAY_END);
    }

    /**
     * Whether this is the active period of the lock switch of the given type. The convenience switches belong
     * to the binding, not to the raw periods of the device, so they only look at their own entry:
     * {@code OH_Block_Charge} or {@code OH_Block_Discharge}, which has to be active.
     */
    public boolean blocks(Type blocked) {
        return active && name.equals(blocker(blocked).name());
    }

    /** Whether the window ends after it starts, which is what the device and rscp2mqtt require. */
    public boolean hasValidWindow() {
        return start.isBefore(stop);
    }

    public RscpValue toValue() {
        return RscpValue.container(RscpTag.EMS_IDLE_PERIOD_2,
                List.of(RscpValue.cstring(RscpTag.EMS_PERIOD_NAME, name),
                        RscpValue.uchar8(RscpTag.EMS_IDLE_PERIOD_TYPE, type.getCode()),
                        RscpValue.uchar8(RscpTag.EMS_PERIOD_WEEKDAYS, weekdays),
                        RscpValue.bool(RscpTag.EMS_PERIOD_ACTIVE, active),
                        RscpValue.uint32(RscpTag.EMS_PERIOD_START, start.toSecondOfDay()),
                        RscpValue.uint32(RscpTag.EMS_PERIOD_STOP, stop.toSecondOfDay())));
    }

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
        return name + ":" + daysText() + ":" + (type == Type.CHARGE ? "charge" : "discharge") + ":" + active + ":"
                + windowText();
    }

    /**
     * The text form the channel of this period's direction carries, which names no direction:
     * {@code name:days:active:hh:mm-hh:mm}.
     */
    public String toDirectionalText() {
        return name + ":" + daysText() + ":" + active + ":" + windowText();
    }

    private String daysText() {
        StringBuilder days = new StringBuilder();
        for (int day = 0; day < DAY_NAMES.size(); day++) {
            if ((weekdays & dayBit(day)) != 0) {
                if (days.length() > 0) {
                    days.append(',');
                }
                days.append(DAY_NAMES.get(day));
            }
        }
        return days.toString();
    }

    private String windowText() {
        return format(start) + "-" + format(stop);
    }

    private static String format(LocalTime time) {
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

    /**
     * Parses the text of a whole period list, one period per line; a line that cannot be parsed makes the
     * whole text invalid.
     */
    public static @Nullable List<E3DCIdlePeriod> parseAll(String text) {
        return parseAllInternal(text, null);
    }

    /** Parses the lines of one direction, whose text names no direction. */
    public static @Nullable List<E3DCIdlePeriod> parseAll(String text, Type type) {
        return parseAllInternal(text, type);
    }

    private static @Nullable List<E3DCIdlePeriod> parseAllInternal(String text, @Nullable Type fixedType) {
        List<E3DCIdlePeriod> periods = new ArrayList<>();
        for (String line : text.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            E3DCIdlePeriod period = parseInternal(trimmed, fixedType);
            if (period == null) {
                return null;
            }
            periods.add(period);
        }
        return periods;
    }

    public static String toText(List<E3DCIdlePeriod> periods) {
        StringBuilder text = new StringBuilder();
        for (E3DCIdlePeriod period : periods) {
            appendLine(text, period.toText());
        }
        return text.toString();
    }

    /** The text of the periods of one direction, as the channel of that direction carries them. */
    public static String toText(List<E3DCIdlePeriod> periods, Type type) {
        StringBuilder text = new StringBuilder();
        for (E3DCIdlePeriod period : periods) {
            if (period.type() == type) {
                appendLine(text, period.toDirectionalText());
            }
        }
        return text.toString();
    }

    private static void appendLine(StringBuilder text, String line) {
        if (text.length() > 0) {
            text.append('\n');
        }
        text.append(line);
    }

    /**
     * The text of the periods a device reported. The response holds one container per period; a container
     * that does not name a period is skipped rather than rendered as an empty line.
     */
    public static String toTextFromResponse(RscpValue response) {
        return toText(fromResponse(response));
    }

    /** The text of the periods of one direction a device reported. */
    public static String toTextFromResponse(RscpValue response, Type type) {
        return toText(fromResponse(response), type);
    }

    /**
     * The periods a device reported, in the order of the response; a container that does not name a period
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

    private static final long SECONDS_PER_DAY = 24L * 60 * 60;
}
