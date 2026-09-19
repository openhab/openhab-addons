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
 * Gson DTO for one schedule ({@code ch_schedule} or {@code dhw_schedule}) in the {@code schedules}
 * block of a {@code retrieve_reply}.
 * <p>
 * {@code entries} is one array per weekday, each holding that day's {@code [start, end, temp]}
 * triples (minutes-since-midnight, minutes-since-midnight, °C) — variable length per day, not fixed.
 * Outside every triple's window, {@code base_temp} is the fallback setpoint.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault({})
public class ScheduleDTO {
    /** Fallback setpoint (°C) for any time not covered by an entry. */
    public double base_temp;
    /** Per-weekday arrays of {@code [start, end, temp]} triples. */
    public double[][][] entries;
}
