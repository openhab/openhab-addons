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
package org.openhab.binding.awattar.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.awattar.internal.handler.TimeRange;

/**
 * Class to store hourly price data.
 *
 * @author Wolfgang Klimt - initial contribution
 * @author Jan N. Klug - Refactored to record
 */
@NonNullByDefault
public record AwattarPrice(double netPrice, double grossPrice, double netTotal, double grossTotal,
        TimeRange timerange) implements Comparable<AwattarPrice> {

    @Override
    public String toString() {
        return String.format("(%1$tF %1$tR - %2$tR: %3$.3f)", timerange.start(), timerange.end(), netPrice);
    }

    @Override
    public int compareTo(AwattarPrice o) {
        return Double.compare(netPrice, o.netPrice);
    }
}
