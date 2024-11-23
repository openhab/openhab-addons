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
 * The {@link Vehicle} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - refactored for v2 API
 */
public class Vehicle {
    private VehicleBase vehicleBase = new VehicleBase();
    private VehicleStateContainer vehicleState = new VehicleStateContainer();

    public VehicleBase getVehicleBase() {
        return vehicleBase;
    }

    public void setVehicleBase(VehicleBase vehicleBase) {
        this.vehicleBase = vehicleBase;
    }

    public VehicleStateContainer getVehicleState() {
        return vehicleState;
    }

    public void setVehicleState(VehicleStateContainer vehicleState) {
        this.vehicleState = vehicleState;
    }

    @Override
    public String toString() {
        return "Vehicle [vehicleBase=" + vehicleBase + ", vehicleState=" + vehicleState + "]";
    }
}
