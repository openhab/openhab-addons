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
package org.openhab.persistence.timescaledb.internal;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;

/**
 * A historic item returned by TimescaleDB queries.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
public class TimescaleDBHistoricItem implements HistoricItem {

    private final String name;
    private final State state;
    private final Instant timestamp;

    public TimescaleDBHistoricItem(String name, State state, Instant timestamp) {
        this.name = name;
        this.state = state;
        this.timestamp = timestamp;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public ZonedDateTime getTimestamp() {
        return timestamp.atZone(ZoneId.systemDefault());
    }

    @Override
    public String toString() {
        return "TimescaleDBHistoricItem{name='" + name + "', state=" + state + ", timestamp=" + timestamp + "}";
    }
}
