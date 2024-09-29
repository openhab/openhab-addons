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
public class VehicleTireStateDetails {
    private String dimension = ""; // 225/45 R18 95V XL,
    private String treadDesign = ""; // Winter Contact TS 860 S SSR,
    private String manufacturer = ""; // Continental,
    private int manufacturingWeek = -1; // 5299,
    private boolean isOptimizedForOemBmw = false; // true,
    private String partNumber = ""; // 2471558,
    private VehicleTireStateDetailsClassification speedClassification; //
    private String mountingDate = ""; // 2022-10-06T00:00:00.000Z,
    private int season = -1; // 4,
    private boolean identificationInProgress = false; // false

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getTreadDesign() {
        return treadDesign;
    }

    public void setTreadDesign(String treadDesign) {
        this.treadDesign = treadDesign;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int getManufacturingWeek() {
        return manufacturingWeek;
    }

    public void setManufacturingWeek(int manufacturingWeek) {
        this.manufacturingWeek = manufacturingWeek;
    }

    public boolean isOptimizedForOemBmw() {
        return isOptimizedForOemBmw;
    }

    public void setOptimizedForOemBmw(boolean isOptimizedForOemBmw) {
        this.isOptimizedForOemBmw = isOptimizedForOemBmw;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public VehicleTireStateDetailsClassification getSpeedClassification() {
        return speedClassification;
    }

    public void setSpeedClassification(VehicleTireStateDetailsClassification speedClassification) {
        this.speedClassification = speedClassification;
    }

    public String getMountingDate() {
        return mountingDate;
    }

    public void setMountingDate(String mountingDate) {
        this.mountingDate = mountingDate;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public boolean isIdentificationInProgress() {
        return identificationInProgress;
    }

    public void setIdentificationInProgress(boolean identificationInProgress) {
        this.identificationInProgress = identificationInProgress;
    }

    @Override
    public String toString() {
        return "VehicleTireStateDetails [dimension=" + dimension + ", treadDesign=" + treadDesign + ", manufacturer="
                + manufacturer + ", manufacturingWeek=" + manufacturingWeek + ", isOptimizedForOemBmw="
                + isOptimizedForOemBmw + ", partNumber=" + partNumber + ", speedClassification=" + speedClassification
                + ", mountingDate=" + mountingDate + ", season=" + season + ", identificationInProgress="
                + identificationInProgress + "]";
    }
}
