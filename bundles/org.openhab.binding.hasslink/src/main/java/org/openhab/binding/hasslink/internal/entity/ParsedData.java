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
package org.openhab.binding.hasslink.internal.entity;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;

/**
 * Represents parsed data emitted from Home Assistant entity payloads, restricted strictly
 * to valid openHAB dispatch types.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public sealed interface ParsedData {

    record StateData(State state) implements ParsedData {
    }

    record TimeSeriesData(TimeSeries timeSeries) implements ParsedData {
    }

    // Factory methods for clean instantiation inside entity parsers
    static ParsedData of(State state) {
        return new StateData(state);
    }

    static ParsedData of(TimeSeries timeSeries) {
        return new TimeSeriesData(timeSeries);
    }
}
