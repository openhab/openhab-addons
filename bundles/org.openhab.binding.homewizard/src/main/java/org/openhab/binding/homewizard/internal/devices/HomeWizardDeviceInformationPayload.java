/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homewizard.internal.devices;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class that provides storage for the json objects obtained from HomeWizard devices.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 *
 */
@NonNullByDefault
public class HomeWizardDeviceInformationPayload {
    private String productName = "";
    private String productType = "";
    private String serial = "";
    private String firmwareVersion = "";
    private String apiVersion = "";

    /**
     * Getter for the product name
     *
     * @return The product name obtained from the Device Information API
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Getter for the product type
     *
     * @return The product type obtained from the Device Information API
     */
    public String getProductType() {
        return productType;
    }

    /**
     * Getter for the serial number
     *
     * @return The product serial number obtained from the Device Information API
     */
    public String getSerialNumber() {
        return serial;
    }

    /**
     * Getter for the firmware version
     *
     * @return The firmware version obtained from the Device Information API
     */
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    /**
     * Getter for the api version
     *
     * @return The api version obtained from the Device Information API
     */
    public String getApiVersion() {
        return apiVersion;
    }

    @Override
    public String toString() {
        return String.format("""
                Data [productName: %s productType: %s serial: %s firmwareVersion: %s apiVersion: %s]
                """, productName, productType, serial, firmwareVersion, apiVersion);
    }
}
