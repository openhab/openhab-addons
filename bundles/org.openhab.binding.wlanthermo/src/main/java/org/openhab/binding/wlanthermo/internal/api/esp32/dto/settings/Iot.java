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
package org.openhab.binding.wlanthermo.internal.api.esp32.dto.settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class Iot {

    @SerializedName("PMQhost")
    @Expose
    private String pMQhost;
    @SerializedName("PMQport")
    @Expose
    private Integer pMQport;
    @SerializedName("PMQuser")
    @Expose
    private String pMQuser;
    @SerializedName("PMQpass")
    @Expose
    private String pMQpass;
    @SerializedName("PMQqos")
    @Expose
    private Integer pMQqos;
    @SerializedName("PMQon")
    @Expose
    private Boolean pMQon;
    @SerializedName("PMQint")
    @Expose
    private Integer pMQint;
    @SerializedName("CLon")
    @Expose
    private Boolean cLon;
    @SerializedName("CLtoken")
    @Expose
    private String cLtoken;
    @SerializedName("CLint")
    @Expose
    private Integer cLint;
    @SerializedName("CLurl")
    @Expose
    private String cLurl;

    public String getPMQhost() {
        return pMQhost;
    }

    public void setPMQhost(String pMQhost) {
        this.pMQhost = pMQhost;
    }

    public Integer getPMQport() {
        return pMQport;
    }

    public void setPMQport(Integer pMQport) {
        this.pMQport = pMQport;
    }

    public String getPMQuser() {
        return pMQuser;
    }

    public void setPMQuser(String pMQuser) {
        this.pMQuser = pMQuser;
    }

    public String getPMQpass() {
        return pMQpass;
    }

    public void setPMQpass(String pMQpass) {
        this.pMQpass = pMQpass;
    }

    public Integer getPMQqos() {
        return pMQqos;
    }

    public void setPMQqos(Integer pMQqos) {
        this.pMQqos = pMQqos;
    }

    public Boolean getPMQon() {
        return pMQon;
    }

    public void setPMQon(Boolean pMQon) {
        this.pMQon = pMQon;
    }

    public Integer getPMQint() {
        return pMQint;
    }

    public void setPMQint(Integer pMQint) {
        this.pMQint = pMQint;
    }

    public Boolean getCLon() {
        return cLon;
    }

    public void setCLon(Boolean cLon) {
        this.cLon = cLon;
    }

    public String getCLtoken() {
        return cLtoken;
    }

    public void setCLtoken(String cLtoken) {
        this.cLtoken = cLtoken;
    }

    public Integer getCLint() {
        return cLint;
    }

    public void setCLint(Integer cLint) {
        this.cLint = cLint;
    }

    public String getCLurl() {
        return cLurl;
    }

    public void setCLurl(String cLurl) {
        this.cLurl = cLurl;
    }
}
