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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

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
        public @NonNullByDefault({}) Property[] property = new Property[0];

        @SerializedName("shared_property")
        public @NonNullByDefault({}) Property[] sharedProperty = new Property[0];
    }

    /**
     * Returns all properties associated with the account, including shared properties.
     */
    public List<Property> getAllProperties() {
        List<Property> all = new java.util.ArrayList<>();
        if (info != null) {
            if (info.property != null) {
                all.addAll(java.util.Arrays.asList(info.property));
            }
            if (info.sharedProperty != null) {
                all.addAll(java.util.Arrays.asList(info.sharedProperty));
            }
        }
        return all;
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

    public record HeatpumpContext(Property property, Heatpump heatpump) {
    }

    /**
     * Finds a heat pump and its parent property by UUID.
     */
    public @Nullable HeatpumpContext findHeatpump(String uuid) {
        for (Property prop : getAllProperties()) {
            if (prop.heatpump != null) {
                for (Heatpump hp : prop.heatpump) {
                    if (uuid.equals(hp.id)) {
                        return new HeatpumpContext(prop, hp);
                    }
                }
            }
        }
        return null;
    }
}
