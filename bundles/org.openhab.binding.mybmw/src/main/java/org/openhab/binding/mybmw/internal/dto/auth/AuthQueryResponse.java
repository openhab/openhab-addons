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
package org.openhab.binding.mybmw.internal.dto.auth;

import java.util.List;

/**
 * The {@link AuthQueryResponse} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class AuthQueryResponse {
    public String clientName;// ": "mybmwapp",
    public String clientSecret;// ": "c0e3393d-70a2-4f6f-9d3c-8530af64d552",
    public String clientId;// ": "31c357a0-7a1d-4590-aa99-33b97244d048",
    public String gcdmBaseUrl;// ": "https://customer.bmwgroup.com",
    public String returnUrl;// ": "com.bmw.connected://oauth",
    public String brand;// ": "bmw",
    public String language;// ": "en",
    public String country;// ": "US",
    public String authorizationEndpoint;// ": "https://customer.bmwgroup.com/oneid/login",
    public String tokenEndpoint;// ": "https://customer.bmwgroup.com/gcdm/oauth/token",
    public List<String> scopes;// ;": [
    // "openid",
    // "profile",
    // "email",
    // "offline_access",
    // "smacc",
    // "vehicle_data",
    // "perseus",
    // "dlm",
    // "svds",
    // "cesim",
    // "vsapi",
    // "remote_services",
    // "fupo",
    // "authenticate_user"
    // ],
    public List<String> promptValues; // ": ["login"]
}
