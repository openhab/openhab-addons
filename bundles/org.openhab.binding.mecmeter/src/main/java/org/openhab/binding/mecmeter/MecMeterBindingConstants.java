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
package org.openhab.binding.mecmeter;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MecMeterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Florian Pazour - Initial contribution
 * @author Klaus Berger - Initial contribution
 */
@NonNullByDefault
public class MecMeterBindingConstants {

    private static final String BINDING_ID = "mecmeter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_METER = new ThingTypeUID(BINDING_ID, "meter");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_METER);

    /*
     * List of all Groups
     */
    public static final String GENERAL_GROUP = "general_group";
    public static final String VOLTAGE_GROUP = "voltage_group";
    public static final String CURRENT_GROUP = "current_group";
    public static final String ANGLE_GROUP = "angle_group";

    public static final String ACTIVE_POWER_GROUP = "activepower_group";
    public static final String ACTIVE_FUND_POWER_GROUP = "activefundpower_group";
    public static final String POWER_FACTOR_GROUP = "powerfactor_group";
    public static final String ACTIVE_HARM_POWER_GROUP = "activeharmpower_group";
    public static final String REACTIVE_POWER_GROUP = "reactivepower_group";

    public static final String APP_POWER_GROUP = "apppower_group";

    public static final String FWD_ACTIVE_ENERGY_GROUP = "fwd_active_energy_group";
    public static final String FWD_ACTIVE_FUND_ENERGY_GROUP = "fwd_active_fund_energy_group";
    public static final String FWD_ACTIVE_HARM_ENERGY_GROUP = "fwd_active_harm_energy_group";
    public static final String FWD_REACTIVE_ENERGY_GROUP = "fwd_reactive_energy_group";

    public static final String REV_ACTIVE_ENERGY_GROUP = "rev_active_energy_group";
    public static final String REV_ACTIVE_FUND_ENERGY_GROUP = "rev_active_fund_energy_group";
    public static final String REV_ACTIVE_HARM_ENERGY_GROUP = "rev_active_harm_energy_group";
    public static final String REV_REACTIVE_ENERGY_GROUP = "rev_reactive_energy_group";

    public static final String APP_ENERGY_GROUP = "app_energy_group";

    /*
     * List of all Channels
     */
    // General Channels
    public static final String FREQUENCY = GENERAL_GROUP + "#" + "frequency";
    public static final String TEMPERATURE = GENERAL_GROUP + "#" + "temperature";
    public static final String OPERATIONAL_TIME = GENERAL_GROUP + "#" + "op_time";

    // Voltage Channels
    public static final String VOLTAGE_PHASE_1 = VOLTAGE_GROUP + "#" + "voltage_phase1";
    public static final String VOLTAGE_PHASE_2 = VOLTAGE_GROUP + "#" + "voltage_phase2";
    public static final String VOLTAGE_PHASE_3 = VOLTAGE_GROUP + "#" + "voltage_phase3";

    public static final String VOLTAGE_PHASE_3_TO_PHASE_2 = VOLTAGE_GROUP + "#" + "voltage_phase3-2";
    public static final String VOLTAGE_PHASE_2_TO_PHASE_1 = VOLTAGE_GROUP + "#" + "voltage_phase2-1";
    public static final String VOLTAGE_PHASE_1_TO_PHASE_3 = VOLTAGE_GROUP + "#" + "voltage_phase1-3";

    public static final String AVERAGE_VOLTAGE_PHASE_2_PHASE = VOLTAGE_GROUP + "#" + "avg_phase_phase_voltage";
    public static final String AVERAGE_VOLTAGE_NEUTRAL_2_PHASE = VOLTAGE_GROUP + "#" + "avg_neutral_phase_voltage";

    // Current Channels
    public static final String CURRENT_PHASE_1 = CURRENT_GROUP + "#" + "current_phase1";
    public static final String CURRENT_PHASE_2 = CURRENT_GROUP + "#" + "current_phase2";
    public static final String CURRENT_PHASE_3 = CURRENT_GROUP + "#" + "current_phase3";
    public static final String CURRENT_SUM = CURRENT_GROUP + "#" + "current_allphase";

    // Angle Channels
    public static final String PHASE_ANGLE_TO_CURRENT_PHASE_1 = ANGLE_GROUP + "#" + "phase_angle_currvolt_phase1";
    public static final String PHASE_ANGLE_TO_CURRENT_PHASE_2 = ANGLE_GROUP + "#" + "phase_angle_currvolt_phase2";
    public static final String PHASE_ANGLE_TO_CURRENT_PHASE_3 = ANGLE_GROUP + "#" + "phase_angle_currvolt_phase3";

    public static final String PHASE_ANGLE_PHASE_1_3 = ANGLE_GROUP + "#" + "phase_angle_phase1-3";
    public static final String PHASE_ANGLE_PHASE_2_3 = ANGLE_GROUP + "#" + "phase_angle_phase2-3";

