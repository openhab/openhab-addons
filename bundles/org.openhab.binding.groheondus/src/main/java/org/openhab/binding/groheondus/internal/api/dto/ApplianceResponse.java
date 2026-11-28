/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.groheondus.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Appliance as returned by the dashboard and appliance endpoints.
 *
 * @author Arne Seime - Initial contribution
 */
public class ApplianceResponse {
    @SerializedName("appliance_id")
    public String applianceId;
    public String name;
    public Integer type;
    @SerializedName("serial_number")
    public String serialNumber;
    /** Firmware version */
    public String version;
    @SerializedName("installation_date")
    public String installationDate;
    /** Time of the last data transmission from the device, ISO 8601 with offset */
    public String tdt;
    public Config config;

    public static class Config {
        @SerializedName("measurement_period")
        public Integer measurementPeriod;
        @SerializedName("measurement_transmission_intervall")
        public Integer measurementTransmissionInterval;
    }
}
