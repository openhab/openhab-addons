/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.jellyfin.internal.api.generated.auth;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class HttpBearerAuth implements Authentication {
    private final String scheme;
    private String bearerToken;

    public HttpBearerAuth(String scheme) {
        this.scheme = scheme;
    }

    /**
     * Gets the token, which together with the scheme, will be sent as the value of the Authorization header.
     *
     * @return The bearer token
     */
    public String getBearerToken() {
        return bearerToken;
    }

    /**
     * Sets the token, which together with the scheme, will be sent as the value of the Authorization header.
     *
     * @param bearerToken The bearer token to send in the Authorization header
     */
    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    @Override
    public void applyToParams(List<Pair> queryParams, Map<String, String> headerParams,
            Map<String, String> cookieParams, String payload, String method, URI uri) throws ApiException {
        if (bearerToken == null) {
            return;
        }

        headerParams.put("Authorization", (scheme != null ? upperCaseBearer(scheme) + " " : "") + bearerToken);
    }

    private static String upperCaseBearer(String scheme) {
        return ("bearer".equalsIgnoreCase(scheme)) ? "Bearer" : scheme;
    }
}
