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
package org.openhab.binding.e3dc.internal.handler;

import static java.util.Map.entry;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.E3DCChannelRegistry;
import org.openhab.binding.e3dc.internal.E3DCChannelSpec;
import org.openhab.binding.e3dc.internal.config.E3DCBatteryConfiguration;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;

/**
 * One battery of the power plant, read from the battery container addressed by the battery index.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCBatteryHandler extends E3DCThingHandler {

    private static final Map<String, String> ENERGY_SOURCES = Map.of("design-energy", "design-capacity",
            "usable-energy", "usable-capacity");

    /** The channels this handler publishes from the cell modules and the capacities of the answer. */
    private static final Set<String> COMPUTED_CHANNELS = Set.of("usable-remaining-capacity", "usable-remaining-energy");

    /** The values that describe the battery rather than measuring it, published as thing properties. */
    private static final Map<String, E3DCChannelSpec> PROPERTIES = Map.ofEntries(//
            entry("deviceName", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_DEVICE_NAME)), //
            entry("moduleCount", E3DCChannelSpec.in(RscpTag.BAT_DATA, RscpTag.BAT_DCB_COUNT)));

    private final E3DCChannelRegistry registry = E3DCChannelSpec.battery();
    private E3DCBatteryConfiguration config = new E3DCBatteryConfiguration();

    public E3DCBatteryHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(E3DCBatteryConfiguration.class);
        super.initialize();
    }

    @Override
    protected E3DCChannelRegistry getRegistry() {
        return registry;
    }

    @Override
    protected @Nullable Integer getDeviceIndex() {
        return config.batteryIndex;
    }

    @Override
    public void onData(RscpValue response) {
        super.onData(response);
        updateEnergies(response, E3DCBindingConstants.GROUP_PACK, "voltage", ENERGY_SOURCES);
        updateRemaining(response);
        updateProperties(response, PROPERTIES);
    }

    @Override
    protected Set<String> channelsComputedByHandler() {
        return COMPUTED_CHANNELS;
    }

    /**
     * Publishes the capacity that is actually stored in the battery. The usable remaining capacity the device
     * reports counts from the bottom of the usable window, so it is negative while the battery is within its
     * reserve. The cell modules report the capacity they hold themselves, so their sum is preferred; otherwise
     * the reserve, the difference between the design and the usable capacity, is added to the reported value.
     */
    private void updateRemaining(RscpValue response) {
        Double volts = readValue(response, E3DCBindingConstants.GROUP_PACK, "voltage");
        Double stored = storedCapacity(response);
        if (volts == null || volts <= 0 || stored == null) {
            return;
        }
        updateState(channel("usable-remaining-capacity"), new QuantityType<>(stored, Units.AMPERE_HOUR));
        updateState(channel("usable-remaining-energy"),
                new QuantityType<>(stored * volts / 1000.0, Units.KILOWATT_HOUR));
    }

    private @Nullable Double storedCapacity(RscpValue response) {
        RscpValue batteryData = E3DCBatteryModuleHandler.batteryData(response, config.batteryIndex);
        Double fromModules = batteryData != null ? moduleCapacity(batteryData) : null;
        if (fromModules != null) {
            return fromModules;
        }
        Double remaining = readValue(response, E3DCBindingConstants.GROUP_PACK, "usable-remaining-capacity");
        Double design = readValue(response, E3DCBindingConstants.GROUP_PACK, "design-capacity");
        Double usable = readValue(response, E3DCBindingConstants.GROUP_PACK, "usable-capacity");
        if (remaining == null || design == null || usable == null) {
            return null;
        }
        return Math.max(0, remaining + design - usable);
    }

    /** The stored capacity as the sum of the cell modules, or {@code null} unless every module answered. */
    private static @Nullable Double moduleCapacity(RscpValue batteryData) {
        Number expected = batteryData.findNumber(RscpTag.BAT_DCB_COUNT);
        if (expected == null || expected.intValue() <= 0) {
            return null;
        }
        double capacity = 0;
        int answered = 0;
        for (RscpValue module : batteryData.findAll(RscpTag.BAT_DCB_INFO)) {
            Number remaining = module.findNumber(RscpTag.BAT_DCB_REMAINING_CAPACITY);
            if (remaining != null) {
                capacity += remaining.doubleValue();
                answered++;
            }
        }
        return answered == expected.intValue() ? capacity : null;
    }

    private ChannelUID channel(String channelId) {
        return new ChannelUID(getThing().getUID(), E3DCBindingConstants.GROUP_PACK + "#" + channelId);
    }
}
