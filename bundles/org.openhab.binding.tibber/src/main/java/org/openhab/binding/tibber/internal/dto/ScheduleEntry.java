/*
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
package org.openhab.binding.tibber.internal.dto;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link ScheduleEntry} is one entry of a non consecutive scheduling plan.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ScheduleEntry {
    public String start;
    public String stop;
    public int duration;
    public double cost;

    public ScheduleEntry(String start, String stop, int duration, double cost) {
        this.start = start;
        this.stop = stop;
        this.duration = duration;
        this.cost = cost;
    }

    public ScheduleEntry(Instant start, Instant stop, int duration, double cost) {
        this(start.toString(), stop.toString(), duration, cost);
    }

    @Override
    public String toString() {
        return "{\"start\":\"" + start + "\",\"stop\":\"" + stop + "\",\"duration\":" + duration + ",\"cost\":" + cost
                + "}";
    }
}
