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

/**
 * @author Florian Lettner - Initial contribution
 */
public class ReportDetailsDTO {
    public double boiler_temp;
    public double boiler_return_temp;
    public int min_mod_level;
    public int rel_mod_level;
    public int boiler_capacity;
    public double target_temp;
    public double overshoot;
    public double max_boiler_temp;
    public double alpha_used;
    public int regulation_state;
    public double ch_m_dot_c;
    public double c_house;
    public double r_rad;
    public double r_env;
    public double alpha;
    public double alpha_max;
    public int delay;
    public double mu;
    public double threshold_offs;
    public double wd_k_factor;
    public double wd_exponent;
    public double lmuc_burner_hours;
    public double lmuc_dhw_hours;
    public double KP;
    public double KI;
}
