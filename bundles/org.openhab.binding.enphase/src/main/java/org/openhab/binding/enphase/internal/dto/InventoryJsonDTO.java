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
package org.openhab.binding.enphase.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class InventoryJsonDTO {

    public class DeviceDTO {
        public String type;

        @SerializedName("part_num")
        public String partNumber;
        @SerializedName("serial_num")
        public String serialNumber;

        @SerializedName("device_status")
        private String[] deviceStatus;
        @SerializedName("last_rpt_date")
        public String lastReportDate;
        public boolean producing;
        public boolean communicating;
        public boolean provisioned;
        public boolean operating;
        // NSRB data
        public String relay;
        @SerializedName("line1-connected")
        public boolean line1Connected;
        @SerializedName("line2-connected")
        public boolean line2Connected;
        @SerializedName("line3-connected")
        public boolean line3Connected;

        public String getSerialNumber() {
            return serialNumber;
        }

        public String getDeviceStatus() {
            return deviceStatus == null || deviceStatus.length == 0 ? "" : deviceStatus[0];
        }
    }

    public String type;
    public DeviceDTO[] devices;
}
