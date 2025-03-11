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
package org.openhab.binding.sedif.internal.api;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.sedif.internal.handler.BridgeSedifWebHandler;
import org.openhab.binding.sedif.internal.types.SedifException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link SedifHttpApi} wraps the Sedif Webservice.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SedifHttpApi {

    private final Logger logger = LoggerFactory.getLogger(SedifHttpApi.class);
    private final Gson gson;
    private final HttpClient httpClient;
    private final BridgeSedifWebHandler bridge;

    public SedifHttpApi(BridgeSedifWebHandler bridge, Gson gson, HttpClient httpClient) {
        this.gson = gson;
        this.httpClient = httpClient;
        this.bridge = bridge;
    }

    public void removeAllCookie() {
        httpClient.getCookieStore().removeAll();
    }

    public String getLocation(ContentResponse response) {
        return response.getHeaders().get(HttpHeader.LOCATION);
    }

    public String getContent(String url) throws SedifException {
        return getContent(logger, bridge, url, httpClient, "");
    }

    private static String getContent(Logger logger, BridgeSedifWebHandler bridgeHandler, String url,
            HttpClient httpClient, String token) throws SedifException {
        try {
            Request request = httpClient.newRequest(url);

            request = request.agent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0");
            request = request.method(HttpMethod.GET);
            if (!token.isEmpty()) {
                request = request.header("Authorization", "" + token);
                request = request.header("Accept", "application/json");
            }

            ContentResponse result = request.send();
            if (result.getStatus() == HttpStatus.TEMPORARY_REDIRECT_307
                    || result.getStatus() == HttpStatus.MOVED_TEMPORARILY_302) {
                String loc = result.getHeaders().get("Location");
                String newUrl = "";

                if (loc.startsWith("http://") || loc.startsWith("https://")) {
                    newUrl = loc;
                } else {
                    newUrl = bridgeHandler.getBaseUrl() + loc.substring(1);
                }

                request = httpClient.newRequest(newUrl);
                request = request.method(HttpMethod.GET);
                result = request.send();

                if (result.getStatus() == HttpStatus.TEMPORARY_REDIRECT_307
                        || result.getStatus() == HttpStatus.MOVED_TEMPORARILY_302) {
                    loc = result.getHeaders().get("Location");
                    String[] urlParts = loc.split("/");
                    if (urlParts.length < 4) {
                        throw new SedifException("malformed url : %s", loc);
                    }
                    return urlParts[3];
                }
            }
            if (result.getStatus() != 200) {
                throw new SedifException("Error requesting '%s': %s", url, result.getContentAsString());
            }

            String content = result.getContentAsString();
            logger.trace("getContent returned {}", content);
            return content;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new SedifException(e, "Error getting url: '%s'", url);
        }
    }

    public FormContentProvider getFormContent(String fieldName, String fieldValue) {
        Fields fields = new Fields();
        fields.put(fieldName, fieldValue);
        return new FormContentProvider(fields);
    }

}