    // Power Channels
    public static final String ACTIVE_POWER_PHASE_1 = ACTIVE_POWER_GROUP + "#" + "activepower_phase1";
    public static final String ACTIVE_POWER_PHASE_2 = ACTIVE_POWER_GROUP + "#" + "activepower_phase2";
    public static final String ACTIVE_POWER_PHASE_3 = ACTIVE_POWER_GROUP + "#" + "activepower_phase3";
    public static final String ACTIVE_POWER_SUM = ACTIVE_POWER_GROUP + "#" + "activepower_allphase";

    public static final String ACTIVE_FUND_POWER_PHASE_1 = ACTIVE_FUND_POWER_GROUP + "#" + "activefundpower_phase1";
    public static final String ACTIVE_FUND_POWER_PHASE_2 = ACTIVE_FUND_POWER_GROUP + "#" + "activefundpower_phase2";
    public static final String ACTIVE_FUND_POWER_PHASE_3 = ACTIVE_FUND_POWER_GROUP + "#" + "activefundpower_phase3";
    public static final String ACTIVE_FUND_POWER_ALL = ACTIVE_FUND_POWER_GROUP + "#" + "activefundpower_allphase";

    public static final String POWER_FACTOR_PHASE_1 = POWER_FACTOR_GROUP + "#" + "powerFactor_phase1";
    public static final String POWER_FACTOR_PHASE_2 = POWER_FACTOR_GROUP + "#" + "powerFactor_phase2";
    public static final String POWER_FACTOR_PHASE_3 = POWER_FACTOR_GROUP + "#" + "powerFactor_phase3";
    public static final String POWER_FACTOR_ALL = POWER_FACTOR_GROUP + "#" + "powerFactor_allphase";

    public static final String ACTIVE_HARM_POWER_PHASE_1 = ACTIVE_HARM_POWER_GROUP + "#" + "activeharmpower_phase1";
    public static final String ACTIVE_HARM_POWER_PHASE_2 = ACTIVE_HARM_POWER_GROUP + "#" + "activeharmpower_phase2";
    public static final String ACTIVE_HARM_POWER_PHASE_3 = ACTIVE_HARM_POWER_GROUP + "#" + "activeharmpower_phase3";
    public static final String ACTIVE_HARM_POWER_ALL = ACTIVE_HARM_POWER_GROUP + "#" + "activeharmpower_allphase";

    public static final String REACTIVE_POWER_PHASE_1 = REACTIVE_POWER_GROUP + "#" + "reactivepower_phase1";
    public static final String REACTIVE_POWER_PHASE_2 = REACTIVE_POWER_GROUP + "#" + "reactivepower_phase2";
    public static final String REACTIVE_POWER_PHASE_3 = REACTIVE_POWER_GROUP + "#" + "reactivepower_phase3";
    public static final String REACTIVE_POWER_ALL = REACTIVE_POWER_GROUP + "#" + "reactivepower_allphase";

    public static final String APP_POWER_PHASE_1 = APP_POWER_GROUP + "#" + "apppower_phase1";
    public static final String APP_POWER_PHASE_2 = APP_POWER_GROUP + "#" + "apppower_phase2";
    public static final String APP_POWER_PHASE_3 = APP_POWER_GROUP + "#" + "apppower_phase3";
    public static final String APP_POWER_ALL = APP_POWER_GROUP + "#" + "apppower_allphase";

    // Forward Energy Channels
    public static final String FORWARD_ACTIVE_ENERGY_PHASE_1 = FWD_ACTIVE_ENERGY_GROUP + "#"
            + "fwd_active_energy_phase1";
    public static final String FORWARD_ACTIVE_ENERGY_PHASE_2 = FWD_ACTIVE_ENERGY_GROUP + "#"
            + "fwd_active_energy_phase2";
    public static final String FORWARD_ACTIVE_ENERGY_PHASE_3 = FWD_ACTIVE_ENERGY_GROUP + "#"
            + "fwd_active_energy_phase3";
    public static final String FORWARD_ACTIVE_ENERGY_ALL = FWD_ACTIVE_ENERGY_GROUP + "#" + "fwd_active_energy_allphase";

    public static final String FORWARD_ACTIVE_FUND_ENERGY_PHASE_1 = FWD_ACTIVE_FUND_ENERGY_GROUP + "#"
            + "fwd_active_fund_energy_phase1";
    public static final String FORWARD_ACTIVE_FUND_ENERGY_PHASE_2 = FWD_ACTIVE_FUND_ENERGY_GROUP + "#"
            + "fwd_active_fund_energy_phase2";
    public static final String FORWARD_ACTIVE_FUND_ENERGY_PHASE_3 = FWD_ACTIVE_FUND_ENERGY_GROUP + "#"
            + "fwd_active_fund_energy_phase3";
    public static final String FORWARD_ACTIVE_FUND_ENERGY_ALL = FWD_ACTIVE_FUND_ENERGY_GROUP + "#"
            + "fwd_active_fund_energy_allphase";

