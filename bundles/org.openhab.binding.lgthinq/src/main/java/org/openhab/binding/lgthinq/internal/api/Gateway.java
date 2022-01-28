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
package org.openhab.binding.lgthinq.internal.api;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Gateway} hold informations about the LG Gateway
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class Gateway implements Serializable {
    private String empBaseUri = "";
    private String loginBaseUri = "";
    private String apiRootV1 = "";
    private String apiRootV2 = "";
    private String authBase = "";
    private String language = "";
    private String country = "";
    private String username = "";
    private String password = "";

    public Gateway() {
    }

    public Gateway(Map<String, String> params, String language, String country) {
        this.apiRootV2 = Objects.requireNonNullElse(params.get("thinq2Uri"), "");
        this.apiRootV1 = Objects.requireNonNullElse(params.get("thinq1Uri"), "");
        this.loginBaseUri = Objects.requireNonNullElse(params.get("empSpxUri"), "");
        this.authBase = Objects.requireNonNullElse(params.get("empUri"), "");
        this.empBaseUri = Objects.requireNonNullElse(params.get("empTermsUri"), "");
        this.language = language;
        this.country = country;
    }

    public String getEmpBaseUri() {
        return empBaseUri;
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
