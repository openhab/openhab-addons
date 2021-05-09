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
package org.openhab.binding.juicenet.internal.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link JuiceNetHttp} implements the http-based REST API to access the JuiceNet Cloud
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class JuiceNetHttp {
    private final Logger logger = LoggerFactory.getLogger(JuiceNetHttp.class);

    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(20)).build();

    public JuiceNetHttp() {
    }

    public HttpResponse<String> httpGet(String url, @Nullable Map<String, Object> params)
            throws IOException, InterruptedException {
        String paramString = "";

        if (params != null) {
            paramString = paramsToUrl(params);
        }

        return httpGet(url, paramString);
    }

    public HttpResponse<String> httpGet(String url, @Nullable String paramString)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder(URI.create(url + paramString)).GET().build();

        return httpClient.send(request, BodyHandlers.ofString());
    }

    public HttpResponse<String> httpPost(String url, @Nullable Map<String, Object> params)
            throws IOException, InterruptedException {
        String paramString = "";

        if (params != null) {
            paramString = paramsToBody(params);
        }

        return httpPost(url, paramString);
    }

    public HttpResponse<String> httpPost(String url, String postData) throws IOException, InterruptedException {
        logger.trace("{}", postData);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(postData)).build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        return response;
    }

    public String paramsToUrl(Map<String, Object> params) {
        return params.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&", "?", ""));
    }

    public String paramsToBody(Map<String, Object> params) {
        return params.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"")
                .collect(Collectors.joining(",", "{", "}"));
    }
}
