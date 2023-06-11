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
package org.openhab.binding.wolfsmartset.internal.dto;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * generated with https://www.jsonschema2pojo.org/
 * 
 * @author Bo Biene - Initial contribution
 */
@Generated("jsonschema2pojo")
public class CreateSession2DTO {

    @SerializedName("CultureInfoCode")
    @Expose
    private String cultureInfoCode;
    @SerializedName("IsPasswordReset")
    @Expose
    private Boolean isPasswordReset;
    @SerializedName("IsProfessional")
    @Expose
    private Boolean isProfessional;
    @SerializedName("IsProfessionalPasswordReset")
    @Expose
    private Boolean isProfessionalPasswordReset;
    @SerializedName("BrowserSessionId")
    @Expose
    private Integer browserSessionId;

    public String getCultureInfoCode() {
        return cultureInfoCode;
    }

    public void setCultureInfoCode(String cultureInfoCode) {
        this.cultureInfoCode = cultureInfoCode;
    }

    public Boolean getIsPasswordReset() {
        return isPasswordReset;
    }

    public void setIsPasswordReset(Boolean isPasswordReset) {
        this.isPasswordReset = isPasswordReset;
    }

    public Boolean getIsProfessional() {
        return isProfessional;
    }

    public void setIsProfessional(Boolean isProfessional) {
        this.isProfessional = isProfessional;
    }

    public Boolean getIsProfessionalPasswordReset() {
        return isProfessionalPasswordReset;
    }

    public void setIsProfessionalPasswordReset(Boolean isProfessionalPasswordReset) {
        this.isProfessionalPasswordReset = isProfessionalPasswordReset;
    }

    public Integer getBrowserSessionId() {
        return browserSessionId;
    }

    public void setBrowserSessionId(Integer browserSessionId) {
        this.browserSessionId = browserSessionId;
    }
}
