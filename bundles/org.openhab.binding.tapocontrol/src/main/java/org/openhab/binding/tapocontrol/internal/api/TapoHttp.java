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
package org.openhab.binding.tapocontrol.internal.api;

import static org.openhab.binding.tapocontrol.internal.TapoControlBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.tapocontrol.internal.helpers.TapoHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler class for TAPO Smart Home device connections.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoHttp {
    public String url = "";
    public String request = "";
    public String cookie = "";
    public Fields requestFields = new Fields();
    public TapoHttpResponse response = new TapoHttpResponse();

    private final Logger logger = LoggerFactory.getLogger(TapoHttp.class);
    private HttpClient httpClient = new HttpClient();;

    /**
     * Init Class
     *
     */
    public TapoHttp() {
        setConfig();
        /*
         * this.gson = new Gson();
         * this.httpClient = new HttpClient();
         */
    }

    /**
     * Init Class with parameters
     *
     * @param url URL request is sent to
     * @param request (encoded) request to send to device
     */
    public TapoHttp(String url, String request) {
        setConfig();
        this.url = url;
        this.request = request;
    }

    /**
     * Init Class with parameters
     *
     * @param url URL request is sent to
     * @param request (encoded) request to send to device
     * @param cookie cookie header craeted by handshake
     */
    public TapoHttp(String url, String request, String cookie) {
        setConfig();
        this.url = url;
        this.request = request;
        this.cookie = cookie;
    }

    /**
     * Init default vars
     *
     */
    public void setConfig() {
        httpClient.setConnectTimeout(TAPO_HTTP_TIMEOUT_MS);
        httpClient.setFollowRedirects(false);
    }

    /**
     * Send Request
     * 
     * @return true on success
     */
    public TapoHttpResponse send() {
        logger.trace("sending HTTP '{}'' to {}", this.request, this.url);
        try {
            this.httpClient.start();

            Request httpRequest = httpClient.newRequest(url).method(HttpMethod.POST.toString());

            /* set header */
            httpRequest.header("content-type", CONTENT_TYPE_JSON);
            httpRequest.header("Accept", CONTENT_TYPE_JSON);
            if (!this.cookie.isEmpty()) {
                httpRequest.header(HTTP_AUTH_TYPE_COOKIE, this.cookie);
            }

            /* add request body */
            httpRequest.content(new StringContentProvider(this.request, CONTENT_CHARSET), CONTENT_TYPE_JSON);

            ContentResponse httpResponse = httpRequest.send();
            this.response = new TapoHttpResponse(httpResponse);

            this.httpClient.stop();
        } catch (Exception ex) {
            logger.trace("HTTP-Request send returned exception: ", ex);
            this.response = new TapoHttpResponse("POST", url);
        }
        return this.response;
    }

    /***********************************
     *
     * SET PARAMETERS
     *
     ************************************/

    /**
     * Set Parameters: url
     * 
     * @param url URL request is sent to
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Set Parameters: request
     * 
     * @param request (encoded) request to send to device
     */
    public void setRequest(String request) {
        this.request = request;
    }

    /**
     * Set Parameters: request
     * 
     * @param fields request
     */
    public void setRequest(Fields requestFields) {
        this.requestFields = requestFields;
    }

    /**
     * Set Parameters: cookie
     * 
     * @param cookie cookie header craeted by handshake
     */
    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    /***********************************
     *
     * GET RESULTS
     *
     ************************************/

    public TapoHttpResponse response() {
        return this.response;
    }
}
