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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.dimension.Intensity;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Holds the calculated direct, diffuse and total
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Christoph Weitkamp - Introduced UoM
 */
@NonNullByDefault
public class Radiation {
    public static final Radiation NULL = new Radiation();

    private final double direct;
    private final double diffuse;

    private Radiation() {
        this(Double.NaN, Double.NaN);
    }

    public Radiation(double direct, double diffuse) {
        this.direct = direct;
        this.diffuse = diffuse;
    }

    /**
     * Returns the total radiation.
     */
    public @Nullable QuantityType<Intensity> getTotal() {
        return Double.isNaN(direct) || Double.isNaN(diffuse) ? null
                : new QuantityType<>(direct + diffuse, Units.IRRADIANCE);
    }

    /**
     * Returns the direct radiation.
     */
    public State getDirect() {
        return Double.isNaN(direct) ? UnDefType.UNDEF : new QuantityType<>(direct, Units.IRRADIANCE);
    }

    /**
     * Returns the diffuse radiation.
     */
    public State getDiffuse() {
        return Double.isNaN(diffuse) ? UnDefType.UNDEF : new QuantityType<>(diffuse, Units.IRRADIANCE);
    }
}
