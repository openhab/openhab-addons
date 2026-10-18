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
package org.openhab.binding.iaqualink.internal.v2.api.mapping;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Defines possible channels, based on JSON path.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public class Channels {

    private Channels() {
    }

    public static Collection<ChannelDef> all() {
        return Stream.of(schedules(), swcs()).flatMap(Collection::stream).toList();
    }

    public static Collection<ChannelDef> schedules() {
        return Stream.of(1, 2, 3, 4, 10).flatMap(id -> scheduleById(id).stream()).toList();
    }

    public static Collection<ChannelDef> swcs() {
        return Stream.of(0, 1, 2, 3, 4).flatMap(id -> swcById(id).stream()).toList();
    }

    public static Collection<ChannelDef> sensorsForSwc(int swcId) {
        return Stream.of(1, 2, 3).flatMap(id -> sensorById(swcId, id).stream()).toList();
    }

    public static Collection<ChannelDef> sensorById(int swcId, int id) {
        ChannelDefBuilder builder = new ChannelDefBuilder("SWC%d_".formatted(swcId),
                "Salt Water Chlorinator %d ".formatted(swcId), "equipment.swc_%d.sns_%d".formatted(swcId, id));

        return List.of(
                builder.build("TempSensor" + id, "Temperature Sensor " + id, "Number:Temperature", "temperature",
                        ".value", "[?(@.state == 1 && @.sensor_type == \"Water temp\")]"),
                builder.build("PhSensor" + id, "Ph Sensor " + id, "Number", "chemical", ".value",
                        "[?(@.state == 1 && @.sensor_type == \"Ph\")]"));
    }

    public static Collection<ChannelDef> swcById(int id) {
        ChannelDefBuilder builder = new ChannelDefBuilder("SWC%d_".formatted(id),
                "Salt Water Chlorinator %d ".formatted(id), "equipment.swc_%d.".formatted(id));

        return Stream.concat(
                Stream.of(builder.build("Boost", "Boost", "Switch", "equipment-switch", "boost"),
                        builder.build("Boost_Time", "Boost Time", "Number:Time", "duration", "boost_time"),
                        builder.build("Low", "Low", "Switch", "equipment-switch", "low"),
                        builder.build("Production", "Production", "Switch", "equipment-switch", "production"),
                        builder.build("Filter_Pump", "Filter Pump", "Switch", "readonly-switch", "filter_pump.state"),
                        builder.build("SaltLevel", "Salt Level", "Number", "chemical", "swc")),
                sensorsForSwc(id).stream()).toList();
    }

    public static Collection<ChannelDef> scheduleById(int id) {
        ChannelDefBuilder builder = new ChannelDefBuilder("Schedule%d_".formatted(id),
                "Salt Water Chlorinator Schedule %d ".formatted(id), "schedules.sch%d.".formatted(id));

        return List.of(builder.build("Active", "Active", "Switch", "readonly-switch", "active"),
                builder.build("Enabled", "Enabled", "Switch", "equipment-switch", "enabled"),
                builder.build("Start", "Start", "String", "schedule-time", "timer.start"),
                builder.build("End", "End", "String", "schedule-time", "timer.end"));
    }

    public static Collection<ChannelDef> appliesToState(Collection<ChannelDef> from, DeviceState newState) {
        return from.stream().filter(def -> def.appliesToState(newState)).toList();
    }

    private record ChannelDefBuilder(String baseId, String baseLabel, String basePath) {

        ChannelDef build(String idSuffix, String labelSuffix, String itemType, String typeId, String pathSuffix) {
            return build(idSuffix, labelSuffix, itemType, typeId, pathSuffix, null);
        }

        ChannelDef build(String idSuffix, String labelSuffix, String itemType, String typeId, String pathSuffix,
                @Nullable String appliesPathSuffix) {
            return new ChannelDef(baseId + idSuffix, baseLabel + labelSuffix, itemType, typeId, basePath + pathSuffix,
                    appliesPathSuffix != null ? basePath + appliesPathSuffix : null);
        }
    }
}