    public static final String FORWARD_ACTIVE_HARM_ENERGY_PHASE_1 = FWD_ACTIVE_HARM_ENERGY_GROUP + "#"
            + "fwd_active_harm_energy_phase1";
    public static final String FORWARD_ACTIVE_HARM_ENERGY_PHASE_2 = FWD_ACTIVE_HARM_ENERGY_GROUP + "#"
            + "fwd_active_harm_energy_phase2";
    public static final String FORWARD_ACTIVE_HARM_ENERGY_PHASE_3 = FWD_ACTIVE_HARM_ENERGY_GROUP + "#"
            + "fwd_active_harm_energy_phase3";
    public static final String FORWARD_ACTIVE_HARM_ENERGY_ALL = FWD_ACTIVE_HARM_ENERGY_GROUP + "#"
            + "fwd_active_harm_energy_allphase";

    public static final String FORWARD_REACTIVE_ENERGY_PHASE_1 = FWD_REACTIVE_ENERGY_GROUP + "#"
            + "fwd_reactive_energy_phase1";
    public static final String FORWARD_REACTIVE_ENERGY_PHASE_2 = FWD_REACTIVE_ENERGY_GROUP + "#"
            + "fwd_reactive_energy_phase2";
    public static final String FORWARD_REACTIVE_ENERGY_PHASE_3 = FWD_REACTIVE_ENERGY_GROUP + "#"
            + "fwd_reactive_energy_phase3";
    public static final String FORWARD_REACTIVE_ENERGY_ALL = FWD_REACTIVE_ENERGY_GROUP + "#"
            + "fwd_reactive_energy_allphase";

    // Reverse Energy Channels
    public static final String REVERSE_ACTIVE_ENERGY_PHASE_1 = REV_ACTIVE_ENERGY_GROUP + "#"
            + "rev_active_energy_phase1";
    public static final String REVERSE_ACTIVE_ENERGY_PHASE_2 = REV_ACTIVE_ENERGY_GROUP + "#"
            + "rev_active_energy_phase2";
    public static final String REVERSE_ACTIVE_ENERGY_PHASE_3 = REV_ACTIVE_ENERGY_GROUP + "#"
            + "rev_active_energy_phase3";
    public static final String REVERSE_ACTIVE_ENERGY_ALL = REV_ACTIVE_ENERGY_GROUP + "#" + "rev_active_energy_allphase";

    public static final String REVERSE_ACTIVE_FUND_ENERGY_PHASE_1 = REV_ACTIVE_FUND_ENERGY_GROUP + "#"
            + "rev_active_fund_energy_phase1";
    public static final String REVERSE_ACTIVE_FUND_ENERGY_PHASE_2 = REV_ACTIVE_FUND_ENERGY_GROUP + "#"
            + "rev_active_fund_energy_phase2";
    public static final String REVERSE_ACTIVE_FUND_ENERGY_PHASE_3 = REV_ACTIVE_FUND_ENERGY_GROUP + "#"
            + "rev_active_fund_energy_phase3";
    public static final String REVERSE_ACTIVE_FUND_ENERGY_ALL = REV_ACTIVE_FUND_ENERGY_GROUP + "#"
            + "rev_active_fund_energy_allphase";

    public static final String REVERSE_ACTIVE_HARM_ENERGY_PHASE_1 = REV_ACTIVE_HARM_ENERGY_GROUP + "#"
            + "rev_active_harm_energy_phase1";
    public static final String REVERSE_ACTIVE_HARM_ENERGY_PHASE_2 = REV_ACTIVE_HARM_ENERGY_GROUP + "#"
            + "rev_active_harm_energy_phase2";
    public static final String REVERSE_ACTIVE_HARM_ENERGY_PHASE_3 = REV_ACTIVE_HARM_ENERGY_GROUP + "#"
            + "rev_active_harm_energy_phase3";
    public static final String REVERSE_ACTIVE_HARM_ENERGY_ALL = REV_ACTIVE_HARM_ENERGY_GROUP + "#"
            + "rev_active_harm_energy_allphase";

    public static final String REVERSE_REACTIVE_ENERGY_PHASE_1 = REV_REACTIVE_ENERGY_GROUP + "#"
            + "rev_reactive_energy_phase1";
    public static final String REVERSE_REACTIVE_ENERGY_PHASE_2 = REV_REACTIVE_ENERGY_GROUP + "#"
            + "rev_reactive_energy_phase2";
    public static final String REVERSE_REACTIVE_ENERGY_PHASE_3 = REV_REACTIVE_ENERGY_GROUP + "#"
            + "rev_reactive_energy_phase3";
    public static final String REVERSE_REACTIVE_ENERGY_ALL = REV_REACTIVE_ENERGY_GROUP + "#"
            + "rev_reactive_energy_allphase";

    // Apparent Energy Channels
    public static final String APP_ENERGY_PHASE_1 = APP_ENERGY_GROUP + "#" + "appenergy_consumption_phase1";
    public static final String APP_ENERGY_PHASE_2 = APP_ENERGY_GROUP + "#" + "appenergy_consumption_phase2";
    public static final String APP_ENERGY_PHASE_3 = APP_ENERGY_GROUP + "#" + "appenergy_consumption_phase3";
    public static final String APP_ENERGY_ALL = APP_ENERGY_GROUP + "#" + "appenergy_consumption_allphase";

    // list of all URLs
    public static final String POWERMETER_DATA_URL = "http://%IP%/wizard/public/api/measurements";
}
