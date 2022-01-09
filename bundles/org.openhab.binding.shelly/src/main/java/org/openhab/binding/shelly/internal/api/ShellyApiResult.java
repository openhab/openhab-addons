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
package org.openhab.binding.shelly.internal.api;

import static org.eclipse.jetty.http.HttpStatus.*;
import static org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;

/**
 * The {@link ShellyApiResult} wraps up the API result and provides some more information like url, http code, received
 * response etc.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyApiResult {
    public String url = "";
    public String method = "";
    public String response = "";
    public int httpCode = -1;
    public String httpReason = "";

    public ShellyApiResult() {
    }

    public ShellyApiResult(String method, String url) {
        this.method = method;
        this.url = url;
    }

    public ShellyApiResult(ContentResponse contentResponse) {
        fillFromResponse(contentResponse);
    }

    public String getUrl() {
        return !url.isEmpty() ? method + " " + url : "";
    }

    public String getHttpResponse() {
        return response;
    }

    @Override
    public String toString() {
        return getUrl() + " > " + getHttpResponse();
    }

    public boolean isHttpOk() {
        return httpCode == OK_200;
    }

    public boolean isNotFound() {
        return httpCode == NOT_FOUND_404;
    }

    public boolean isHttpAccessUnauthorized() {
        return (httpCode == UNAUTHORIZED_401 || response.contains(SHELLY_APIERR_UNAUTHORIZED));
    }

    public boolean isHttpTimeout() {
        return httpCode == -1 || response.toUpperCase().contains(SHELLY_APIERR_TIMEOUT.toLowerCase());
    }

    public boolean isHttpServerError() {
        return httpCode == INTERNAL_SERVER_ERROR_500;
    }

    public boolean isNotCalibrtated() {
        return getHttpResponse().contains(SHELLY_APIERR_NOT_CALIBRATED);
    }

    private void fillFromResponse(@Nullable ContentResponse contentResponse) {
        if (contentResponse != null) {
            String r = contentResponse.getContentAsString();
            response = r != null ? r : "";
            httpCode = contentResponse.getStatus();
            httpReason = contentResponse.getReason();

            Request request = contentResponse.getRequest();
            if (request != null) {
                url = request.getURI().toString();
                method = request.getMethod();
            }
        }
    }
}
