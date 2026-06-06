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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Immutable class holding Moon Position information
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MoonPosition extends Position {
    public static final MoonPosition NONE = new MoonPosition();
    private final double longitude;

    private MoonPosition() {
        super(Double.NaN, Double.NaN);
        this.longitude = Double.NaN;
    }

    public MoonPosition(double azimuth, double elevation, double longitude) {
        super(azimuth, elevation);
        this.longitude = longitude;
    }

    /**
     * Returns the moon longitude
     */
    public double getLongitude() {
        return longitude;
    }
}
