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

package org.openhab.binding.mielecloud.internal.webservice.api;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A physical quantity as obtained from the Miele REST API.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class Quantity {
    double value;
    Optional<String> unit;

    public Quantity(double value, @Nullable String unit) {
        this.value = value;
        this.unit = Optional.ofNullable(unit);
    }

    public double getValue() {
        return value;
    }

    public Optional<String> getUnit() {
        return unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Quantity other = (Quantity) obj;
        return Double.doubleToLongBits(value) == Double.doubleToLongBits(other.value)
                && Objects.equals(unit, other.unit);
    }

    @Override
    public String toString() {
        return "Quantity [value=" + value + ", unit=" + unit + "]";
    }
}
