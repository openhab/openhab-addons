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
package org.openhab.binding.ondilo.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Pool} DTO for representing Ondilo pools.
 *
 * @author MikeTheTux - Initial contribution
 */
public class Pool {
    /*
     * Example JSON representation:
     * {
     * "id": 234,
     * "name": "John's Pool",
     * "type": "outdoor_inground_pool",
     * "volume": 15,
     * "disinfection": {
     * "primary": "chlorine",
     * "secondary": {
     * "uv_sanitizer": true,
     * "ozonator": false
     * }
     * },
     * "address": {
     * "street": "162 Avenue Robert Schuman",
     * "zipcode": "13760",
     * "city": "Saint-Cannat",
     * "country": "France",
     * "latitude": 43.612282,
     * "longitude": 5.3179397
     * },
     * "updated_at": "2019-11-27T23:00:21+0000"
     * }
     */
    public int id;
    public String name;
    public String type;
    public float volume;
    public Disinfection disinfection;
    public Address address;

    @SerializedName("updated_at")
    public String updatedAt;

    public static class Disinfection {
        public String primary;
        public Secondary secondary;

        public static class Secondary {
            @SerializedName("uv_sanitizer")
            public boolean uvSanitizer;
            public boolean ozonator;
        }
    }

    public static class Address {
        public String street;
        public String zipcode;
        public String city;
        public String country;
        public double latitude;
        public double longitude;
    }

    public String getLocation() {
        return address.latitude + ", " + address.longitude;
    }

    public String getDisinfection() {
        return disinfection.primary + " (UV:" + disinfection.secondary.uvSanitizer + ", Ozone:"
                + disinfection.secondary.ozonator + ")";
    }

    public String getAddress() {
        return address.street + ", " + address.zipcode + " " + address.city + ", " + address.country;
    }

    public String getVolume() {
        return volume + " m³";
    }
}
