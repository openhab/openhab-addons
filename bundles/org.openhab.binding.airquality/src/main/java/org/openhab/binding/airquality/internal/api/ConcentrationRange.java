/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.airquality.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ConcentrationRange} is responsible to store the range of
 * a given physical measure associated with the corresponding AQI
 * index.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ConcentrationRange {
    private final double min;
    private final double span;
    private final Index index;

    ConcentrationRange(double min, double max, Index index) {
        this.min = min;
        this.span = max - min;
        this.index = index;
    }

    /*
     * Computes the concentration corresponding to the index
     * if contained in the range
     *
     * @return : a physical concentration or -1 if not in range
     */
    double getConcentration(double idx) {
        return index.contains(idx) ? span / index.getSpan() * (idx - index.getMin()) + min : -1;
    }
}
