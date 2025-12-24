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
package org.openhab.binding.astro.internal.model;

import static org.openhab.core.library.unit.MetricPrefix.KILO;
import static org.openhab.core.library.unit.SIUnits.METRE;

import java.util.Calendar;

import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;

/**
 * Holds a distance informations.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Christoph Weitkamp - Introduced UoM
 */
@NonNullByDefault
public class MoonDistance {

    private @Nullable Calendar date;
    private double distance;

    /**
     * Returns the date of the calculated distance.
     */
    @Nullable
    public Calendar getDate() {
        return date;
    }

    /**
     * Sets the date of the calculated distance.
     */
    public void setDate(@Nullable Calendar date) {
        this.date = date;
    }

    /**
     * Returns the distance in kilometers.
     */
    public QuantityType<Length> getDistance() {
        return new QuantityType<>(distance, KILO(METRE));
    }

    /**
     * Sets the distance in kilometers.
     */
    public void setDistance(double kilometer) {
        this.distance = kilometer;
    }
}
