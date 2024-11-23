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
package org.openhab.binding.boschindego.internal.dto.response;

import java.time.Instant;

import com.google.gson.annotations.SerializedName;

/**
 * Response for serial number and other device service properties.
 * 
 * @author Jacob Laursen - Initial contribution
 */
public class DevicePropertiesResponse {

    @SerializedName("alm_sn")
    public String serialNumber = "";

    @SerializedName("service_counter")
    public int serviceCounter;

    @SerializedName("needs_service")
    public boolean needsService;

    /**
     * Mode: manual, smart
     */
    @SerializedName("alm_mode")
    public String mode;

    @SerializedName("bareToolnumber")
    public String bareToolNumber;

    @SerializedName("alm_firmware_version")
    public String firmwareVersion;

    @SerializedName("renew_date")
    public Instant renewDate;
}
