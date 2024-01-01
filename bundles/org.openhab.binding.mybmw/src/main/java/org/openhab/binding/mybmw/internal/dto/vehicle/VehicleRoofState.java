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
public class VehicleRoofState {
    private String roofState = ""; // CLOSED,
    private String roofStateType = ""; // SUN_ROOF

    public String getRoofState() {
        return roofState;
    }

    public void setRoofState(String roofState) {
        this.roofState = roofState;
    }

    public String getRoofStateType() {
        return roofStateType;
    }

    public void setRoofStateType(String roofStateType) {
        this.roofStateType = roofStateType;
    }

    @Override
    public String toString() {
        return "VehicleRoofState [roofState=" + roofState + ", roofStateType=" + roofStateType + "]";
    }
}
