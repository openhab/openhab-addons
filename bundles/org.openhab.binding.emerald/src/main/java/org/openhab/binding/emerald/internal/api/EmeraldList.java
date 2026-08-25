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
package org.openhab.binding.emerald.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class for holding the list of devices from the Emerald API.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class EmeraldList {
    public int code;
    public String message = "";

    @SerializedName("info")
    public @NonNullByDefault({}) Info info;

    public class Info {
        public @NonNullByDefault({}) Property[] property;
    }

    public class Property {
        public String id = "";

        @SerializedName("customer_id")
        public String customerId = "";

        @SerializedName("heat_pump")
        public @NonNullByDefault({}) Heatpump[] heatpump;
    }

    public class Heatpump {
        public String id = "";

        @SerializedName("serial_number")
        public String serialNumber = "";
        public String brand = "";
        public String model = "";

        @SerializedName("hw_version")
        public String hwVersion = "";

        @SerializedName("soft_version")
        public String softVersion = "";

        @SerializedName("mac_address")
        public String macAddress = "";

        @SerializedName("wifi_name")
        public String wifiName = "";
        public String status = "";

        @SerializedName("last_state")
        public @NonNullByDefault({}) LastState lastState;

        @SerializedName("device_type")
        public String deviceType = "";
    }

    public class LastState {
        public int mode;
        @SerializedName("switch")
        public String switchOn = "";

        @SerializedName("temp_set")
        public int tempSet;

        @SerializedName("temp_current")
        public int tempCurrent;
    }

    private EmeraldList() {
    }
}
