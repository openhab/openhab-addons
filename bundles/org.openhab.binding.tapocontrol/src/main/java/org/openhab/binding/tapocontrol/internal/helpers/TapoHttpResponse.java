/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.helpers;

import static org.eclipse.jetty.http.HttpStatus.*;
import static org.openhab.binding.tapocontrol.internal.TapoControlBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;

/**
 * The {@link TapoHttpResponse} wraps up the API result and provides some more information like url, http code, received
 * response etc.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoHttpResponse {
    private String url = "";
    private String method = "";
    private String response = "";
    private int httpCode = -1;
    private String httpReason = "";
    private Map httpHeaders = new HashMap();
    private boolean isValidResponse = false;

    /**
     * Init Class
     *
     */
    public TapoHttpResponse() {
    }

    public TapoHttpResponse(String method, String url) {
        this.method = method;
        this.url = url;
    }

    public TapoHttpResponse(ContentResponse contentResponse) {
        fillFromResponse(contentResponse);
        this.isValidResponse = validateResponse(response);
    }

    /***********************************
     *
     * Functions
     *
     ************************************/

    /**
     * Fill from ContentResponse
     * 
     * @param ContentResponse
     */
    private void fillFromResponse(@Nullable ContentResponse contentResponse) {
        if (contentResponse != null) {
            String r = contentResponse.getContentAsString();
            response = r != null ? r : "";
            httpCode = contentResponse.getStatus();
            httpReason = contentResponse.getReason();
            httpHeaders = convertHeaderToMap(contentResponse.getHeaders());

            Request request = contentResponse.getRequest();
            if (request != null) {
                url = request.getURI().toString();
                method = request.getMethod();
            }
        }
    }

    /**
     * Create Map from HEADERS
     * 
     * @param HttpFields headers
     * @return map
     */
    private static Map<String, String> convertHeaderToMap(HttpFields headers) {
        Map<String, String> httpHeader = new HashMap<>();
        for (HttpField header : headers) {
            httpHeader.put(header.getName(), header.getValue());
        }
        return httpHeader;
    }

    /**
     * Validate Response
     * 
     * @param response ContentResponse-Object of http-response
     * @return true if response is valid
     */
    public boolean validateResponse(String contentType) {
        // validate response code
        if (httpCode != OK_200) {
            return false;
        }
        // validate JSON
        String rBody = response.replace("\t", "").replace("\r\n", "").trim();
        if (contentType == CONTENT_TYPE_JSON) {
            if (rBody == null || rBody.isEmpty() || !rBody.startsWith("{") && !rBody.startsWith("[")) {
                /* throw new TapoApiException("Unexpected response: '{}'", rBody); */
                return false;
            }
        }
        return true;
    }

    /***********************************
     *
     * GET RESULTS
     *
     ************************************/

    /**
     * Get Response-Header
     * 
     * @return HTTP-Header object
     */
    public Map getResponseHeaders() {
        return httpHeaders;
    }

    /**
     * Get Response-Header
     * 
     * @return string
     */
    public String getResponseHeader(String headerName) {
        return httpHeaders.get(headerName).toString();
    }

    /**
     * Get Response-Content
     * 
     * @return String responsebody
     */
    public String getResponseBody() {
        // return this.response.getContent().body()
        return response;
    }

    /**
     * Get Response-Status
     * 
     * @return Integer Response-Status
     */
    public Integer getResponseStatus() {
        return httpCode;
    }

    /**
     * Response is valid?
     * 
     * @return true if response was valid
     */
    public boolean responseIsOK() {
        return httpCode == OK_200;
    }

    /**
     * Response is valid?
     * 
     * @return true if response was valid
     */
    public boolean responseIsValid() {
        return this.isValidResponse;
    }

    /**
     * Return URL
     * 
     * @return url
     */
    public String getUrl() {
        return !url.isEmpty() ? method + " " + url : "";
    }

    @Override
    public String toString() {
        return getUrl() + " > " + getResponseBody();
    }
}
