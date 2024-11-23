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
package org.openhab.binding.mybmw.internal.dto.charge;

/**
 * The {@link ChargingSettings} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - refactored to Java Bean
 */
public class ChargingSettings {
    private int acCurrentLimit = -1; // 32,
    private String hospitality = ""; // HOSP_INACTIVE,
    private String idcc = ""; // AUTOMATIC_INTELLIGENT,
    private boolean isAcCurrentLimitActive = false; // false,
    private int targetSoc = -1; // 80

    public int getAcCurrentLimit() {
        return acCurrentLimit;
    }

    public void setAcCurrentLimit(int acCurrentLimit) {
        this.acCurrentLimit = acCurrentLimit;
    }

    public String getHospitality() {
        return hospitality;
    }

    public void setHospitality(String hospitality) {
        this.hospitality = hospitality;
    }

    public String getIdcc() {
        return idcc;
    }

    public void setIdcc(String idcc) {
        this.idcc = idcc;
    }

    public boolean isAcCurrentLimitActive() {
        return isAcCurrentLimitActive;
    }

    public void setAcCurrentLimitActive(boolean isAcCurrentLimitActive) {
        this.isAcCurrentLimitActive = isAcCurrentLimitActive;
    }

    public int getTargetSoc() {
        return targetSoc;
    }

    public void setTargetSoc(int targetSoc) {
        this.targetSoc = targetSoc;
    }

    @Override
    public String toString() {
        return "ChargingSettings [acCurrentLimit=" + acCurrentLimit + ", hospitality=" + hospitality + ", idcc=" + idcc
                + ", isAcCurrentLimitActive=" + isAcCurrentLimitActive + ", targetSoc=" + targetSoc + "]";
    }
}
