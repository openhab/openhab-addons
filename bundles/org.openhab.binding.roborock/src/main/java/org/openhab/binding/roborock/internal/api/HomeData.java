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
 * Class for holding the set of parameters used to read the controller variables.
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
        public String sn = "";
        public @NonNullByDefault({}) DeviceStatus deviceStatus;
        public boolean silentOtaSwitch;
        public boolean f;
    }

    public class DeviceStatus {
        @SerializedName("120")
        public int onetwenty;
        @SerializedName("121")
        public int onetwentyone;
        @SerializedName("122")
        public int onetwentytwo;
        @SerializedName("123")
        public int onetwentythree;
        @SerializedName("124")
        public int onetwentyfour;
        @SerializedName("125")
        public int onetwentyfive;
        @SerializedName("126")
        public int onetwentysix;
        @SerializedName("127")
        public int onetwentyseven;
        @SerializedName("128")
        public int onetwentyeight;
        @SerializedName("133")
        public int onethirtythree;
        @SerializedName("134")
        public int onethirtyfour;
    }

    public class Rooms {
        public int id;
        public String name = "";
    }

    public HomeData() {
    }
}
