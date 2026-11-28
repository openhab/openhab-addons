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
package org.openhab.binding.bydbatterybox.internal.handler;

import static org.openhab.binding.bydbatterybox.internal.BydBatteryBoxBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bydbatterybox.internal.dto.SystemInfo;

/**
 * Decides which tower and module channels exist for the reported topology.
 * <p>
 * The thing type declares tower and module channel groups for the largest topology, so that user interfaces can
 * group the channels. The handler removes the channels of towers and modules that do not exist, and the cell and
 * sensor channels a module type does not have (HVM modules have 16 cells and 8 sensors, the group type 32 and 12).
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
final class ChannelLayout {

    private static final Pattern TOWER_GROUP = Pattern.compile(Pattern.quote(GROUP_TOWER_PREFIX) + "(\\d+)");
    private static final Pattern MODULE_GROUP = Pattern
            .compile(Pattern.quote(GROUP_TOWER_PREFIX) + "(\\d+)" + Pattern.quote(GROUP_MODULE_INFIX) + "(\\d+)");
    private static final Pattern NUMBERED_CHANNEL = Pattern.compile("(" + Pattern.quote(CHANNEL_CELL_VOLTAGE_PREFIX)
            + "|" + Pattern.quote(CHANNEL_CELL_TEMPERATURE_PREFIX) + ")(\\d+)");

    private ChannelLayout() {
    }

    /**
     * @return the ids of the tower and module groups that exist for the topology
     */
    static List<String> groups(SystemInfo info) {
        List<String> groups = new ArrayList<>();
        if (!info.batteryType().hasCellDetails()) {
            return groups;
        }
        for (int tower = 1; tower <= info.towers(); tower++) {
            groups.add(towerGroup(tower));
            for (int module = 1; module <= info.modulesPerTower(); module++) {
                groups.add(moduleGroup(tower, module));
            }
        }
        return groups;
    }

    /**
     * @return {@code false} for channels of towers, modules, cells or sensors that do not exist; {@code true} for all
     *         other channels
     */
    static boolean exists(String groupId, String channelId, SystemInfo info) {
        Matcher module = MODULE_GROUP.matcher(groupId);
        if (module.matches()) {
            return info.batteryType().hasCellDetails() && Integer.parseInt(module.group(1)) <= info.towers()
                    && Integer.parseInt(module.group(2)) <= info.modulesPerTower() && existsInModule(channelId, info);
        }
        Matcher tower = TOWER_GROUP.matcher(groupId);
        if (tower.matches()) {
            return info.batteryType().hasCellDetails() && Integer.parseInt(tower.group(1)) <= info.towers();
        }
        return true;
    }

    private static boolean existsInModule(String channelId, SystemInfo info) {
        Matcher numbered = NUMBERED_CHANNEL.matcher(channelId);
        if (!numbered.matches()) {
            return true;
        }
        int number = Integer.parseInt(numbered.group(2));
        return CHANNEL_CELL_VOLTAGE_PREFIX.equals(numbered.group(1)) ? number <= info.batteryType().getCellsPerModule()
                : number <= info.batteryType().getTemperatureSensorsPerModule();
    }

    static String towerGroup(int tower) {
        return GROUP_TOWER_PREFIX + tower;
    }

    static String moduleGroup(int tower, int module) {
        return GROUP_TOWER_PREFIX + tower + GROUP_MODULE_INFIX + module;
    }
}
