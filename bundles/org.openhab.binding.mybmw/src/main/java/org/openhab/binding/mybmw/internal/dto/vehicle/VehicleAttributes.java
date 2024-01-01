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

import org.openhab.binding.mybmw.internal.utils.BimmerConstants;

/**
 *
 * derived from the API responses
 *
 * @author Martin Grassl - initial contribution
 * @author Mark Herwege - fix brand BMW_I
 */
public class VehicleAttributes {
    private String lastFetched = ""; // "2022-12-21T17:30:40.363Z"
    private String model = "";// ": "i3 94 (+ REX)",
    private int year = -1;// ": 2017,
    private long color = -1;// ": 4284572001,
    private String brand = "";// ": "BMW",
    private String driveTrain = "";// ": "ELECTRIC",
    private String headUnitType = "";// ": "ID5",
    private String headUnitRaw = "";// ": "ID5",
    private String hmiVersion = "";// ": "ID4",
    // softwareVersionCurrent - needed?
    // softwareVersionExFactory - needed?
    private String telematicsUnit = "";// ": "TCB1",
    private String bodyType = "";// ": "I01",
    private String countryOfOrigin = ""; // "DE"
    // driverGuideInfo - needed?

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public long getColor() {
        return color;
    }

    public void setColor(long color) {
        this.color = color;
    }

    public String getBrand() {
        if (BimmerConstants.BRAND_BMWI.equals(brand.toLowerCase())) {
            return BimmerConstants.BRAND_BMW;
        } else {
            return brand.toLowerCase();
        }
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDriveTrain() {
        return driveTrain;
    }

    public void setDriveTrain(String driveTrain) {
        this.driveTrain = driveTrain;
    }

    public String getHeadUnitType() {
        return headUnitType;
    }

    public void setHeadUnitType(String headUnitType) {
        this.headUnitType = headUnitType;
    }

    public String getHeadUnitRaw() {
        return headUnitRaw;
    }

    public void setHeadUnitRaw(String headUnitRaw) {
        this.headUnitRaw = headUnitRaw;
    }

    public String getHmiVersion() {
        return hmiVersion;
    }

    public void setHmiVersion(String hmiVersion) {
        this.hmiVersion = hmiVersion;
    }

    public String getTelematicsUnit() {
        return telematicsUnit;
    }

    public void setTelematicsUnit(String telematicsUnit) {
        this.telematicsUnit = telematicsUnit;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public void setCountryOfOrigin(String countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
    }

    public String getLastFetched() {
        return lastFetched;
    }

    public void setLastFetched(String lastFetched) {
        this.lastFetched = lastFetched;
    }

    @Override
    public String toString() {
        return "VehicleAttributes [lastFetched=" + lastFetched + ", model=" + model + ", year=" + year + ", color="
                + color + ", brand=" + brand + ", driveTrain=" + driveTrain + ", headUnitType=" + headUnitType
                + ", headUnitRaw=" + headUnitRaw + ", hmiVersion=" + hmiVersion + ", telematicsUnit=" + telematicsUnit
                + ", bodyType=" + bodyType + ", countryOfOrigin=" + countryOfOrigin + "]";
    }
}
