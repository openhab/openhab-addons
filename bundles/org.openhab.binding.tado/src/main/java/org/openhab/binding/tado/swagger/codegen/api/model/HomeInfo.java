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
package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * Static imported copy of the Java file originally created by Swagger Codegen.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class HomeInfo {
    @SerializedName("id")
    private Integer id = null;

    @SerializedName("name")
    private String name = null;

    @SerializedName("dateTimeZone")
    private String dateTimeZone = null;

    @SerializedName("temperatureUnit")
    private TemperatureUnit temperatureUnit = null;

    @SerializedName("geolocation")
    private Geolocation geolocation = null;

    @SerializedName("awayRadiusInMeters")
    private Double awayRadiusInMeters = null;

    public HomeInfo id(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public HomeInfo name(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HomeInfo dateTimeZone(String dateTimeZone) {
        this.dateTimeZone = dateTimeZone;
        return this;
    }

    public String getDateTimeZone() {
        return dateTimeZone;
    }

    public void setDateTimeZone(String dateTimeZone) {
        this.dateTimeZone = dateTimeZone;
    }

    public HomeInfo temperatureUnit(TemperatureUnit temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
        return this;
    }

    public TemperatureUnit getTemperatureUnit() {
        return temperatureUnit;
    }

    public void setTemperatureUnit(TemperatureUnit temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }

    public HomeInfo geolocation(Geolocation geolocation) {
        this.geolocation = geolocation;
        return this;
    }

    public Geolocation getGeolocation() {
        return geolocation;
    }

    public void setGeolocation(Geolocation geolocation) {
        this.geolocation = geolocation;
    }

    public HomeInfo awayRadiusInMeters(Double awayRadiusInMeters) {
        this.awayRadiusInMeters = awayRadiusInMeters;
        return this;
    }

    public Double getAwayRadiusInMeters() {
        return awayRadiusInMeters;
    }

    public void setAwayRadiusInMeters(Double awayRadiusInMeters) {
        this.awayRadiusInMeters = awayRadiusInMeters;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HomeInfo homeInfo = (HomeInfo) o;
        return Objects.equals(this.id, homeInfo.id) && Objects.equals(this.name, homeInfo.name)
                && Objects.equals(this.dateTimeZone, homeInfo.dateTimeZone)
                && Objects.equals(this.temperatureUnit, homeInfo.temperatureUnit)
                && Objects.equals(this.geolocation, homeInfo.geolocation)
                && Objects.equals(this.awayRadiusInMeters, homeInfo.awayRadiusInMeters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dateTimeZone, temperatureUnit, geolocation, awayRadiusInMeters);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class HomeInfo {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    dateTimeZone: ").append(toIndentedString(dateTimeZone)).append("\n");
        sb.append("    temperatureUnit: ").append(toIndentedString(temperatureUnit)).append("\n");
        sb.append("    geolocation: ").append(toIndentedString(geolocation)).append("\n");
        sb.append("    awayRadiusInMeters: ").append(toIndentedString(awayRadiusInMeters)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
