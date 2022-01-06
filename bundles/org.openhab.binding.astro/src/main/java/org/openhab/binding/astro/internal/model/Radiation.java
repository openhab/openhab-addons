/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.openhab.core.library.dimension.Intensity;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * Holds the calculated direct, diffuse and total
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Christoph Weitkamp - Introduced UoM
 */
public class Radiation {

    private double direct;
    private double diffuse;
    private double total;

    public Radiation() {
    }

    public Radiation(double direct, double diffuse, double total) {
        this.direct = direct;
        this.diffuse = diffuse;
        this.total = total;
    }

    /**
     * Sets the direct radiation.
     */
    public void setDirect(double direct) {
        this.direct = direct;
    }

    /**
     * Sets the diffuse radiation.
     */
    public void setDiffuse(double diffuse) {
        this.diffuse = diffuse;
    }

    /**
     * Sets the total radiation.
     */
    public void setTotal(double total) {
        this.total = total;
    }

    /**
     * Returns the total radiation.
     */
    public QuantityType<Intensity> getTotal() {
        return new QuantityType<>(total, Units.IRRADIANCE);
    }

    /**
     * Returns the direct radiation.
     */
    public QuantityType<Intensity> getDirect() {
        return new QuantityType<>(direct, Units.IRRADIANCE);
    }

    /**
     * Returns the diffuse radiation.
     */
    public QuantityType<Intensity> getDiffuse() {
        return new QuantityType<>(diffuse, Units.IRRADIANCE);
    }
}
