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

    private static final String BINDING_ID = "froniuswattpilot";

    // Wattpilot Thing Type UID
    public static final ThingTypeUID THING_TYPE = new ThingTypeUID(BINDING_ID, "wattpilot");

    // Wattpilot channel groups
    public static final String CHANNEL_GROUP_ID_CONTROL = "control";
    public static final String CHANNEL_GROUP_ID_STATUS = "status";
    public static final String CHANNEL_GROUP_ID_METRICS = "metrics";

    // Wattpilot metrics channel prefixes
    public static final String PREFIX_PHASE_1 = "l1-";
    public static final String PREFIX_PHASE_2 = "l2-";
    public static final String PREFIX_PHASE_3 = "l3-";

    // Wattpilot status channels
    public static final String CHANNEL_CHARGING_STATE = "charging-state";
    public static final String CHANNEL_CHARGING_ALLOWED = "charging-allowed";
    public static final String CHANNEL_CHARGING_SINGLE_PHASE = "single-phase";

    // Wattpilot metrics channels
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_CHARGED_ENERGY = "energy-session";
    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String CHANNEL_CURRENT = "current";
}
