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
package org.openhab.binding.intesis.internal.api;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.intesis.internal.config.IntesisHomeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IntesisHomeHttpApi} wraps the IntesisHome REST API and provides various low level function to access the
 * device api (not cloud api).
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class IntesisHomeHttpApi {
    public static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

    private final Logger logger = LoggerFactory.getLogger(IntesisHomeHttpApi.class);
    private final HttpClient httpClient;

    public IntesisHomeHttpApi(IntesisHomeConfiguration config, HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Used to post a request to the device
     *
     * @param ipAddress of the device
     * @param content string
     * @return JSON string as response
     */
    @Nullable
    public String postRequest(String ipAddress, String contentString) {
        String url = "http://" + ipAddress + "/api.cgi";
        try {
            Request request = httpClient.POST(url);
            request.header(HttpHeader.CONTENT_TYPE, "application/json");
            request.content(new StringContentProvider(contentString), "application/json");

            ContentResponse contentResponse = request.timeout(5, TimeUnit.SECONDS).send();

            String response = contentResponse.getContentAsString().replace("\t", "").replace("\r\n", "").trim();

            if (response != null && !response.isEmpty()) {
                return response;
            } else {
                return null;
            }
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            logger.debug("Could not make HTTP Post request");
        }
        return null;
    }
}
