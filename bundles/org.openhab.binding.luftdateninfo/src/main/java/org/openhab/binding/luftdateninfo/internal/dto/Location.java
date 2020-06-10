/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

/**
 * The {@link LuftdatenInfo} class definition for Logging identification
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Location {
    private Integer id;
    private String country;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }

    public Integer getIndoor() {
        return indoor;
    }

    public void setIndoor(Integer indoor) {
        this.indoor = indoor;
    }

    public Integer getExact_location() {
        return exact_location;
    }

    public void setExact_location(Integer exact_location) {
        this.exact_location = exact_location;
    }

    private String altitude;
    private String latitude;
    private String longtitude;
    private Integer indoor;
    private Integer exact_location;
    /**
     * "id": 11447,
     * "country": "DE",
     * "altitude": "151.5",
     * "latitude": "50.562",
     * "longitude": "8.504",
     * "indoor": 0,
     * "exact_location": 0
     *
     */
}
