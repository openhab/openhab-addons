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
package org.openhab.binding.juicenet.internal.api;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.core.io.net.http.HttpClientFactory;
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
    private final HttpClientFactory httpClientFactory;

    public JuiceNetHttp(HttpClientFactory httpClientFactory) throws Exception {
        this.httpClientFactory = httpClientFactory;
    }

    public ContentResponse httpGet(String url, @Nullable Map<String, Object> params)
            throws IOException, InterruptedException, ExecutionException, TimeoutException {
        String paramString = "";

        if (params != null) {
            paramString = paramsToUrl(params);
        }

        return httpGet(url, paramString);
    }

    public ContentResponse httpGet(String url, @Nullable String paramString)
            throws IOException, InterruptedException, ExecutionException, TimeoutException {
        HttpClient httpClient = this.httpClientFactory.getCommonHttpClient();

        return httpClient.GET(url + paramString);
    }

    public ContentResponse httpPost(String url, @Nullable Map<String, Object> params)
            throws IOException, InterruptedException, TimeoutException, ExecutionException {
        String paramString = "";

        if (params != null) {
            paramString = paramsToBody(params);
        }

        return httpPost(url, paramString);
    }

    public ContentResponse httpPost(String url, String postData)
            throws IOException, InterruptedException, TimeoutException, ExecutionException {
        logger.trace("{}", postData);

        HttpClient httpClient = this.httpClientFactory.getCommonHttpClient();

        Request request = httpClient.POST(url);
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        request.content(new StringContentProvider(postData), "application/json");

        return request.send();
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
