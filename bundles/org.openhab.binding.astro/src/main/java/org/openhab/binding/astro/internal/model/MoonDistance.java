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
package org.openhab.binding.astro.internal.model;

import static org.openhab.core.library.unit.MetricPrefix.KILO;
import static org.openhab.core.library.unit.SIUnits.METRE;

import java.time.Instant;

import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.util.DateTimeUtils;
import org.openhab.core.library.types.QuantityType;

/**
 * Holds a distance informations.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Christoph Weitkamp - Introduced UoM
 * @author Gaël L'hopital - Use Instant, made immutable
 */
@NonNullByDefault
public class MoonDistance {
    public static final MoonDistance NULL = new MoonDistance();

    private final @Nullable Instant date;
    private final double distance;

    private MoonDistance() {
        this.date = null;
        this.distance = Double.NaN;
    }

    public MoonDistance(double distanceJd, double distance) {
        this.date = DateTimeUtils.jdToInstant(distanceJd);
        this.distance = distance;
    }

    /**
     * Returns the date of the calculated distance.
     */
    @Nullable
    public Instant getDate() {
        return date;
    }

    /**
     * Returns the distance in kilometers.
     */
    public @Nullable QuantityType<Length> getDistance() {
        return Double.isNaN(distance) ? null : new QuantityType<>(distance, KILO(METRE));
    }
}
