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
package org.openhab.binding.senechome.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.binding.senechome.internal.json.SenecHomeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;

/**
 * The {@link SenecHomeApi} class configures http client and
 * performs status requests
 *
 * @author Steven Schwarznau - Initial contribution
 *
 */
@NonNullByDefault
public class SenecHomeApi {
    private static final String HTTP_PROTO_PREFIX = "http://";

    private final Logger logger = LoggerFactory.getLogger(SenecHomeApi.class);
    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private String hostname = "";

    public SenecHomeApi(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * POST json with empty, but expected fields, to lala.cgi of Senec webinterface
     * the response will contain the same fields, but with the corresponding values
     *
     * To receive new values, just modify the Json objects and add them to the thing channels
     *
     * @return Instance of SenecHomeResponse
     * @throws MalformedURLException Configuration/URL is wrong
     * @throws IOException Communication failed
     */
    public SenecHomeResponse getStatistics()
            throws InterruptedException, TimeoutException, ExecutionException, IOException {
        String location = HTTP_PROTO_PREFIX + hostname;

        Request request = httpClient.newRequest(location);
        request.header(HttpHeader.ACCEPT, MimeTypes.Type.APPLICATION_JSON.asString());
        request.header(HttpHeader.CONTENT_TYPE, MimeTypes.Type.FORM_ENCODED.asString());
        ContentResponse response = null;
        try {
            response = request.method(HttpMethod.POST)
                    .content(new StringContentProvider(gson.toJson(new SenecHomeResponse()))).send();
            if (response.getStatus() == HttpStatus.OK_200) {
                return Objects.requireNonNull(gson.fromJson(response.getContentAsString(), SenecHomeResponse.class));
            } else {
                logger.trace("Got unexpected response code {}", response.getStatus());
                throw new IOException("Got unexpected response code " + response.getStatus());
            }
        } catch (MalformedJsonException | JsonSyntaxException | InterruptedException | TimeoutException
                | ExecutionException e) {
            String errorMessage = "\nlocation: " + location;
            errorMessage += "\nrequest: " + request.toString();
            errorMessage += "\nrequest.getHeaders: " + request.getHeaders();
            if (response == null) {
                errorMessage += "\nresponse: null";
            } else {
                errorMessage += "\nresponse: " + response.toString();
                errorMessage += "\nresponse.getHeaders: " + response.getHeaders();
                if (response.getContent() == null) {
                    errorMessage += "\nresponse.getContent is null";
                } else {
                    errorMessage += "\nresponse.getContentAsString: " + response.getContentAsString();
                }
            }
            logger.trace("Issue with getting SenecHomeResponse\n{}", errorMessage);
            throw e;
        }
    }
}
