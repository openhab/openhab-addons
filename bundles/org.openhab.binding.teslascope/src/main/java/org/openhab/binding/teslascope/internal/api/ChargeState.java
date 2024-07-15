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
package org.openhab.binding.teslascope.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class for holding the set of parameters used to read the controller variables.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class ChargeState {
    // charge_state
    public int battery_level;
    public int usable_battery_level;
    public float battery_range;
    public float est_battery_range;
    public int charge_enable_request;
    public float charge_energy_added;
    public int charge_limit_soc;
    public int charge_port_door_open;
    public float charge_rate;
    public int charger_power;
    public int charger_voltage;
    public String charging_state = "";
    public float time_to_full_charge;
    public int scheduled_charging_pending;
    public String scheduled_charging_start_time = " ";

    private ChargeState() {
    }
}
