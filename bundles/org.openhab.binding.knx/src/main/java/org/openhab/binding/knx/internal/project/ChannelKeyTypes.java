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
package org.openhab.binding.knx.internal.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Which datapoint types work on which key of which channel type. A key works when the binding maps the type to
 * the value class the key exists for; with another type the binding warns at start and then drops commands.
 * Only types with a sub number count: without one the channel text holds no type. The rows are proved against
 * the binding's type mapping by a test.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class ChannelKeyTypes {

    private enum Value {
        ON_OFF,
        OPEN_CLOSED,
        NUMBER,
        STRING,
        DATE_TIME,
        PERCENT,
        INCREASE_DECREASE,
        UP_DOWN,
        STOP_MOVE,
        HSB
    }

    private record Key(String channelType, String key, Value value) {
    }

    private static final String GA = "ga";

    // single-key channel types first; keys in the order the wizard lists them, the main key first
    private static final List<Key> KEYS = List.of( //
            new Key("switch", GA, Value.ON_OFF), //
            new Key("contact", GA, Value.OPEN_CLOSED), //
            new Key("number", GA, Value.NUMBER), //
            new Key("string", GA, Value.STRING), //
            new Key("datetime", GA, Value.DATE_TIME), //
            new Key("dimmer", "switch", Value.ON_OFF), //
            new Key("dimmer", "position", Value.PERCENT), //
            new Key("dimmer", "increaseDecrease", Value.INCREASE_DECREASE), //
            new Key("rollershutter", "upDown", Value.UP_DOWN), //
            new Key("rollershutter", "stopMove", Value.STOP_MOVE), //
            new Key("rollershutter", "position", Value.PERCENT), //
            new Key("color", "hsb", Value.HSB), //
            new Key("color", "switch", Value.ON_OFF), //
            new Key("color", "position", Value.PERCENT), //
            new Key("color", "increaseDecrease", Value.INCREASE_DECREASE));

    private static final List<String> SINGLE_KEY = List.of("switch", "contact", "number", "string", "datetime");

    private static final Set<String> NOT_ON_OFF = Set.of("1.008", "1.009", "1.010", "1.019", "1.022");
    private static final Set<String> OPEN_CLOSED = Set.of("1.009", "1.019");
    private static final Set<Integer> NUMBER_MAINS = Set.of(2, 5, 6, 7, 8, 9, 12, 13, 14, 17, 18, 20, 21, 22, 29, 229,
            235);
    private static final Set<Integer> STRING_MAINS = Set.of(4, 6, 16, 20, 21, 22, 28, 243, 249, 250, 252, 253, 254);
    private static final Set<Integer> DATE_TIME_MAINS = Set.of(10, 11, 19);
    private static final Set<String> UP_DOWN = Set.of("1.008", "3.008");
    private static final Set<Integer> HSB_MAINS = Set.of(232, 242, 251);

    private ChannelKeyTypes() {
    }

    /** The channel types of the table, single-key types first. */
    public static List<String> channelTypes() {
        return KEYS.stream().map(Key::channelType).distinct().toList();
    }

    /** The keys of a channel type, the main key first; empty for a type the table does not hold. */
    public static List<String> keysOf(String channelType) {
        return KEYS.stream().filter(k -> k.channelType().equals(channelType)).map(Key::key).toList();
    }

    /** Whether the type works on the key of the channel type. */
    public static boolean works(String channelType, String key, KnxDatapointType type) {
        for (Key entry : KEYS) {
            if (entry.channelType().equals(channelType) && entry.key().equals(key)) {
                return gives(entry.value(), type);
            }
        }
        return false;
    }

    /** The single-key channel types whose {@code ga} works for the type, in the order of {@link #channelTypes()}. */
    public static List<String> singleKeyChannelTypesFor(KnxDatapointType type) {
        List<String> types = new ArrayList<>(SINGLE_KEY.size());
        for (String channelType : SINGLE_KEY) {
            if (works(channelType, GA, type)) {
                types.add(channelType);
            }
        }
        return List.copyOf(types);
    }

    private static boolean gives(Value value, KnxDatapointType type) {
        if (type.sub() == null) {
            return false;
        }
        String id = type.toString();
        int main = type.main();
        return switch (value) {
            case ON_OFF -> main == 1 && !NOT_ON_OFF.contains(id);
            case OPEN_CLOSED -> OPEN_CLOSED.contains(id);
            case NUMBER -> "1.022".equals(id) || (NUMBER_MAINS.contains(main) && !"6.020".equals(id));
            case STRING -> STRING_MAINS.contains(main) || "0.000".equals(id);
            case DATE_TIME -> DATE_TIME_MAINS.contains(main);
            case PERCENT -> "5.001".equals(id) || main == 251;
            case INCREASE_DECREASE -> main == 3 && !"3.008".equals(id);
            case UP_DOWN -> UP_DOWN.contains(id);
            case STOP_MOVE -> "1.010".equals(id);
            case HSB -> HSB_MAINS.contains(main);
        };
    }
}
