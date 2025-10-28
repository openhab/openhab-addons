/**
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
package org.openhab.binding.ferroamp.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FerroampBindingConstants} class defines common constants, which are
 * used throughout the binding.
 *
 * @author Örjan Backsell - Initial contribution
 *
 */
@NonNullByDefault
public class FerroampBindingConstants {

    public static final String BINDING_ID = "ferroamp";

    // Broker (energyhub) port number
    public static final int BROKER_PORT = 1883;

    // Broker (energyhub) status
    public static final String CONNECTED = "connected";

    // Broker (energyhub) topics
    public static final String EHUB_TOPIC = "extapi/data/ehub";
    public static final String SSO_TOPIC = "extapi/data/sso";
    public static final String ESO_TOPIC = "extapi/data/eso";
    public static final String ESM_TOPIC = "extapi/data/esm";
    public static final String REQUEST_TOPIC = "extapi/control/request";

    // Broker (energyhub) QOS level
    public static final String QOS = "2";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ENERGYHUB = new ThingTypeUID(BINDING_ID, "energyhub");

    // List of EHUB Channel ids
    public static final String CHANNEL_GRID_FREQUENCY = "grid-frequency";
    public static final String CHANNEL_ACE_CURRENT_L1 = "ace-current-l1";
    public static final String CHANNEL_ACE_CURRENT_L2 = "ace-current-l2";
    public static final String CHANNEL_ACE_CURRENT_L3 = "ace-current-l3";
    public static final String CHANNEL_GRID_VOLTAGE_L1 = "grid-voltage-l1";
    public static final String CHANNEL_GRID_VOLTAGE_L2 = "grid-voltage-l2";
    public static final String CHANNEL_GRID_VOLTAGE_L3 = "grid-voltage-l3";
    public static final String CHANNEL_INVERTER_RMS_CURRENT_L1 = "inverter-rms-current-l1";
    public static final String CHANNEL_INVERTER_RMS_CURRENT_L2 = "inverter-rms-current-l2";
    public static final String CHANNEL_INVERTER_RMS_CURRENT_L3 = "inverter-rms-current-l3";
    public static final String CHANNEL_INVERTER_REACTIVE_CURRENT_L1 = "inverter-reactive-current-l1";
    public static final String CHANNEL_INVERTER_REACTIVE_CURRENT_L2 = "inverter-reactive-current-l2";
    public static final String CHANNEL_INVERTER_REACTIVE_CURRENT_L3 = "inverter-reactive-current-l3";
    public static final String CHANNEL_INVERTER_ACTIVE_CURRENT_L1 = "inverter-active-current-l1";
    public static final String CHANNEL_INVERTER_ACTIVE_CURRENT_L2 = "inverter-active-current-l2";
    public static final String CHANNEL_INVERTER_ACTIVE_CURRENT_L3 = "inverter-active-current-l3";
    public static final String CHANNEL_GRID_CURRENT_L1 = "grid-current-l1";
    public static final String CHANNEL_GRID_CURRENT_L2 = "grid-current-l2";
    public static final String CHANNEL_GRID_CURRENT_L3 = "grid-current-l3";
    public static final String CHANNEL_GRID_REACTIVE_CURRENT_L1 = "grid-reactive-current-l1";
    public static final String CHANNEL_GRID_REACTIVE_CURRENT_L2 = "grid-reactive-current-l2";
    public static final String CHANNEL_GRID_REACTIVE_CURRENT_L3 = "grid-reactive-current-l3";
    public static final String CHANNEL_GRID_ACTIVE_CURRENT_L1 = "grid-active-current-l1";
    public static final String CHANNEL_GRID_ACTIVE_CURRENT_L2 = "grid-active-current-l2";
    public static final String CHANNEL_GRID_ACTIVE_CURRENT_L3 = "grid-active-current-l3";
    public static final String CHANNEL_INVERTER_LOAD_REACTIVE_CURRENT_L1 = "inverter-load-reactive-current-l1";
    public static final String CHANNEL_INVERTER_LOAD_REACTIVE_CURRENT_L2 = "inverter-load-reactive-current-l2";
    public static final String CHANNEL_INVERTER_LOAD_REACTIVE_CURRENT_L3 = "inverter-load-reactive-current-l3";
    public static final String CHANNEL_INVERTER_LOAD_ACTIVE_CURRENT_L1 = "inverter-load-active-current-l1";
    public static final String CHANNEL_INVERTER_LOAD_ACTIVE_CURRENT_L2 = "inverter-load-active-current-l2";
    public static final String CHANNEL_INVERTER_LOAD_ACTIVE_CURRENT_L3 = "inverter-load-active-current-l3";
    public static final String CHANNEL_APPARENT_POWER = "apparent-power";
    public static final String CHANNEL_GRID_POWER_ACTIVE_L1 = "grid-power-active-l1";
    public static final String CHANNEL_GRID_POWER_ACTIVE_L2 = "grid-power-active-l2";
    public static final String CHANNEL_GRID_POWER_ACTIVE_L3 = "grid-power-active-l3";
    public static final String CHANNEL_GRID_POWER_REACTIVE_L1 = "grid-power-reactive-l1";
    public static final String CHANNEL_GRID_POWER_REACTIVE_L2 = "grid-power-reactive-l2";
    public static final String CHANNEL_GRID_POWER_REACTIVE_L3 = "grid-power-reactive-l3";
    public static final String CHANNEL_INVERTER_POWER_ACTIVE_L1 = "inverter-power-active-l1";
    public static final String CHANNEL_INVERTER_POWER_ACTIVE_L2 = "inverter-power-active-l2";
    public static final String CHANNEL_INVERTER_POWER_ACTIVE_L3 = "inverter-power-active-l3";
    public static final String CHANNEL_INVERTER_POWER_REACTIVE_L1 = "inverter-power-reactive-l1";
    public static final String CHANNEL_INVERTER_POWER_REACTIVE_L2 = "inverter-power-reactive-l2";
    public static final String CHANNEL_INVERTER_POWER_REACTIVE_L3 = "inverter-power-reactive-l3";
    public static final String CHANNEL_CONSUMPTION_POWER_L1 = "consumption-power-l1";
    public static final String CHANNEL_CONSUMPTION_POWER_L2 = "consumption-power-l2";
    public static final String CHANNEL_CONSUMPTION_POWER_L3 = "consumption-power-l3";
    public static final String CHANNEL_CONSUMPTION_POWER_REACTIVE_L1 = "consumption-power-reactive-l1";
    public static final String CHANNEL_CONSUMPTION_POWER_REACTIVE_L2 = "consumption-power-reactive-l2";
    public static final String CHANNEL_CONSUMPTION_POWER_REACTIVE_L3 = "consumption-power-reactive-l3";
    public static final String CHANNEL_SOLAR_PV = "solar-pv";
    public static final String CHANNEL_POSITIVE_DC_LINK_VOLTAGE = "positive-dc-link-voltage";
    public static final String CHANNEL_NEGATIVE_DC_LINK_VOLTAGE = "negative-dc-link-voltage";
    public static final String CHANNEL_GRID_ENERGY_PRODUCED_L1 = "grid-energy-produced-l1";
    public static final String CHANNEL_GRID_ENERGY_PRODUCED_L2 = "grid-energy-produced-l2";
    public static final String CHANNEL_GRID_ENERGY_PRODUCED_L3 = "grid-energy-produced-l3";
    public static final String CHANNEL_GRID_ENERGY_CONSUMED_L1 = "grid-energy-consumed-l1";
    public static final String CHANNEL_GRID_ENERGY_CONSUMED_L2 = "grid-energy-consumed-l2";
    public static final String CHANNEL_GRID_ENERGY_CONSUMED_L3 = "grid-energy-consumed-l3";
    public static final String CHANNEL_INVERTER_ENERGY_PRODUCED_L1 = "inverter-energy-produced-l1";
    public static final String CHANNEL_INVERTER_ENERGY_PRODUCED_L2 = "inverter-energy-produced-l2";
    public static final String CHANNEL_INVERTER_ENERGY_PRODUCED_L3 = "inverter-energy-produced-l3";
    public static final String CHANNEL_INVERTER_ENERGY_CONSUMED_L1 = "inverter-energy-consumed-l1";
    public static final String CHANNEL_INVERTER_ENERGY_CONSUMED_L2 = "inverter-energy-consumed-l2";
    public static final String CHANNEL_INVERTER_ENERGY_CONSUMED_L3 = "inverter-energy-consumed-l3";
    public static final String CHANNEL_LOAD_ENERGY_PRODUCED_L1 = "load-energy-produced-l1";
    public static final String CHANNEL_LOAD_ENERGY_PRODUCED_L2 = "load-energy-produced-l2";
    public static final String CHANNEL_LOAD_ENERGY_PRODUCED_L3 = "load-energy-produced-l3";
    public static final String CHANNEL_LOAD_ENERGY_CONSUMED_L1 = "load-energy-consumed-l1";
    public static final String CHANNEL_LOAD_ENERGY_CONSUMED_L2 = "load-energy-consumed-l2";
    public static final String CHANNEL_LOAD_ENERGY_CONSUMED_L3 = "load-energy-consumed-l3";
    public static final String CHANNEL_GRID_ENERGY_PRODUCED_TOTAL = "grid-energy-produced-total";
    public static final String CHANNEL_GRID_ENERGY_CONSUMED_TOTAL = "grid-energy-consumed-total";
    public static final String CHANNEL_INVERTER_ENERGY_PRODUCED_TOTAL = "inverter-energy-produced-total";
    public static final String CHANNEL_INVERTER_ENERGY_CONSUMED_TOTAL = "inverter-energy-consumed-total";
    public static final String CHANNEL_LOAD_ENERGY_PRODUCED_TOTAL = "load-energy-produced-total";
    public static final String CHANNEL_LOAD_ENERGY_CONSUMED_TOTAL = "load-energy-consumed-total";
    public static final String CHANNEL_TOTAL_SOLAR_ENERGY = "total-solar-energy";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_TIMESTAMP = "timestamp";

