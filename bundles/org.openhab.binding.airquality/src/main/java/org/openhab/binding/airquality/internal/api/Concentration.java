/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
 * The {@link Concentration} is responsible to store the range of
 * a given physical measure associated with the corresponding AQI
 * index.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Concentration {
    private final double min;
    private final double span;
    private final Index index;

    public Concentration(double min, double max, Index index) {
        this.min = min;
        this.span = max - min;
        this.index = index;
    }

    public double getMin() {
        return min;
    }

    public double getSpan() {
        return span;
    }

    public Index getIndex() {
        return index;
    }
}
