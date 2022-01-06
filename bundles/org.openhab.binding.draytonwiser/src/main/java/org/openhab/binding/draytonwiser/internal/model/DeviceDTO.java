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

import com.google.gson.annotations.SerializedName;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class DeviceDTO {

    @SerializedName("id")
    private Integer id;
    private String productType;
    private String productIdentifier;
    private String activeFirmwareVersion;
    private String manufacturer;
    private String modelIdentifier;
    private String hardwareVersion;
    private String serialNumber;
    private String productRange;
    private String productModel;
    private String productFamily;
    private String displayedSignalStrength;
    private Integer batteryVoltage;
    private String batteryLevel;
    private Integer rssi;
    private Integer lqi;
    private ReceptionDTO receptionOfDevice;
    private ReceptionDTO receptionOfController;
    private Boolean deviceLockEnabled;

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
        if (rssi != null) {
            return rssi;
        }

        // JSON response changed with firmware update to include RSSI and LQI on a separate object
        return receptionOfDevice == null ? null : receptionOfDevice.getRSSI();
    }

    public Integer getLqi() {
        if (lqi != null) {
            return lqi;
        }

        return receptionOfDevice == null ? null : receptionOfDevice.getLQI();
    }

    public ReceptionDTO getReceptionOfController() {
        return receptionOfController;
    }

    public Boolean getDeviceLockEnabled() {
        return deviceLockEnabled;
    }
}
