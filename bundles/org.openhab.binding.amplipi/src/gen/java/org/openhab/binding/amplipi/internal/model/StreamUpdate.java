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
package org.openhab.binding.amplipi.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Reconfiguration of a Stream
 **/
@Schema(description = "Reconfiguration of a Stream ")
public class StreamUpdate {

    @Schema
    /**
     * Friendly name
     **/
    private String name;

    @Schema
    private String user;

    @Schema
    private String password;

    @Schema
    private String station;

    @Schema
    private String url;

    @Schema
    private String logo;

    /**
     * Friendly name
     *
     * @return name
     **/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StreamUpdate name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get user
     *
     * @return user
     **/
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public StreamUpdate user(String user) {
        this.user = user;
        return this;
    }

    /**
     * Get password
     *
     * @return password
     **/
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public StreamUpdate password(String password) {
        this.password = password;
        return this;
    }

    /**
     * Get station
     *
     * @return station
     **/
    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public StreamUpdate station(String station) {
        this.station = station;
        return this;
    }

    /**
     * Get url
     *
     * @return url
     **/
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public StreamUpdate url(String url) {
        this.url = url;
        return this;
    }

    /**
     * Get logo
     *
     * @return logo
     **/
    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public StreamUpdate logo(String logo) {
        this.logo = logo;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class StreamUpdate {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    user: ").append(toIndentedString(user)).append("\n");
        sb.append("    password: ").append(toIndentedString(password)).append("\n");
        sb.append("    station: ").append(toIndentedString(station)).append("\n");
        sb.append("    url: ").append(toIndentedString(url)).append("\n");
        sb.append("    logo: ").append(toIndentedString(logo)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
