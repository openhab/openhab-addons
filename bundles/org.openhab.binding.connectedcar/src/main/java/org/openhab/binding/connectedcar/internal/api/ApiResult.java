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
package org.openhab.binding.connectedcar.internal.api;

import static org.eclipse.jetty.http.HttpStatus.*;
import static org.openhab.binding.connectedcar.internal.util.Helpers.getString;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNApiError2;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPErrorResponse;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCActionResponse.WCApiError;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCActionResponse.WCApiError2;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * The {@link ApiResult} stores API result information.
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class ApiResult {
    public String url = "";
    public String method = "";
    public String response = "";
    private String location = "";
    public int httpCode = 0;
    public String httpReason = "";
    public String etag = "";
    public int rateLimit = -1;
    public HttpFields responseHeaders = new HttpFields();
    ApiErrorDTO apiError = new ApiErrorDTO();

    public ApiResult() {
    }

    public ApiResult(String url, String method) {
        this.method = method;
        this.url = url;
    }

    public ApiResult(String url, String method, Integer responseCode, String response) {
        this(method, url);
        this.response = response;
    }

    public ApiResult(@Nullable ContentResponse contentResponse) {
        fillFromResponse(contentResponse);
    }

    public ApiResult(@Nullable ContentResponse contentResponse, Throwable e) {
        fillFromResponse(contentResponse);
        response = response + "(" + e.toString() + ")";
    }

    public ApiResult(@Nullable Request request, Throwable e) {
        response = e.toString();
        if (request != null) {
            url = request.getURI().toString();
            method = request.getMethod();
        }
    }

    public ApiErrorDTO getApiError() {
        return apiError;
    }

    public boolean isHttpOk() {
        return httpCode == HttpStatus.OK_200 || httpCode == HttpStatus.ACCEPTED_202;
    }

    public boolean isHttpNotFound() {
        return httpCode == HttpStatus.NOT_FOUND_404;
    }

    public boolean isHttpUnauthorized() {
        return httpCode == UNAUTHORIZED_401 || httpCode == HttpStatus.FORBIDDEN_403
                || httpCode == HttpStatus.PROXY_AUTHENTICATION_REQUIRED_407;
    }

    public boolean isHttpServerError() {
        return httpCode >= INTERNAL_SERVER_ERROR_500 && httpCode <= INTERNAL_SERVER_ERROR_500 + 99;
    }

    public boolean isHttpNotModified() {
        return httpCode == HttpStatus.NOT_MODIFIED_304;
    }

    public boolean isHttpNoContent() {
        return httpCode == HttpStatus.NO_CONTENT_204;
    }

    public boolean isHttpTooManyRequests() {
        return httpCode == HttpStatus.TOO_MANY_REQUESTS_429;
    }

    public boolean isRedirect() {
        return httpCode == HttpStatus.MOVED_PERMANENTLY_301 || httpCode == HttpStatus.FOUND_302
                || httpCode == HttpStatus.TEMPORARY_REDIRECT_307;
    }

    public String getLocation() {
        return location;
    }

    public String getETag() {
        return etag;
    }

    public String getResponseDate() {
        String value = responseHeaders.get("Date");
        return value != null ? value : "";
    }

    private void fillFromResponse(@Nullable ContentResponse contentResponse) {
        if (contentResponse != null) {
            String r = contentResponse.getContentAsString();
            response = r != null ? r : "";
            httpCode = contentResponse.getStatus();
            httpReason = contentResponse.getReason();

            responseHeaders = contentResponse.getHeaders();
            location = getString(responseHeaders.get("Location"));
            etag = getString(responseHeaders.get("ETag"));
            if (responseHeaders.containsKey("X-RateLimit-Remaining")) {
                rateLimit = Integer.parseInt(responseHeaders.get("X-RateLimit-Remaining"));
            }

            Request request = contentResponse.getRequest();
            if (request != null) {
                url = request.getURI().toString();
                method = request.getMethod();
            }

            if (response.contains("\"error\":")) {
                Gson gson = new Gson();
                try {
                    // Try our best to extract details from brand specific formats
                    if (response.contains("Response: {\"httpStatus\"")) {
                        // FordPass
                        apiError = new ApiErrorDTO(gson.fromJson(response, FPErrorResponse.class));
                    } else if (response.contains("uri") && response.contains("status")
                            && response.contains("message")) {
                        // WeConnect v2
                        apiError = new ApiErrorDTO(gson.fromJson(response, WCApiError2.class));
                    } else if (response.contains("\"error\": {") && response.contains("\"group\": ")) {
                        // WeConnect
                        apiError = new ApiErrorDTO(gson.fromJson(response, WCApiError.class));
                    } else if (response.contains("\"error_code\": ")) {
                        // CarNet variant 1
                        ApiErrorDTO error = gson.fromJson(response, ApiErrorDTO.class);
                        if (error != null) {
                            apiError = error;
                        }
                    } else {
                        // CarNet variant 2
                        apiError = new ApiErrorDTO(gson.fromJson(response, CNApiError2.class));
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
