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
package org.openhab.binding.knx.internal.monitor;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;
import io.calimero.KNXFormatException;

/**
 * Turns the text of one monitor filter, as the console and REST take it, into a filter of a
 * {@link TelegramFilter.Builder}. Keys: {@code src dst type dir dpt from to context limit}; lists are separated by
 * commas.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class TelegramFilterText {
    private static final Pattern RELATIVE_TIME = Pattern.compile("-(\\d{1,9})([smh])");
    private static final Pattern DATA_TYPE = Pattern.compile("\\d{1,3}(\\.\\d{3,5})?");

    private TelegramFilterText() {
    }

    /**
     * Adds the filter of one key to the builder.
     *
     * @param key the key in lower case
     * @param clock the clock relative times count back from
     * @param zone the zone of a time of day or a local date and time
     * @throws IllegalArgumentException if the key is unknown or the value cannot be read; its message says why in a
     *             few words, for a time it is the message of the time library
     */
    public static void apply(TelegramFilter.Builder filter, String key, String value, Clock clock, ZoneId zone) {
        switch (key) {
            case "src" ->
                filter.source(each(value, TelegramFilterText::individualAddress).toArray(IndividualAddress[]::new));
            case "dst" ->
                filter.destination(each(value, TelegramFilterText::groupAddress).toArray(GroupAddress[]::new));
            case "type" -> filter.type(each(value, text -> choice(text, TelegramType.values(), "write, read, response"))
                    .toArray(TelegramType[]::new));
            case "dir" -> filter.direction(
                    each(value, text -> choice(text, Direction.values(), "in, out")).toArray(Direction[]::new));
            case "dpt" -> filter.dataType(each(value, TelegramFilterText::dataType).toArray(String[]::new));
            case "from" -> filter.from(time(value, clock, zone));
            case "to" -> filter.to(time(value, clock, zone));
            case "context" -> filter.context(Duration.ofMillis(count(value)));
            case "limit" -> filter.limit(count(value));
            default -> throw new IllegalArgumentException("unknown filter");
        }
    }

    /** {@return the group address of a text such as {@code 1/2/3}} */
    public static GroupAddress groupAddress(String text) {
        try {
            return new GroupAddress(text);
        } catch (KNXFormatException e) {
            throw new IllegalArgumentException("not a group address", e);
        }
    }

    private static <T> List<T> each(String values, Function<String, T> parser) {
        return Arrays.stream(values.split(",")).map(String::trim).map(parser).toList();
    }

    private static <E extends Enum<E>> E choice(String text, E[] constants, String choices) {
        for (E constant : constants) {
            if (constant.name().equalsIgnoreCase(text)) {
                return constant;
            }
        }
        throw new IllegalArgumentException("not one of " + choices);
    }

    private static IndividualAddress individualAddress(String text) {
        try {
            return new IndividualAddress(text);
        } catch (KNXFormatException e) {
            throw new IllegalArgumentException("not an individual address", e);
        }
    }

    private static String dataType(String text) {
        if (!DATA_TYPE.matcher(text).matches()) {
            throw new IllegalArgumentException("not a data type such as 9 or 9.001");
        }
        return text;
    }

    private static int count(String text) {
        try {
            int count = Integer.parseInt(text);
            if (count >= 0) {
                return count;
            }
        } catch (NumberFormatException e) {
            // said below
        }
        throw new IllegalArgumentException("not a number from 0 on");
    }

    /** -10m, -30s, -2h back from now; a time of day today; a local date and time; or an instant ending in Z. */
    private static Instant time(String text, Clock clock, ZoneId zone) {
        try {
            Matcher relative = RELATIVE_TIME.matcher(text);
            if (relative.matches()) {
                long amount = Long.parseLong(relative.group(1));
                Duration back = switch (relative.group(2)) {
                    case "s" -> Duration.ofSeconds(amount);
                    case "m" -> Duration.ofMinutes(amount);
                    default -> Duration.ofHours(amount);
                };
                return clock.instant().minus(back);
            }
            if (text.endsWith("Z")) {
                return Instant.parse(text);
            }
            if (text.contains("T")) {
                return LocalDateTime.parse(text).atZone(zone).toInstant();
            }
            return LocalDate.now(clock.withZone(zone)).atTime(LocalTime.parse(text)).atZone(zone).toInstant();
        } catch (DateTimeException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
