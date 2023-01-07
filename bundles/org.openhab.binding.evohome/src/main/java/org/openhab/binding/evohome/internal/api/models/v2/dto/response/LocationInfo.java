/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.evohome.internal.api.models.v2.dto.response;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for the location info
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public class LocationInfo {

    @SerializedName("locationId")
    private String locationId;

    @SerializedName("name")
    private String name;

    @SerializedName("streetAddress")
    private String streetAddress;

    @SerializedName("city")
    private String city;

    @SerializedName("country")
    private String country;

    @SerializedName("postcode")
    private String postcode;

    @SerializedName("locationType")
    private String locationType;

    @SerializedName("useDaylightSaveSwitching")
    private boolean useDaylightSaveSwitching;

    @SerializedName("timeZone")
    private TimeZone timeZone;

    @SerializedName("locationOwner")
    private LocationOwner locationOwner;

    public String getLocationId() {
        return locationId;
    }

    public String getName() {
        return name;
    }
}
