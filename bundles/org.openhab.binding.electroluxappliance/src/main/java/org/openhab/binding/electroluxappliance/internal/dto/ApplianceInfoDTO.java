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
package org.openhab.binding.electroluxappliance.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ApplianceInfoDTO} class defines the DTO for the Electrolux Appliance Info.
 *
 * @author Jan Gustafsson - Initial contribution
 */

@NonNullByDefault
public class ApplianceInfoDTO {

    private ApplianceInfo applianceInfo = new ApplianceInfo();

    // Map capabilities to Object, so details are not parsed
    private Object capabilities = new Object();

    public ApplianceInfo getApplianceInfo() {
        return applianceInfo;
    }

    public Object getCapabilities() {
        return capabilities;
    }

    public static class ApplianceInfo {
        private String serialNumber = "";
        private String pnc = "";
        private String brand = "";
        private String deviceType = "";
        private String model = "";
        private String variant = "";
        private String colour = "";

        // Getters
        public String getSerialNumber() {
            return serialNumber;
        }

        public String getPnc() {
            return pnc;
        }

        public String getBrand() {
            return brand;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public String getModel() {
            return model;
        }

        public String getVariant() {
            return variant;
        }

        public String getColour() {
            return colour;
        }

        @Override
        public String toString() {
            return "ApplianceInfo{" + "serialNumber='" + serialNumber + '\'' + ", pnc='" + pnc + '\'' + ", brand='"
                    + brand + '\'' + ", deviceType='" + deviceType + '\'' + ", model='" + model + '\'' + ", variant='"
                    + variant + '\'' + ", colour='" + colour + '\'' + '}';
        }
    }
}
