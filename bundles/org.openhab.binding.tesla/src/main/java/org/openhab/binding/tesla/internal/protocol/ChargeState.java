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
package org.openhab.binding.tesla.internal.protocol;

/**
 * The {@link ChargeState} is a datastructure to capture
 * variables sent by the Tesla Vehicle
 *
 * @author Karel Goderis - Initial contribution
 */
public class ChargeState {
    public boolean battery_heater_on;
    public boolean charge_enable_request;
    public boolean charge_port_door_open;
    public boolean charge_to_max_range;
    public boolean eu_vehicle;
    public boolean fast_charger_present;
    public boolean managed_charging_active;
    public boolean managed_charging_user_canceled;
    public boolean motorized_charge_port;
    public boolean not_enough_power_to_heat;
    public boolean scheduled_charging_pending;
    public boolean trip_charging;
    public float battery_current;
    public float battery_range;
    public float charge_energy_added;
    public float charge_miles_added_ideal;
    public float charge_miles_added_rated;
    public float charge_rate;
    public float est_battery_range;
    public float ideal_battery_range;
    public float time_to_full_charge;
    public int battery_level;
    public int charge_amps;
    public int charge_current_request;
    public int charge_current_request_max;
    public int charge_limit_soc;
    public int charge_limit_soc_max;
    public int charge_limit_soc_min;
    public int charge_limit_soc_std;
    public int charger_actual_current;
    public int charger_phases;
    public int charger_pilot_current;
    public int charger_power;
    public int charger_voltage;
    public int max_range_charge_counter;
    public int usable_battery_level;
    public String charge_port_latch;
    public String charging_state;
    public String conn_charge_cable;
    public String fast_charger_brand;
    public String fast_charger_type;
    public String managed_charging_start_time;
    public String scheduled_charging_start_time;
    public String user_charge_enable_request;

    ChargeState() {
    }
}
