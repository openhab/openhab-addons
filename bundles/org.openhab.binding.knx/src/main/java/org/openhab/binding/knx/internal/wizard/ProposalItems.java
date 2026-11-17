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
package org.openhab.binding.knx.internal.wizard;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.dpt.DPTUnits;
import org.openhab.binding.knx.internal.project.KnxDatapointType;
import org.openhab.binding.knx.internal.project.KnxProposal.Channel;
import org.openhab.core.types.util.UnitUtils;

/**
 * The Item the wizard proposes for a channel: its type, the types that may be chosen instead, and tags and a
 * category only where the data type leaves no doubt about what is measured.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class ProposalItems {

    public static final String MEASUREMENT = "Measurement";

    /**
     * @param type the proposed Item type, the first of the allowed ones
     * @param allowedTypes the Item types that may be chosen
     * @param category the icon, {@code null} when none
     */
    public record Item(String type, List<String> allowedTypes, @Nullable String category, List<String> tags) {

        public Item {
            allowedTypes = List.copyOf(allowedTypes);
            tags = List.copyOf(tags);
        }
    }

    private record Row(String tag, String category, Set<String> dataTypes) {
    }

    private static final String NUMBER = "number";

    // as device.xml names them
    private static final Map<String, String> ITEM_TYPES = Map.of("switch", "Switch", "contact", "Contact", NUMBER,
            "Number", "string", "String", "datetime", "DateTime", "dimmer", "Dimmer", "rollershutter", "Rollershutter",
            "color", "Color");

    private static final List<Row> ROWS = List.of( //
            new Row("Temperature", "temperature", Set.of("9.001", "9.027", "14.068")),
            new Row("Humidity", "humidity", Set.of("9.007")), //
            new Row("CO2", "carbondioxide", Set.of("9.008")), //
            new Row("Illuminance", "sun", Set.of("9.004")), //
            new Row("Pressure", "pressure", Set.of("9.006", "14.058")), //
            new Row("Wind", "wind", Set.of("9.005", "9.028")), //
            new Row("Power", "energy", Set.of("9.024", "14.056")), //
            new Row("Energy", "energy", Set.of("13.010", "13.013", "29.010")), //
            new Row("Current", "energy", Set.of("7.012", "9.021", "14.019")), //
            new Row("Voltage", "energy", Set.of("9.020", "14.027")));

    private ProposalItems() {
    }

    /** The Item of a proposed channel, by its channel type and the data type of its first part. */
    public static @Nullable Item of(Channel channel) {
        String channelType = channel.channelType();
        if (channelType == null) {
            return null;
        }
        KnxDatapointType dataType = channel.parts().isEmpty() ? null : channel.parts().get(0).dataType();
        List<String> allowed = allowedTypes(channelType, dataType);
        if (allowed.isEmpty()) {
            return null;
        }
        return new Item(allowed.get(0), allowed, category(channelType, dataType), tags(channelType, dataType));
    }

    /**
     * The Item types for a channel type: its type in {@code device.xml}; for {@code number} first
     * {@code Number:<dimension>} when the binding's unit for the data type has a dimension, then {@code Number}.
     * Empty for a channel type the wizard does not offer.
     */
    public static List<String> allowedTypes(String channelType, @Nullable KnxDatapointType dataType) {
        String itemType = ITEM_TYPES.get(channelType);
        if (itemType == null) {
            return List.of();
        }
        if (NUMBER.equals(channelType) && dataType != null) {
            Unit<?> unit = UnitUtils.parseUnit(DPTUnits.getUnitForDpt(dataType.toString()));
            String dimension = unit == null ? null : UnitUtils.getDimensionName(unit);
            if (dimension != null) {
                return List.of(itemType + ":" + dimension, itemType);
            }
        }
        return List.of(itemType);
    }

    /**
     * The Item types of a proposed channel for each of its {@code allowedChannelTypes}, in their order, by the data
     * type of its first part: what the Item type may be once that channel type is chosen.
     */
    public static Map<String, List<String>> allowedTypesByChannelType(Channel channel) {
        KnxDatapointType dataType = channel.parts().isEmpty() ? null : channel.parts().get(0).dataType();
        Map<String, List<String>> byType = new LinkedHashMap<>();
        for (String channelType : channel.allowedChannelTypes()) {
            List<String> allowed = allowedTypes(channelType, dataType);
            if (!allowed.isEmpty()) {
                byType.put(channelType, allowed);
            }
        }
        return byType;
    }

    /** The Item type the binding gives the channel, {@code null} for a channel type the wizard does not offer. */
    public static @Nullable String itemType(String channelType, @Nullable KnxDatapointType dataType) {
        List<String> allowed = allowedTypes(channelType, dataType);
        return allowed.isEmpty() ? null : allowed.get(0);
    }

    public static List<String> tags(String channelType, @Nullable KnxDatapointType dataType) {
        Row row = row(channelType, dataType);
        return row == null ? List.of() : List.of(MEASUREMENT, row.tag());
    }

    public static @Nullable String category(String channelType, @Nullable KnxDatapointType dataType) {
        Row row = row(channelType, dataType);
        return row == null ? null : row.category();
    }

    private static @Nullable Row row(String channelType, @Nullable KnxDatapointType dataType) {
        if (!NUMBER.equals(channelType) || dataType == null || dataType.sub() == null) {
            return null;
        }
        String id = dataType.toString();
        for (Row row : ROWS) {
            if (row.dataTypes().contains(id)) {
                return row;
            }
        }
        return null;
    }
}
