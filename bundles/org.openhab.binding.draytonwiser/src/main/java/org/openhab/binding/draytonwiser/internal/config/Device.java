/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.draytonwiser.internal.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class Device {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("ProductType")
    @Expose
    private String productType;
    @SerializedName("ProductIdentifier")
    @Expose
    private String productIdentifier;
    @SerializedName("ActiveFirmwareVersion")
    @Expose
    private String activeFirmwareVersion;
    @SerializedName("Manufacturer")
    @Expose
    private String manufacturer;
    @SerializedName("ModelIdentifier")
    @Expose
    private String modelIdentifier;
    @SerializedName("HardwareVersion")
    @Expose
    private String hardwareVersion;
    @SerializedName("SerialNumber")
    @Expose
    private String serialNumber;
    @SerializedName("ProductRange")
    @Expose
    private String productRange;
    @SerializedName("ProductModel")
    @Expose
    private String productModel;
    @SerializedName("ProductFamily")
    @Expose
    private String productFamily;
    @SerializedName("DisplayedSignalStrength")
    @Expose
    private String displayedSignalStrength;
    @SerializedName("BatteryVoltage")
    @Expose
    private Integer batteryVoltage;
    @SerializedName("BatteryLevel")
    @Expose
    private String batteryLevel;
    @SerializedName("Rssi")
    @Expose
    private Integer rssi;
    @SerializedName("Lqi")
    @Expose
    private Integer lqi;

    public Integer getId() {
        return id;
    }

    public String getProductType() {
        return productType;
    }

    public String getProductIdentifier() {
        return productIdentifier;
    }

    public String getActiveFirmwareVersion() {
        return activeFirmwareVersion;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModelIdentifier() {
        return modelIdentifier;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getProductRange() {
        return productRange;
    }

    public String getProductModel() {
        return productModel;
    }

    public String getProductFamily() {
        return productFamily;
    }

    public String getDisplayedSignalStrength() {
        return displayedSignalStrength;
    }

    public Integer getBatteryVoltage() {
        return batteryVoltage;
    }

    public String getBatteryLevel() {
        return batteryLevel;
    }

    public Integer getRssi() {
        return rssi;
    }

    public Integer getLqi() {
        return lqi;
    }

}
