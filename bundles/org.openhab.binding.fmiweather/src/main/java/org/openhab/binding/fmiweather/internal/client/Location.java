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
package org.openhab.binding.fmiweather.internal.client;

import java.math.BigDecimal;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Station location
 *
 * Note: For simplicity, location implements object equality and hashCode using only id.
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class Location {

    public final String name;
    public final String id;
    public final BigDecimal latitude;
    public final BigDecimal longitude;

    /**
     *
     * @param name name for the location
     * @param id string identifying this location uniquely. Typically FMISID or latitude-longitude pair
     * @param latitude latitude of the location
     * @param longitude longitude of the location
     */
    public Location(String name, String id, BigDecimal latitude, BigDecimal longitude) {
        this.name = name;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Location)) {
            return false;
        }
        Location other = (Location) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder("Location(name=\"").append(name).append("\", id=\"").append(id).append("\", latitude=")
                .append(latitude).append(", longitude=").append(longitude).append(")").toString();
    }
}
