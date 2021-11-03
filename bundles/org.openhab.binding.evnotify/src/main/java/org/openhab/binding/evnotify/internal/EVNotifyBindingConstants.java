/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.evnotify.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EVNotifyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Schmidt - Initial contribution
 */
@NonNullByDefault
public class EVNotifyBindingConstants {

    private static final String BINDING_ID = "evnotify";

    // // Bridge
    // public static final String THING_TYPE_BRIDGE_ID = "bridge";
    // public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, THING_TYPE_BRIDGE_ID);
    // public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = Collections
    // .unmodifiableSet(Set.of(THING_TYPE_BRIDGE));

    // List of all Thing Type UIDs
    public static final String THING_TYPE_VEHICLE_ID = "vehicle";
    public static final ThingTypeUID THING_TYPE_VEHICLE = new ThingTypeUID(BINDING_ID, THING_TYPE_VEHICLE_ID);

    // List of all Channel ids
    public static final String STATE_OF_CHARGE_DISPLAY = "soc_display";
    public static final String STATE_OF_CHARGE_BMS = "soc_bms";
    public static final String LAST_STATE_OF_CHARGE = "last_soc";
    public static final String STATE_OF_HEALTH = "soh";
    public static final String CHARGING = "charging";
    public static final String RAPID_CHARING_PORT = "rapid_charge_port";
    public static final String NORMAL_CHARING_PORT = "normal_charge_port";
    public static final String SLOW_CHARING_PORT = "slow_charge_port";
    public static final String AUX_BATTERY_VOLTAGE = "aux_battery_voltage";
    public static final String DC_BATTERY_VOLTAGE = "dc_battery_voltage";
    public static final String DC_BATTERY_CURRENT = "dc_battery_current";
    public static final String DC_BATTERY_POWER = "dc_battery_power";
    public static final String CUMULATIVE_ENERGY_CHARGED = "cumulative_energy_charged";
    public static final String CUMULATIVE_ENERGY_DISCHARGED = "cumulative_energy_discharged";
    public static final String BATTERY_MIN_TEMPERATURE = "battery_min_temperature";
    public static final String BATTERY_MAX_TEMPERATURE = "battery_max_temperature";
    public static final String BATTERY_INLET_TEMPERATURE = "battery_inlet_temperature";
    public static final String EXTERNAL_TEMPERATURE = "external_temperature";
    public static final String LAST_EXTENDED = "last_extended";

    // Collection of all supported thing types
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Set.of(THING_TYPE_VEHICLE));

    // Collections.unmodifiableSet(
    // Stream.concat(
    // SUPPORTED_BRIDGE_THING_TYPES_UIDS.stream(),
    // Collections.unmodifiableSet(Set.of(THING_TYPE_EVNOTIFY)).stream()
    // )
    // .collect(Collectors.toSet()));
}
