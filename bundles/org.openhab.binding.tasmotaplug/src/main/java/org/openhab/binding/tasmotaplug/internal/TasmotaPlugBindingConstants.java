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
package org.openhab.binding.tasmotaplug.internal;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TasmotaPlugBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class TasmotaPlugBindingConstants {
    public static final String BINDING_ID = "tasmotaplug";

    public static final int DEFAULT_REFRESH_PERIOD_SEC = 30;
    public static final int DEFAULT_NUM_CHANNELS = 1;

    public static final String CMD_URI = "/cm?cmnd=%s";
    public static final String CMD_URI_AUTH = "/cm?user=%s&password=%s&cmnd=%s";

    public static final String STATUS = "Status";
    public static final String STATUS_CMD = "10";
    public static final String ON = "ON";
    public static final String OFF = "OFF";
    public static final String BLANK = "";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PLUG = new ThingTypeUID(BINDING_ID, "plug");

    // List of all Channel id's
    public static final String POWER = "power";
    public static final String POWER2 = "power2";
    public static final String POWER3 = "power3";
    public static final String POWER4 = "power4";
    public static final String VOLTAGE = "voltage";
    public static final String CURRENT = "current";
    public static final String WATTS = "watts";
    public static final String VOLT_AMPERE = "volt-ampere";
    public static final String VOLT_AMPERE_REACTIVE = "volt-ampere-reactive";
    public static final String POWER_FACTOR = "power-factor";
    public static final String ENERGY_TODAY = "energy-today";
    public static final String ENERGY_YESTERDAY = "energy-yesterday";
    public static final String ENERGY_TOTAL = "energy-total";
    public static final String ENERGY_TOTAL_START = "energy-total-start";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_PLUG);
    public static final List<String> CONTROL_CHANNEL_IDS = List.of(POWER, POWER2, POWER3, POWER4);
    public static final List<String> ENERGY_CHANNEL_IDS = List.of(VOLTAGE, CURRENT, WATTS, VOLT_AMPERE,
            VOLT_AMPERE_REACTIVE, POWER_FACTOR, ENERGY_TODAY, ENERGY_YESTERDAY, ENERGY_TOTAL, ENERGY_TOTAL_START);
}
