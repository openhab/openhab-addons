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
 * Digital stream such as Pandora, Airplay or Spotify
 **/
@Schema(description = "Digital stream such as Pandora, Airplay or Spotify ")
public class Stream {

    @Schema
    /**
     * Unique identifier
     **/
    private Integer id;

    @Schema(required = true)
    /**
     * Friendly name
     **/
    private String name;

    @Schema(required = true)
    /**
     * stream type * pandora * shairport * dlna * internetradio * spotify
     **/
    private String type;

    @Schema
    /**
     * User login
     **/
    private String user;

    @Schema
    /**
     * Password
     **/
    private String password;

    @Schema
    /**
     * Radio station identifier
     **/
    private String station;

    @Schema
    /**
     * Stream url, used for internetradio
     **/
    private String url;

    @Schema
    /**
     * Icon/Logo url, used for internetradio
     **/
    private String logo;

    @Schema
    /**
     * Additional info about the current audio playing from the stream (generated during playback
     **/
    private Object info;

    @Schema
    /**
     * State of the stream
     **/
    private String status;

    /**
     * Unique identifier
     *
     * @return id
     **/
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Stream id(Integer id) {
        this.id = id;
        return this;
    }

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

    public Stream name(String name) {
        this.name = name;
        return this;
    }

    /**
     * stream type * pandora * shairport * dlna * internetradio * spotify
     *
     * @return type
     **/
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Stream type(String type) {
        this.type = type;
        return this;
    }

    /**
     * User login
     *
     * @return user
     **/
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Stream user(String user) {
        this.user = user;
        return this;
    }

    /**
     * Password
     *
     * @return password
     **/
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Stream password(String password) {
        this.password = password;
        return this;
    }

    /**
     * Radio station identifier
     *
     * @return station
     **/
    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public Stream station(String station) {
        this.station = station;
        return this;
    }

    /**
     * Stream url, used for internetradio
     *
     * @return url
     **/
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Stream url(String url) {
        this.url = url;
        return this;
    }

    /**
     * Icon/Logo url, used for internetradio
     *
     * @return logo
     **/
    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Stream logo(String logo) {
        this.logo = logo;
        return this;
    }

    /**
     * Additional info about the current audio playing from the stream (generated during playback
     *
     * @return info
     **/
    public Object getInfo() {
        return info;
    }

    public void setInfo(Object info) {
        this.info = info;
    }

    public Stream info(Object info) {
        this.info = info;
        return this;
    }

    /**
     * State of the stream
     *
     * @return status
     **/
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Stream status(String status) {
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Stream {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    user: ").append(toIndentedString(user)).append("\n");
        sb.append("    password: ").append(toIndentedString(password)).append("\n");
        sb.append("    station: ").append(toIndentedString(station)).append("\n");
        sb.append("    url: ").append(toIndentedString(url)).append("\n");
        sb.append("    logo: ").append(toIndentedString(logo)).append("\n");
        sb.append("    info: ").append(toIndentedString(info)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
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
