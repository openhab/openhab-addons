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
package org.openhab.binding.froniuswattpilot.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FroniusWattpilotBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class FroniusWattpilotBindingConstants {
    private FroniusWattpilotBindingConstants() {
    }

    private static final String BINDING_ID = "froniuswattpilot";

    public static final String HOSTNAME_CONFIGURATION_KEY = "hostname";

    // Wattpilot Thing Type UID
    public static final ThingTypeUID THING_TYPE_WATTPILOT = new ThingTypeUID(BINDING_ID, "wattpilot");

    // Wattpilot channel groups
    public static final String CHANNEL_GROUP_ID_CONTROL = "control";
    public static final String CHANNEL_GROUP_ID_STATUS = "status";
    public static final String CHANNEL_GROUP_ID_METRICS = "metrics";

    // Wattpilot control channels
    public static final String CHANNEL_CHARGING_ALLOWED = "charging-allowed";
    public static final String CHANNEL_CHARGING_MODE = "charging-mode";
    public static final String CHANNEL_CHARGING_CURRENT = "charging-current";
    public static final String CHANNEL_PV_SURPLUS_THRESHOLD = "pv-surplus-threshold";
    public static final String CHANNEL_PV_SURPLUS_SOC = "pv-surplus-soc";
    public static final String CHANNEL_BOOST_ENABLED = "boost-enabled";
    public static final String CHANNEL_BOOST_SOC = "boost-soc";

    // Wattpilot status channels
    public static final String CHANNEL_CHARGING_STATE = "charging-state";
    public static final String CHANNEL_CHARGING_POSSIBLE = "charging-possible";
    public static final String CHANNEL_CHARGING_SINGLE_PHASE = "single-phase";

    // Wattpilot metrics channel prefixes
    public static final String PREFIX_PHASE_1 = "l1-";
    public static final String PREFIX_PHASE_2 = "l2-";
    public static final String PREFIX_PHASE_3 = "l3-";

    // Wattpilot metrics channels
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_CHARGED_ENERGY_SESSION = "energy-session";
    public static final String CHANNEL_CHARGED_ENERGY_TOTAL = "energy-total";
    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String CHANNEL_CURRENT = "current";
}
