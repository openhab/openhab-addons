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
package org.openhab.binding.enphase.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Data class for the per IQ Battery power data from the {@code /ivp/ensemble/power} api call.
 *
 * @author Cedric Boon - Initial contribution
 */
public class EnsemblePowerDTO {

    public static class DevicePowerDTO {
        @SerializedName("serial_num")
        public String serialNumber;
        /**
         * Active power in milliwatts. A positive value means the battery is discharging, negative means charging.
         */
        @SerializedName("real_power_mw")
        public Long realPowerMw;
    }

    // The Envoy returns the key with a trailing colon. The key without colon is accepted in case this gets fixed.
    @SerializedName(value = "devices:", alternate = { "devices" })
    public DevicePowerDTO[] devices;
}
