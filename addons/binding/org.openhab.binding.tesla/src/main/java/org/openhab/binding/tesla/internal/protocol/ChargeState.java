/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tesla.internal.protocol;

/**
 * The {@link ChargeState} is a datastructure to capture
 * variables sent by the Tesla Vehicle
 *
 * @author Karel Goderis - Initial contribution
 */
public class ChargeState {
    public String charging_state;
    public int charge_limit_soc;
    public int charge_limit_soc_std;
    public int charge_limit_soc_min;
    public int charge_limit_soc_max;
    public boolean charge_to_max_range;
    public boolean battery_heater_on;
    public boolean not_enough_power_to_heat;
    public int max_range_charge_counter;
    public boolean fast_charger_present;
    public String fast_charger_type;
    public float battery_range;
    public float est_battery_range;
    public float ideal_battery_range;
    public int battery_level;
    public int usable_battery_level;
    public float battery_current;
    public float charge_energy_added;
    public float charge_miles_added_rated;
    public float charge_miles_added_ideal;
    public int charger_voltage;
    public int charger_pilot_current;
    public int charger_actual_current;
    public int charger_power;
    public float time_to_full_charge;
    public boolean trip_charging;
    public float charge_rate;
    public boolean charge_port_door_open;
    public boolean motorized_charge_port;
    public String scheduled_charging_start_time;
    public boolean scheduled_charging_pending;
    public String user_charge_enable_request;
    public boolean charge_enable_request;
    public boolean eu_vehicle;
    public int charger_phases;

    ChargeState() {
    }

}
