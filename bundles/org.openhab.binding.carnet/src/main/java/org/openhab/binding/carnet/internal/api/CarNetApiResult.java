/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.carnet.internal.api;

import static org.eclipse.jetty.http.HttpStatus.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.carnet.internal.api.CarNetApiErrorDTO.CNApiError2;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * The {@link CarNetApiResult} stores API result information.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetApiResult {
    public String url = "";
    public String method = "";
    public String response = "";
    public Integer httpCode = 0;
    public String httpReason = "";
    CarNetApiErrorDTO apiError = new CarNetApiErrorDTO();

    public CarNetApiResult() {
    }

    public CarNetApiResult(String url, String method) {
        this.method = method;
        this.url = url;
    }

    public CarNetApiResult(String url, String method, Integer responseCode, String response) {
        this.method = method;
        this.url = url;
        this.httpCode = 0;
        this.response = response;
    }

    public CarNetApiResult(@Nullable ContentResponse contentResponse) {
        fillFromResponse(contentResponse);
    }

    public CarNetApiResult(@Nullable ContentResponse contentResponse, Throwable e) {
        fillFromResponse(contentResponse);
        response = response + "(" + e.toString() + ")";
    }

    public CarNetApiResult(@Nullable Request request, Throwable e) {
        response = e.toString();
        if (request != null) {
            url = request.getURI().toString();
            method = request.getMethod();
        }
    }

    public CarNetApiErrorDTO getApiError() {
        return apiError;
    }

    public boolean isHttpOk() {
        return httpCode == HttpStatus.OK_200 || httpCode == HttpStatus.ACCEPTED_202;
    }

    public boolean isHttpUnauthorized() {
        return (httpCode == UNAUTHORIZED_401 || httpCode == HttpStatus.FORBIDDEN_403
                || httpCode == HttpStatus.PROXY_AUTHENTICATION_REQUIRED_407);
    }

    public boolean isHttpServerError() {
        return (httpCode >= INTERNAL_SERVER_ERROR_500) && (httpCode <= INTERNAL_SERVER_ERROR_500 + 99);
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

            if (response.contains("\"error\":")) {
                Gson gson = new Gson();
                try {
                    if (response.contains("\"error_code\": ")) {
                        CarNetApiErrorDTO error = gson.fromJson(response, CarNetApiErrorDTO.class);
                        if (error != null) {
                            apiError = error;
                        }
                    } else {
                        CNApiError2 error2 = gson.fromJson(response, CNApiError2.class);
                        if (error2 != null) {
                            apiError = new CarNetApiErrorDTO(error2);
                        }
                    }
                } catch (JsonParseException e) {
                    apiError.error = "Unable to parse '" + response + "'";
                    String message = e.getMessage();
                    apiError.description = message != null ? message : "";
                }
            }
        }
    }
}
