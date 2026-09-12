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
    /** Regulation target temperature (°C). */
    public double target_temp;
    /** Maximum boiler temperature (°C). */
    public double max_boiler_temp;
    /** Regulation state (0=off, 1=on). */
    public int regulation_state;
}
