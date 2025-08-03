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
package org.openhab.binding.iaqualink.internal.v2.api.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Defines possible channels, based on JSON path.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public class Channels {
    public static Collection<ChannelDef> all() {
        return Stream.of(schedules(), swcs()).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static Collection<ChannelDef> schedules() {
        return Stream.of(1, 2, 3, 4, 10).flatMap(id -> scheduleById(id).stream()).collect(Collectors.toList());
    }

    public static Collection<ChannelDef> swcs() {
        return Stream.of(0, 1, 2, 3, 4).flatMap(id -> swcById(id).stream()).collect(Collectors.toList());
    }

    public static Collection<ChannelDef> sensorsForSwc(int swcId) {
        return Stream.of(1, 2, 3).flatMap(id -> sensorById(swcId, id).stream()).collect(Collectors.toList());
    }

    public static Collection<ChannelDef> sensorById(int swcId, int id) {
        return List.of(new ChannelDef(String.format("SWC%d_TempSensor%d", swcId, id),
                String.format("Salt Water Chlorinator %d Temperature Sensor %d", swcId, id), "Number:Temperature",
                "temperature", String.format("$.state.reported.equipment.swc_%d.sns_%d.value", swcId, id),
                String.format(
                        "$.state.reported.equipment.swc_%d.sns_%d[?(@.state == 1 && @.sensor_type == \"Water temp\")]",
                        swcId, id)),
                new ChannelDef(String.format("SWC%d_PhSensor%d", swcId, id),
                        String.format("Salt Water Chlorinator %d Ph Sensor %d", swcId, id), "Number", "chemical",
                        String.format("$.state.reported.equipment.swc_%d.sns_%d.value", swcId, id),
                        String.format(
                                "$.state.reported.equipment.swc_%d.sns_%d[?(@.state == 1 && @.sensor_type == \"Ph\")]",
                                swcId, id)));
    }

    public static Collection<ChannelDef> swcById(int id) {
        Collection<ChannelDef> rv = new ArrayList<>();

        rv.add(new ChannelDef(String.format("SWC%d_Boost", id), String.format("Salt Water Chlorinator %d Boost", id),
                "Switch", "equipment-switch", String.format("$.state.reported.equipment.swc_%d.boost", id)));
        rv.add(new ChannelDef(String.format("SWC%d_Boost_Time", id),
                String.format("Salt Water Chlorinator %d Boost Time", id), "Number:Time", "duration",
                String.format("$.state.reported.equipment.swc_%d.boost_time", id)));

        rv.add(new ChannelDef(String.format("SWC%d_Low", id), String.format("Salt Water Chlorinator %d Low", id),
                "Switch", "equipment-switch", String.format("$.state.reported.equipment.swc_%d.low", id)));
        rv.add(new ChannelDef(String.format("SWC%d_Production", id),
                String.format("Salt Water Chlorinator %d Production", id), "Switch", "readonly-switch",
                String.format("$.state.reported.equipment.swc_%d.production", id)));
        rv.add(new ChannelDef(String.format("SWC%d_Filter_Pump", id),
                String.format("Salt Water Chlorinator %d Filter Pump", id), "Switch", "equipment-switch",
                String.format("$.state.reported.equipment.swc_%d.filter_pump.state", id)));
        rv.add(new ChannelDef(String.format("SWC%d_SaltLevel", id),
                String.format("Salt Water Chlorinator %d Salt Level", id), "Number", "chemical",
                String.format("$.state.reported.equipment.swc_%d.swc", id)));

        rv.addAll(sensorsForSwc(id));

        return rv;
    }

    public static Collection<ChannelDef> scheduleById(int id) {
        return List.of(
                new ChannelDef(String.format("Schedule%d_Active", id),
                        String.format("Salt Water Chlorinator Schedule %s Active", id), "Switch", "readonly-switch",
                        String.format("$.state.reported.schedules.sch%d.active", id)),
                new ChannelDef(String.format("Schedule%d_Enabled", id),
                        String.format("Salt Water Chlorinator Schedule %s Enabled", id), "Switch", "equipment-switch",
                        String.format("$.state.reported.schedules.sch%d.enabled", id)),
                new ChannelDef(String.format("Schedule%d_Start", id),
                        String.format("Salt Water Chlorinator Schedule %s Start", id), "String", "schedule-time",
                        String.format("$.state.reported.schedules.sch%d.timer.start", id)),
                new ChannelDef(String.format("Schedule%d_End", id),
                        String.format("Salt Water Chlorinator Schedule %s End", id), "String", "schedule-time",
                        String.format("$.state.reported.schedules.sch%d.timer.end", id)));
    }

    public static Collection<ChannelDef> appliesToState(Collection<ChannelDef> from, DeviceState newState) {
        return from.stream().filter(def -> def.appliesToState(newState)).collect(Collectors.toList());
    }
}
