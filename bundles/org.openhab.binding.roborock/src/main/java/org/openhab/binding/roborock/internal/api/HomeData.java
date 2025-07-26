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
package org.openhab.binding.roborock.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class for holding the set of parameters used by the HomeData response
 *
 * @author Paul Smedley - Initial Contribution
 *
 */

@NonNullByDefault
public class HomeData {
    public String api = "";
    public @NonNullByDefault({}) Result result;
    public String status = "";
    public boolean success;

    public class Result {
        public int id;
        public String name = "";
        public @NonNullByDefault({}) Products[] products;
        public @NonNullByDefault({}) Devices[] devices;
        public @NonNullByDefault({}) Rooms[] rooms;
        public String status = "";
        public boolean success;
    }

    public class Products {
        public String id = "";
        public String name = "";
        public String model = "";
        public String category = "";
        public @NonNullByDefault({}) Schema[] schema;
    }

    public class Schema {
        public int id;
        public String name = "";
        public String code = "";
        public String mode = "";
        public String type = "";
        public String property = "";
    }

    public class Devices {
        public String duid = "";
        public String name = "";
        public String localKey = "";
        public String productId = "";
        public boolean online;
        public String sn = "";
        public @NonNullByDefault({}) DeviceStatus deviceStatus;
        public boolean silentOtaSwitch;
        public boolean f;
    }

    public class DeviceStatus {
        @SerializedName("120")
        public int errorCode;
        @SerializedName("121")
        public int vacuumState;
        @SerializedName("122")
        public int battery;
        @SerializedName("123")
        public int fanPower;
        @SerializedName("124")
        public int waterBoxMode;
        @SerializedName("125")
        public int mainBrushWorkTime;
        @SerializedName("126")
        public int sideBrushWorkTime;
        @SerializedName("127")
        public int filterWorkTime;
        @SerializedName("128")
        public int additionProps;
        @SerializedName("133")
        public int chargeStatus;
        @SerializedName("134")
        public int dryingStatus;
    }

    public class Rooms {
        public int id;
        public String name = "";
    }

    public HomeData() {
    }
}
