/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.eclipse.smarthome.core.library.dimension.Intensity;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;

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
        return new QuantityType<>(total, SmartHomeUnits.IRRADIANCE);
    }

    /**
     * Returns the direct radiation.
     */
    public QuantityType<Intensity> getDirect() {
        return new QuantityType<>(direct, SmartHomeUnits.IRRADIANCE);
    }

    /**
     * Returns the diffuse radiation.
     */
    public QuantityType<Intensity> getDiffuse() {
        return new QuantityType<>(diffuse, SmartHomeUnits.IRRADIANCE);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("direct", direct)
                .append("diffuse", diffuse).append("total", total).toString();
    }
}
