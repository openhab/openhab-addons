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
public class VehicleState {
    // vehicle_state
    public int locked;
    public int sentry_mode;
    public int valet_mode;
    public String software_update_status = "";
    public String software_update_version = "";
    public int fd_window;
    public int fp_window;
    public int rd_window;
    public int rp_window;
    public String sun_roof_state = "";
    public int sun_roof_percent_open;
    public int homelink_nearby;
    public double tpms_pressure_fl;
    public double tpms_pressure_fr;
    public double tpms_pressure_rl;
    public double tpms_pressure_rr;
    public int tpms_soft_warning_fl;
    public int tpms_soft_warning_fr;
    public int tpms_soft_warning_rl;
    public int tpms_soft_warning_rr;
    public int df;
    public int dr;
    public int pf;
    public int pr;
    public int ft;
    public int rt;

    private VehicleState() {
    }
}
