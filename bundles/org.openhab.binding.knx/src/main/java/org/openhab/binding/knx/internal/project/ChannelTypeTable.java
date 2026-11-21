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

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Which channel type, and which configuration key of it, suits a datapoint type. The one place of this table.
 * Only rows that the cross-check test proves against the binding's own type mapping are in it.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class ChannelTypeTable {

    /** The channel type and the configuration key the text goes under. */
    record Row(String channelType, String key) {
    }

    private static final String GA = "ga";
    private static final Row SWITCH = new Row("switch", GA);
    private static final Row CONTACT = new Row("contact", GA);
    private static final Row NUMBER = new Row("number", GA);
    private static final Row STRING = new Row("string", GA);
    private static final Row DATETIME = new Row("datetime", GA);

    // rows of one datapoint type
    private static final Map<String, Row> EXACT = Map.of( //
            "1.008", new Row("rollershutter", "upDown"), //
            "1.009", CONTACT, //
            "1.019", CONTACT, //
            "232.600", new Row("color", "hsb"), //
            "3.007", new Row("dimmer", "increaseDecrease"));

    // rows of a main number, for every sub number except those listed in EXCLUDED
    private static final Map<Integer, Row> BY_MAIN = Map.ofEntries( //
            Map.entry(1, SWITCH), //
            Map.entry(5, NUMBER), Map.entry(6, NUMBER), Map.entry(7, NUMBER), Map.entry(8, NUMBER),
            Map.entry(9, NUMBER), Map.entry(12, NUMBER), Map.entry(13, NUMBER), Map.entry(14, NUMBER),
            Map.entry(17, NUMBER), Map.entry(18, NUMBER), Map.entry(29, NUMBER), //
            Map.entry(16, STRING), Map.entry(28, STRING), //
            Map.entry(10, DATETIME), Map.entry(11, DATETIME), Map.entry(19, DATETIME));

    // sub numbers the binding maps to other types than their row's channel accepts
    private static final Set<String> EXCLUDED = Set.of("1.010", "1.022", "6.020");

    private ChannelTypeTable() {
    }

    /** The row for a datapoint type with a sub number, {@code null} when the table has none. */
    static @Nullable Row lookup(KnxDatapointType type) {
        Integer sub = type.sub();
        if (sub == null) {
            return null;
        }
        String id = type.toString();
        Row exact = EXACT.get(id);
        if (exact != null) {
            return exact;
        }
        return EXCLUDED.contains(id) ? null : BY_MAIN.get(type.main());
    }
}
