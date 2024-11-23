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
 * 
 * derived from the API responses
 * 
 * @author Martin Grassl - initial contribution
 */
public class VehicleTireState {
    private VehicleTireStateDetails details = new VehicleTireStateDetails();
    private VehicleTireStateStatus status = new VehicleTireStateStatus();

    public VehicleTireStateDetails getDetails() {
        return details;
    }

    public void setDetails(VehicleTireStateDetails details) {
        this.details = details;
    }

    public VehicleTireStateStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleTireStateStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "VehicleTireState [details=" + details + ", status=" + status + "]";
    }
}
