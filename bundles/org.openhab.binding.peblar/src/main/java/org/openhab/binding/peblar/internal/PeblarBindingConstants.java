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
package org.openhab.binding.peblar.internal;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
class PeblarBindingConstants {

    public static final String BINDING_ID = "peblar";

    // Thing type
    public static final ThingTypeUID THING_TYPE_CHARGER = new ThingTypeUID(BINDING_ID, "charger");

    // Channel group IDs
    public static final String GROUP_METER = "meter";
    public static final String GROUP_EVINTERFACE = "ev-interface";
    public static final String GROUP_SYSTEM = "system";

    // Meter channels
    public static final String CHANNEL_CURRENT_PHASE1 = "meter#current-p1";
    public static final String CHANNEL_CURRENT_PHASE2 = "meter#current-p2";
    public static final String CHANNEL_CURRENT_PHASE3 = "meter#current-p3";
    public static final String CHANNEL_VOLTAGE_PHASE1 = "meter#voltage-p1";
    public static final String CHANNEL_VOLTAGE_PHASE2 = "meter#voltage-p2";
    public static final String CHANNEL_VOLTAGE_PHASE3 = "meter#voltage-p3";
    public static final String CHANNEL_POWER_PHASE1 = "meter#power-p1";
    public static final String CHANNEL_POWER_PHASE2 = "meter#power-p2";
    public static final String CHANNEL_POWER_PHASE3 = "meter#power-p3";
    public static final String CHANNEL_POWER_TOTAL = "meter#power-total";
    public static final String CHANNEL_ENERGY_TOTAL = "meter#energy-total";
    public static final String CHANNEL_ENERGY_SESSION = "meter#energy-session";

    // EV interface channels
    public static final String CHANNEL_CP_STATE = "ev-interface#cp-state";
    public static final String CHANNEL_LOCK_STATE = "ev-interface#lock-state";
    public static final String CHANNEL_CHARGE_CURRENT_LIMIT = "ev-interface#charge-current-limit";
    public static final String CHANNEL_CHARGE_CURRENT_LIMIT_SOURCE = "ev-interface#charge-current-limit-source";
    public static final String CHANNEL_CHARGE_CURRENT_LIMIT_ACTUAL = "ev-interface#charge-current-limit-actual";
    public static final String CHANNEL_FORCE_1_PHASE = "ev-interface#force1-phase";

    // System channels
    public static final String CHANNEL_PRODUCT_PN = "system#product-pn";
    public static final String CHANNEL_PRODUCT_SN = "system#product-sn";
    public static final String CHANNEL_FIRMWARE_VERSION = "system#firmware-version";
    public static final String CHANNEL_WLAN_SIGNAL_STRENGTH = "system#wlan-signal-strength";
    public static final String CHANNEL_CELLULAR_SIGNAL_STRENGTH = "system#cellular-signal-strength";
    public static final String CHANNEL_UPTIME = "system#uptime";
    public static final String CHANNEL_PHASE_COUNT = "system#phase-count";

    public static final List<String> CHANNELS_METER = List.of(CHANNEL_CURRENT_PHASE1, CHANNEL_CURRENT_PHASE2,
            CHANNEL_CURRENT_PHASE3, CHANNEL_VOLTAGE_PHASE1, CHANNEL_VOLTAGE_PHASE2, CHANNEL_VOLTAGE_PHASE3,
            CHANNEL_POWER_PHASE1, CHANNEL_POWER_PHASE2, CHANNEL_POWER_PHASE3, CHANNEL_POWER_TOTAL, CHANNEL_ENERGY_TOTAL,
            CHANNEL_ENERGY_SESSION);
    public static final List<String> CHANNELS_EVINTERFACE = List.of(CHANNEL_CP_STATE, CHANNEL_LOCK_STATE,
            CHANNEL_CHARGE_CURRENT_LIMIT, CHANNEL_CHARGE_CURRENT_LIMIT_SOURCE, CHANNEL_CHARGE_CURRENT_LIMIT_ACTUAL,
            CHANNEL_FORCE_1_PHASE);
    public static final List<String> CHANNELS_SYSTEM = List.of(CHANNEL_PRODUCT_PN, CHANNEL_PRODUCT_SN,
            CHANNEL_FIRMWARE_VERSION, CHANNEL_WLAN_SIGNAL_STRENGTH, CHANNEL_CELLULAR_SIGNAL_STRENGTH, CHANNEL_UPTIME,
            CHANNEL_PHASE_COUNT);
    public static final List<String> CHANNELS_ALL = Stream.of(CHANNELS_METER, CHANNELS_EVINTERFACE, CHANNELS_SYSTEM)
            .flatMap(Collection::stream).toList();
}
