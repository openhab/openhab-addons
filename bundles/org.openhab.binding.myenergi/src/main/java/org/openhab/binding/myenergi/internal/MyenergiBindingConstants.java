/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.myenergi.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MyenergiBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author Rene Scherer - Initial contribution
 * @author Stephen Cook - Eddi Support
 */
@NonNullByDefault
public class MyenergiBindingConstants {

    private static final String BINDING_ID = "myenergi";

    public static final String PROP_SERIAL_NUMBER = "serialNumber";
    public static final String PROP_FIRMWARE_VERSION = "firmwareVersion";
    public static final String PROP_NUMBER_OF_PHASES = "numberOfPhases";

    // List all Thing Type UIDs, related to the binding
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_ZAPPI = new ThingTypeUID(BINDING_ID, "zappi");
    public static final ThingTypeUID THING_TYPE_EDDI = new ThingTypeUID(BINDING_ID, "eddi");
    public static final ThingTypeUID THING_TYPE_HARVI = new ThingTypeUID(BINDING_ID, "harvi");

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_BRIDGE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(THING_TYPE_ZAPPI, THING_TYPE_EDDI, THING_TYPE_HARVI));

    // Zappi Channel Names
    public static final String ZAPPI_CHANNEL_LAST_UPDATED_TIME = "last-updated-time";
    public static final String ZAPPI_CHANNEL_SUPPLY_VOLTAGE = "supply-voltage";
    public static final String ZAPPI_CHANNEL_SUPPLY_FREQUENCY = "supply-frequency";
    public static final String ZAPPI_CHANNEL_CHARGER_IS_LOCKED = "charger-is-locked";
    public static final String ZAPPI_CHANNEL_CHARGER_LOCKED_WHEN_PLUGGED = "charger-locked-when-plugged";
    public static final String ZAPPI_CHANNEL_CHARGER_LOCKED_WHEN_UNPLUGGED = "charger-locked-when-unplugged";
    public static final String ZAPPI_CHANNEL_CHARGE_WHEN_LOCKED = "charge-when-locked";
    public static final String ZAPPI_CHANNEL_CHARGING_MODE = "charging-mode";
    public static final String ZAPPI_CHANNEL_CHARGER_STATUS = "charger-status";
    public static final String ZAPPI_CHANNEL_PLUG_STATUS = "plug-status";
    public static final String ZAPPI_CHANNEL_LAST_COMMAND_STATUS = "last-command-status";
    public static final String ZAPPI_CHANNEL_DIVERTER_PRIORITY = "diverter-priority";
    public static final String ZAPPI_CHANNEL_MINIMUM_GREEN_LEVEL = "minimum-green-level";
    public static final String ZAPPI_CHANNEL_GRID_POWER = "grid-power";
    public static final String ZAPPI_CHANNEL_GENERATED_POWER = "generated-power";
    public static final String ZAPPI_CHANNEL_DIVERTED_POWER = "diverted-power";
    public static final String ZAPPI_CHANNEL_CONSUMED_POWER = "consumed-power";
    public static final String ZAPPI_CHANNEL_CHARGE_ADDED = "charge-added";
    public static final String ZAPPI_CHANNEL_SMART_BOOST_DURATION = "smart-boost-duration";
    public static final String ZAPPI_CHANNEL_SMART_BOOST_CHARGE = "smart-boost-charge";
    public static final String ZAPPI_CHANNEL_TIMED_BOOST_DURATION = "timed-boost-duration";
    public static final String ZAPPI_CHANNEL_TIMED_BOOST_CHARGE = "timed-boost-charge";
    public static final String ZAPPI_CHANNEL_CLAMP_NAME_1 = "clamp-name1";
    public static final String ZAPPI_CHANNEL_CLAMP_NAME_2 = "clamp-name2";
    public static final String ZAPPI_CHANNEL_CLAMP_NAME_3 = "clamp-name3";
    public static final String ZAPPI_CHANNEL_CLAMP_NAME_4 = "clamp-name4";
    public static final String ZAPPI_CHANNEL_CLAMP_NAME_5 = "clamp-name5";
    public static final String ZAPPI_CHANNEL_CLAMP_NAME_6 = "clamp-name6";
    public static final String ZAPPI_CHANNEL_CLAMP_POWER_1 = "clamp-power1";
    public static final String ZAPPI_CHANNEL_CLAMP_POWER_2 = "clamp-power2";
    public static final String ZAPPI_CHANNEL_CLAMP_POWER_3 = "clamp-power3";
    public static final String ZAPPI_CHANNEL_CLAMP_POWER_4 = "clamp-power4";
    public static final String ZAPPI_CHANNEL_CLAMP_POWER_5 = "clamp-power5";
    public static final String ZAPPI_CHANNEL_CLAMP_POWER_6 = "clamp-power6";

    // Harvi Channel Names
    public static final String HARVI_CHANNEL_LAST_UPDATED_TIME = "last-updated-time";
    public static final String HARVI_CHANNEL_CLAMP_NAME_1 = "clamp-name1";
    public static final String HARVI_CHANNEL_CLAMP_NAME_2 = "clamp-name2";
    public static final String HARVI_CHANNEL_CLAMP_NAME_3 = "clamp-name3";
    public static final String HARVI_CHANNEL_CLAMP_POWER_1 = "clamp-power1";
    public static final String HARVI_CHANNEL_CLAMP_POWER_2 = "clamp-power2";
    public static final String HARVI_CHANNEL_CLAMP_POWER_3 = "clamp-power3";
    public static final String HARVI_CHANNEL_CLAMP_PHASE_1 = "clamp-phase1";
    public static final String HARVI_CHANNEL_CLAMP_PHASE_2 = "clamp-phase2";
    public static final String HARVI_CHANNEL_CLAMP_PHASE_3 = "clamp-phase3";

    // Eddi Channel Names
    public static final String EDDI_CHANNEL_LAST_UPDATED_TIME = "last-updated-time";
    public static final String EDDI_CHANNEL_SUPPLY_VOLTAGE = "supply-voltage";
    public static final String EDDI_CHANNEL_SUPPLY_FREQUENCY = "supply-frequency";
    public static final String EDDI_CHANNEL_DIVERTER_STATUS = "diverter-status";
    public static final String EDDI_CHANNEL_DIVERTER_PRIORITY = "diverter-priority";
    public static final String EDDI_CHANNEL_PHASE = "phase";
    public static final String EDDI_CHANNEL_GRID_POWER = "grid-power";
    public static final String EDDI_CHANNEL_BOOST_MODE = "boost-mode";
    public static final String EDDI_CHANNEL_ENERGY_TRANSFERRED = "energy-transferred";
    public static final String EDDI_CHANNEL_GENERATED_POWER = "generated-power";
    public static final String EDDI_CHANNEL_DIVERTED_POWER = "diverted-power";
    public static final String EDDI_CHANNEL_BOOST_REMAINING = "boost-remaining";
    public static final String EDDI_CHANNEL_ACTIVE_HEATER = "active-heater";
    public static final String EDDI_CHANNEL_HEATER_PRIORITY = "heater-priority";
    public static final String EDDI_CHANNEL_HEATER_NAME_1 = "heater-name1";
    public static final String EDDI_CHANNEL_HEATER_NAME_2 = "heater-name2";
    public static final String EDDI_CHANNEL_TEMPERATURE_1 = "temperature1";
    public static final String EDDI_CHANNEL_TEMPERATURE_2 = "temperature2";
    public static final String EDDI_CHANNEL_CLAMP_NAME_1 = "clamp-name1";
    public static final String EDDI_CHANNEL_CLAMP_POWER_1 = "clamp-power1";
    public static final String EDDI_CHANNEL_CLAMP_NAME_2 = "clamp-name2";
    public static final String EDDI_CHANNEL_CLAMP_POWER_2 = "clamp-power2";
    public static final String EDDI_CHANNEL_CLAMP_NAME_3 = "clamp-name3";
    public static final String EDDI_CHANNEL_CLAMP_POWER_3 = "clamp-power3";
}
