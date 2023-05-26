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
package org.openhab.binding.lgthinq.internal.api;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.V2_EMP_SESS_PATH;
import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.V2_EMP_SESS_URL;

import java.io.Serializable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.api.model.GatewayResult;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The {@link LGThinqGateway} hold informations about the LG Gateway
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinqGateway implements Serializable {
    private String empBaseUri = "";
    private String loginBaseUri = "";
    private String apiRootV1 = "";
    private String apiRootV2 = "";
    private String authBase = "";
    private String language = "";
    private String country = "";
    private String username = "";
    private String password = "";
    private String alternativeEmpServer = "";
    private int accountVersion;

    public LGThinqGateway() {
    }

    public LGThinqGateway(GatewayResult gwResult, String language, String country, String alternativeEmpServer) {
        this.apiRootV2 = gwResult.getThinq2Uri();
        this.apiRootV1 = gwResult.getThinq1Uri();
        this.loginBaseUri = gwResult.getEmpSpxUri();
        this.authBase = gwResult.getEmpUri();
        this.empBaseUri = gwResult.getEmpTermsUri();
        this.language = language;
        this.country = country;
        this.alternativeEmpServer = alternativeEmpServer;
    }

    @JsonIgnore
    public String getTokenSessionEmpUrl() {
        return alternativeEmpServer.isBlank() ? V2_EMP_SESS_URL : alternativeEmpServer + V2_EMP_SESS_PATH;
    }

    public String getEmpBaseUri() {
        return empBaseUri;
    }

    public int getAccountVersion() {
        return accountVersion;
    }

    public String getApiRootV2() {
        return apiRootV2;
    }

    public String getAuthBase() {
        return authBase;
    }

    public String getLanguage() {
        return language;
    }

    public String getCountry() {
        return country;
    }

    public String getLoginBaseUri() {
        return loginBaseUri;
    }

    public String getApiRootV1() {
        return apiRootV1;
    }

    public void setEmpBaseUri(String empBaseUri) {
        this.empBaseUri = empBaseUri;
    }

    public void setLoginBaseUri(String loginBaseUri) {
        this.loginBaseUri = loginBaseUri;
    }

    public void setApiRootV1(String apiRootV1) {
        this.apiRootV1 = apiRootV1;
    }

    public void setApiRootV2(String apiRootV2) {
        this.apiRootV2 = apiRootV2;
    }

    public void setAuthBase(String authBase) {
        this.authBase = authBase;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