    // List of battery setup Channel ids
    public static final String CHANNEL_BATTERY_ENERGY_PRODUCED = "battery-energy-produced";
    public static final String CHANNEL_BATTERY_ENERGY_CONSUMED = "battery-energy-consumed";
    public static final String CHANNEL_SOC = "soc";
    public static final String CHANNEL_SOH = "soh";
    public static final String CHANNEL_POWER_BATTERY = "power-battery";
    public static final String CHANNEL_TOTAL_CAPACITY_BATTERIES = "total-capacity-batteries";

    // List of SSO Channel ids
    public static final String CHANNEL_SSO_ID = "id";
    public static final String CHANNEL_SSO_PV_VOLTAGE = "pv-voltage";
    public static final String CHANNEL_SSO_PV_CURRENT = "pv-current";
    public static final String CHANNEL_SSO_TOTAL_SOLAR_ENERGY = "total-solar-energy";
    public static final String CHANNEL_SSO_RELAY_STATUS = "relay-status";
    public static final String CHANNEL_SSO_TEMPERATURE = "temperature";
    public static final String CHANNEL_SSO_FAULT_CODE = "fault-code";
    public static final String CHANNEL_SSO_DC_LINK_VOLTAGE = "dc-link-voltage";
    public static final String CHANNEL_SSO_TIMESTAMP = "timestamp";

