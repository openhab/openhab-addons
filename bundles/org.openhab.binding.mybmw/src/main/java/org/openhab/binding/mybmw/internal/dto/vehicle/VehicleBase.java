/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.dto.vehicle;

/**
 * The {@link VehicleBase} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - refactored to Java Bean
 */
public class VehicleBase {
    private String vin = "";// ": "WBY1Z81040V905639",
    // mappingInfo - needed?
    // appVehicleType - needed?
    private VehicleAttributes attributes = new VehicleAttributes();

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public VehicleAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(VehicleAttributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "VehicleBase [vin=" + vin + ", attributes=" + attributes + "]";
    }
}
