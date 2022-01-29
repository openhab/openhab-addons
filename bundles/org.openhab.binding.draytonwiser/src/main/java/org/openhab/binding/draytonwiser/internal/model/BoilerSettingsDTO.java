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
package org.openhab.binding.draytonwiser.internal.model;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class BoilerSettingsDTO {

    private String controlType;
    private String fuelType;
    private String cycleRate;

    public String getControlType() {
        return controlType;
    }

    public void setControlType(final String controlType) {
        this.controlType = controlType;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(final String fuelType) {
        this.fuelType = fuelType;
    }

    public String getCycleRate() {
        return cycleRate;
    }

    public void setCycleRate(final String cycleRate) {
        this.cycleRate = cycleRate;
    }
}