    // List of ESO Channel ids
    public static final String CHANNEL_ESO_ID = "id";
    public static final String CHANNEL_ESO_VOLTAGE_BATTERY = "voltage-battery";
    public static final String CHANNEL_ESO_CURRENT_BATTERY = "current-battery";
    public static final String CHANNEL_ESO_BATTERY_ENERGY_PRODUCED = "battery-energy-produced";
    public static final String CHANNEL_ESO_BATTERY_ENERGY_CONSUMED = "battery-energy-consumed";
    public static final String CHANNEL_ESO_SOC = "soc";
    public static final String CHANNEL_ESO_RELAY_STATUS = "relay-status";
    public static final String CHANNEL_ESO_TEMPERATURE = "temperature";
    public static final String CHANNEL_ESO_FAULT_CODE = "fault-code";
    public static final String CHANNEL_ESO_DC_LINK_VOLTAGE = "dc-link-voltage";
    public static final String CHANNEL_ESO_TIMESTAMP = "timestamp";

    // List of ESM Channel ids
    public static final String CHANNEL_ESM_ID = "id";
    public static final String CHANNEL_ESM_SOH = "soh";
    public static final String CHANNEL_ESM_SOC = "soc";
    public static final String CHANNEL_ESM_TOTAL_CAPACITY = "total-capacity";
    public static final String CHANNEL_ESM_POWER_BATTERY = "power-battery";
    public static final String CHANNEL_ESM_STATUS = "status";
    public static final String CHANNEL_ESM_TIMESTAMP = "timestamp";

    // List of all Channel ids for configuration
    public static final String CHANNEL_REQUEST_CHARGE = "request-charge";
    public static final String CHANNEL_REQUEST_DISCHARGE = "request-discharge";
    public static final String CHANNEL_REQUEST_AUTO = "request-auto";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ENERGYHUB);
}