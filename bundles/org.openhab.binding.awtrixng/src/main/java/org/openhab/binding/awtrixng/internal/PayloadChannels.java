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
package org.openhab.binding.awtrixng.internal;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link PayloadChannels} is the table of the channels that are plain fields of a payload DTO. Its factory methods
 * cover the kinds of field AWTRIX NG has, so each channel is declared in one line.
 *
 * @param <T> the payload type
 * @author Keith T. Garner - Initial contribution
 */
@NonNullByDefault
public class PayloadChannels<T> {

    private final Map<String, PayloadChannel<T>> channels = new TreeMap<>();

    public @Nullable PayloadChannel<T> get(String channelId) {
        return channels.get(channelId);
    }

    public Iterable<Map.Entry<String, PayloadChannel<T>>> all() {
        return channels.entrySet();
    }

    /** A channel with a mapping of its own. */
    public void custom(String id, Function<T, @Nullable State> reader, BiPredicate<T, Command> writer) {
        channels.put(id, new PayloadChannel<>(reader, writer));
    }

    public void switchChannel(String id, Function<T, @Nullable Boolean> get, BiConsumer<T, Boolean> set) {
        custom(id, p -> {
            Boolean value = get.apply(p);
            return value == null ? null : OnOffType.from(value);
        }, (p, command) -> {
            if (command instanceof OnOffType onOff) {
                set.accept(p, onOff == OnOffType.ON);
                return true;
            }
            return false;
        });
    }

    public void numberChannel(String id, Function<T, @Nullable Integer> get, BiConsumer<T, Integer> set) {
        numberChannel(id, get, set, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /** A whole number that is only accepted from {@code min} to {@code max}, as AWTRIX NG rejects other values. */
    public void numberChannel(String id, Function<T, @Nullable Integer> get, BiConsumer<T, Integer> set, int min,
            int max) {
        custom(id, p -> {
            Integer value = get.apply(p);
            return value == null ? null : new DecimalType(value);
        }, (p, command) -> {
            if (command instanceof Number number && inRange(number.doubleValue(), min, max)) {
                set.accept(p, number.intValue());
                return true;
            }
            return false;
        });
    }

    /** A decimal number that is only accepted from {@code min} to {@code max}. */
    public void decimalChannel(String id, Function<T, @Nullable Double> get, BiConsumer<T, Double> set, double min,
            double max) {
        custom(id, p -> {
            Double value = get.apply(p);
            return value == null ? null : new DecimalType(value);
        }, (p, command) -> {
            if (command instanceof Number number && inRange(number.doubleValue(), min, max)) {
                set.accept(p, number.doubleValue());
                return true;
            }
            return false;
        });
    }

    /** A percentage that is only accepted from {@code min} to {@code max}. */
    public void percentChannel(String id, Function<T, @Nullable Integer> get, BiConsumer<T, Integer> set, int min,
            int max) {
        custom(id, p -> {
            Integer value = get.apply(p);
            return value == null ? null : new QuantityType<>(value, Units.PERCENT);
        }, (p, command) -> {
            Double percent = Helper.toPercent(command);
            if (percent != null && inRange(percent, min, max)) {
                set.accept(p, (int) Math.round(percent));
                return true;
            }
            return false;
        });
    }

    /**
     * A duration the clock keeps in milliseconds; a plain number is taken as milliseconds. It is only accepted from
     * {@code minMs} to {@code maxMs}.
     */
    public void durationChannel(String id, Function<T, @Nullable Number> get, BiConsumer<T, Long> set, long minMs,
            long maxMs) {
        custom(id, p -> {
            Number millis = get.apply(p);
            return millis == null ? null : new QuantityType<>(millis, MetricPrefix.MILLI(Units.SECOND));
        }, (p, command) -> {
            Double millis = Helper.toMillis(command);
            if (millis != null && inRange(millis, minMs, maxMs)) {
                set.accept(p, Math.round(millis));
                return true;
            }
            return false;
        });
    }

    public void textChannel(String id, Function<T, @Nullable String> get, BiConsumer<T, String> set) {
        textChannel(id, get, set, null);
    }

    /** A text that is only accepted if it is one of the {@code allowed} words, when there are any. */
    public void textChannel(String id, Function<T, @Nullable String> get, BiConsumer<T, String> set,
            @Nullable Set<String> allowed) {
        custom(id, p -> {
            String value = get.apply(p);
            return value == null ? null : new StringType(value);
        }, (p, command) -> {
            if (command instanceof StringType && (allowed == null || allowed.contains(command.toString()))) {
                set.accept(p, command.toString());
                return true;
            }
            return false;
        });
    }

    public void colorChannel(String id, Function<T, @Nullable String> get, BiConsumer<T, String> set) {
        custom(id, p -> Helper.fromHex(get.apply(p)), (p, command) -> {
            if (command instanceof HSBType color) {
                set.accept(p, Helper.toHex(color));
                return true;
            }
            return false;
        });
    }

    private static boolean inRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
}
