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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Immutable POJO representing an amount of consumed water. Queried from the Miele REST API.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class WaterConsumption {
    @Nullable
    private String unit;
    @Nullable
    private Double value;

    /**
     * Gets the measurement unit which represents a volume.
     */
    public Optional<String> getUnit() {
        return Optional.ofNullable(unit);
    }

    /**
     * Gets the amount of water.
     */
    public Optional<Double> getValue() {
        return Optional.ofNullable(value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit, value);
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
        WaterConsumption other = (WaterConsumption) obj;
        return Objects.equals(unit, other.unit) && Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
        return "WaterConsumption [unit=" + unit + ", value=" + value + "]";
    }
}
