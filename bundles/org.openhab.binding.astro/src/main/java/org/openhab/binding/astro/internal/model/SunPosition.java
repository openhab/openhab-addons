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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.util.MathUtils;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Holds informations about the Moon Position
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Gaël L'hopital - Added shade length
 */
@NonNullByDefault
public class SunPosition extends Position {
    public static final Position NULL = new SunPosition();

    private SunPosition() {
        super(Double.NaN, Double.NaN);
    }

    public SunPosition(double azimuth, double elevation) {
        super(azimuth, elevation);
    }

    /**
     * Returns the shade length.
     */
    public State getShadeLength() {
        return Double.isNaN(elevation) ? UnDefType.NULL : new DecimalType(1 / MathUtils.tanDeg(elevation));
    }
}
