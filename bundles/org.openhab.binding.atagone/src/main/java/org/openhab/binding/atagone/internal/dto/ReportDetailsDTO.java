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
package org.openhab.binding.atagone.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Gson DTO for the {@code report.details} block in a {@code retrieve_reply}.
 * Contains boiler regulation internals; most fields are advanced diagnostics.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault({})
public class ReportDetailsDTO {
    /** Boiler flow temperature (°C). */
    public double boiler_temp;
    /** Boiler return temperature (°C). */
    public double boiler_return_temp;
    /** Minimum modulation level (%). */
    public int min_mod_level;
    /** Burner modulation level (%). */
    public int rel_mod_level;
    /** Boiler capacity (kW). */
    public int boiler_capacity;
    /** Regulation target temperature (°C). */
    public double target_temp;
    /** Temperature overshoot (K). */
    public double overshoot;
    /** Maximum boiler temperature (°C). */
    public double max_boiler_temp;
    /** Alpha used in regulation algorithm. */
    public double alpha_used;
    /** Regulation state (0=off, 1=on). */
    public int regulation_state;
    /** CH mass flow rate × thermal capacity (firmware units). */
    public double ch_m_dot_c;
    /** House thermal capacitance (firmware units). */
    public double c_house;
    /** Radiator thermal resistance (firmware units). */
    public double r_rad;
    /** Envelope thermal resistance (firmware units). */
    public double r_env;
    /** Alpha (regulation coefficient). */
    public double alpha;
    /** Alpha max (regulation coefficient). */
    public double alpha_max;
    /** Regulation delay (firmware units). */
    public int delay;
    /** Mu (regulation coefficient). */
    public double mu;
    /** Threshold offset (K). */
    public double threshold_offs;
    /** Weather-dependent k-factor. */
    public double wd_k_factor;
    /** Weather-dependent exponent. */
    public double wd_exponent;
    /** Burner run hours (LMUC). */
    public double lmuc_burner_hours;
    /** DHW run hours (LMUC). */
    public double lmuc_dhw_hours;
    /** Proportional gain (regulation). */
    public double KP;
    /** Integral gain (regulation). */
    public double KI;
}
