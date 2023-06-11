/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.luftdateninfo.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link LuftdatenInfo} class definition for Logging identification
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Location {
    private int id;
    private String country;
    private String altitude;
    private String latitude;
    private String longitude;
    private int indoor;
    @SerializedName("exact_location")
    private int exactLocation;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Integer getIndoor() {
        return indoor;
    }

    public void setIndoor(int indoor) {
        this.indoor = indoor;
    }

    public int getExactLocation() {
        return exactLocation;
    }

    public void setExactLocation(int exactLocation) {
        this.exactLocation = exactLocation;
    }
}
