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

package org.openhab.automation.optimalwindow.internal.calc;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * One value of a forecast and the time range it is valid for.
 *
 * @author Wolfgang Klimt - Initial contribution
 * @author Jan N. Klug - Refactored to record
 *
 * @param value the forecast value, e.g. a price
 * @param timerange the time range of the value
 */
@NonNullByDefault
public record ForecastInterval(double value, TimeRange timerange) {

    @Override
    public String toString() {
        return String.format("(%1$tF %1$tR - %2$tR: %3$.3f)", timerange.start(), timerange.end(), value);
    }
}
