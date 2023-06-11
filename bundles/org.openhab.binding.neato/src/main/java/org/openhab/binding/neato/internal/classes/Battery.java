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
package org.openhab.binding.neato.internal.classes;

/**
 * The {@link Battery} is the internal class for battery information.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class Battery {

    private Integer level;
    private Integer timeToEmpty;
    private Integer timeToFullCharge;
    private Integer totalCharges;
    private String manufacturingDate;
    private Integer authorizationStatus;
    private String vendor;

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getTimeToEmpty() {
        return timeToEmpty;
    }

    public void setTimeToEmpty(Integer timeToEmpty) {
        this.timeToEmpty = timeToEmpty;
    }

    public Integer getTimeToFullCharge() {
        return timeToFullCharge;
    }

    public void setTimeToFullCharge(Integer timeToFullCharge) {
        this.timeToFullCharge = timeToFullCharge;
    }

    public Integer getTotalCharges() {
        return totalCharges;
    }

    public void setTotalCharges(Integer totalCharges) {
        this.totalCharges = totalCharges;
    }

    public String getManufacturingDate() {
        return manufacturingDate;
    }

    public void setManufacturingDate(String manufacturingDate) {
        this.manufacturingDate = manufacturingDate;
    }

    public Integer getAuthorizationStatus() {
        return authorizationStatus;
    }

    public void setAuthorizationStatus(Integer authorizationStatus) {
        this.authorizationStatus = authorizationStatus;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
}
