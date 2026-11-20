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
package org.openhab.binding.e3dc.internal.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;

/**
 * What the discovery makes of the answers of the device; the functions only read answers and build results.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
final class E3DCDiscovery {

    /** The most batteries a system is assumed to have, which keeps a faulty answer from creating a long list. */
    static final int MAX_BATTERIES = 8;

    /** The most cell modules that are proposed for one system. */
    static final int MAX_MODULES = 32;

    /** The most power meters that are probed; the device reports no count for them. */
    static final int MAX_METERS = 8;

    static final int MAX_WALLBOXES = 8;

    static final String PROPERTY_WALLBOX_INDEX = "wallboxIndex";
    static final String PROPERTY_METER_INDEX = "meterIndex";
    static final String PROPERTY_BATTERY_INDEX = "batteryIndex";
    static final String PROPERTY_MODULE_INDEX = "moduleIndex";

    private E3DCDiscovery() {
    }

    // The answer is read in either of the two shapes it may have: a container listing the battery indexes,
    // or a plain number taken as the count; when it says nothing, battery 0 is tried.
    static List<Integer> batteryCandidates(@Nullable RscpValue answer) {
        RscpValue available = answer != null ? answer.find(RscpTag.BAT_AVAILABLE_BATTERIES) : null;
        if (available == null) {
            return List.of(0);
        }
        if (available.getType() == RscpType.CONTAINER) {
            TreeSet<Integer> indexes = new TreeSet<>();
            for (RscpValue battery : available.findAll(RscpTag.BAT_INDEX)) {
                Number index = battery.asNumber();
                if (index != null && index.intValue() >= 0 && index.intValue() < MAX_BATTERIES) {
                    indexes.add(index.intValue());
                }
            }
            return indexes.isEmpty() ? List.of(0) : List.copyOf(indexes);
        }
        Number count = available.asNumber();
        if (count == null) {
            return List.of(0);
        }
        List<Integer> candidates = new ArrayList<>();
        for (int index = 0; index < Math.min(count.intValue(), MAX_BATTERIES); index++) {
            candidates.add(index);
        }
        return candidates;
    }

    // A battery the device does not have is answered with errors or with nothing.
    static boolean isBatteryAnswered(RscpValue answer) {
        return hasData(answer, RscpTag.BAT_DATA, RscpTag.BAT_INDEX);
    }

    // Besides values, a meter has at least one active phase: an answer that reports the phases as 0 is what a
    // device might send for an index without a meter.
    static boolean isMeterAnswered(RscpValue answer) {
        if (!hasData(answer, RscpTag.PM_DATA, RscpTag.PM_INDEX)) {
            return false;
        }
        Number phases = answer.findNumber(RscpTag.PM_ACTIVE_PHASES);
        return phases == null || phases.intValue() != 0;
    }

    static boolean isWallboxAnswered(RscpValue answer) {
        return hasData(answer, RscpTag.WB_DATA, RscpTag.WB_INDEX);
    }

    // The index the device repeats in its answer is no value, it is there for every index that is asked for.
    private static boolean hasData(RscpValue answer, int dataTag, int indexTag) {
        for (RscpValue data : answer.findAll(dataTag)) {
            if (data.getChildren().stream().anyMatch(child -> !child.isError() && child.getTag() != indexTag)) {
                return true;
            }
        }
        return false;
    }

    static List<Integer> moduleIndexes(RscpValue answer) {
        TreeSet<Integer> indexes = new TreeSet<>();
        for (RscpValue module : answer.findAll(RscpTag.BAT_DCB_INFO)) {
            Number index = module.findNumber(RscpTag.BAT_DCB_INDEX);
            if (!module.isError() && index != null && index.intValue() >= 0 && index.intValue() < MAX_MODULES) {
                indexes.add(index.intValue());
            }
        }
        return List.copyOf(indexes);
    }

    static DiscoveryResult powerPlant(ThingUID bridge) {
        return DiscoveryResultBuilder
                .create(new ThingUID(E3DCBindingConstants.THING_TYPE_POWER_PLANT, bridge, "power-plant"))
                .withBridge(bridge).withLabel("E3/DC Power Plant").build();
    }

    static DiscoveryResult meter(ThingUID bridge, int index) {
        return DiscoveryResultBuilder
                .create(new ThingUID(E3DCBindingConstants.THING_TYPE_POWER_METER, bridge, "meter-" + index))
                .withBridge(bridge).withProperty(PROPERTY_METER_INDEX, index)
                .withRepresentationProperty(PROPERTY_METER_INDEX).withLabel("E3/DC Power Meter " + (index + 1)).build();
    }

    static DiscoveryResult wallbox(ThingUID bridge, int index) {
        return DiscoveryResultBuilder
                .create(new ThingUID(E3DCBindingConstants.THING_TYPE_WALLBOX, bridge, "wallbox-" + index))
                .withBridge(bridge).withProperty(PROPERTY_WALLBOX_INDEX, index)
                .withRepresentationProperty(PROPERTY_WALLBOX_INDEX).withLabel("E3/DC Wallbox " + (index + 1)).build();
    }

    static DiscoveryResult battery(ThingUID bridge, int index) {
        return DiscoveryResultBuilder
                .create(new ThingUID(E3DCBindingConstants.THING_TYPE_BATTERY, bridge, "battery-" + index))
                .withBridge(bridge).withProperty(PROPERTY_BATTERY_INDEX, index)
                .withRepresentationProperty(PROPERTY_BATTERY_INDEX).withLabel("E3/DC Battery " + (index + 1)).build();
    }

    // BAT_DCB_INDEX counts the modules inside one battery, so the battery belongs to the identity of a module.
    // No representation property: neither index identifies a module alone.
    static DiscoveryResult module(ThingUID bridge, int battery, int module) {
        return DiscoveryResultBuilder
                .create(new ThingUID(E3DCBindingConstants.THING_TYPE_BATTERY_MODULE, bridge,
                        "module-" + battery + "-" + module))
                .withBridge(bridge).withProperty(PROPERTY_BATTERY_INDEX, battery)
                .withProperty(PROPERTY_MODULE_INDEX, module)
                .withLabel("E3/DC Battery " + (battery + 1) + " Module " + (module + 1)).build();
    }
}
