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
package org.openhab.binding.wlanthermo.internal.api.esp32.dto.settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class System {

    @SerializedName("time")
    @Expose
    private String time;
    @SerializedName("unit")
    @Expose
    private String unit;
    @SerializedName("ap")
    @Expose
    private String ap;
    @SerializedName("host")
    @Expose
    private String host;
    @SerializedName("language")
    @Expose
    private String language;
    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("getupdate")
    @Expose
    private String getupdate;
    @SerializedName("autoupd")
    @Expose
    private Boolean autoupd;
    @SerializedName("prerelease")
    @Expose
    private Boolean prerelease;
    @SerializedName("hwversion")
    @Expose
    private String hwversion;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getAp() {
        return ap;
    }

    public void setAp(String ap) {
        this.ap = ap;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGetupdate() {
        return getupdate;
    }

    public void setGetupdate(String getupdate) {
        this.getupdate = getupdate;
    }

    public Boolean getAutoupd() {
        return autoupd;
    }

    public void setAutoupd(Boolean autoupd) {
        this.autoupd = autoupd;
    }

    public Boolean getPrerelease() {
        return prerelease;
    }

    public void setPrerelease(Boolean prerelease) {
        this.prerelease = prerelease;
    }

    public String getHwversion() {
        return hwversion;
    }

    public void setHwversion(String hwversion) {
        this.hwversion = hwversion;
    }
}
