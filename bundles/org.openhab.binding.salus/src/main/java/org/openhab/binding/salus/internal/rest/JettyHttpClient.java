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
package org.openhab.binding.salus.internal.rest;

import static java.util.Objects.requireNonNull;

import javax.validation.constraints.NotNull;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grześlowski - Initial contribution
 */
public class JettyHttpClient implements RestClient {
    private final Logger logger = LoggerFactory.getLogger(JettyHttpClient.class);
    private final HttpClient client;

    public JettyHttpClient(HttpClient client) {
        this.client = requireNonNull(client, "client");
        if (this.client.isStopped()) {
            throw new IllegalStateException("HttpClient is stopped");
        }
    }

    @Override
    public Response<String> get(String url, Header... headers) {
        var request = client.newRequest(url);
        return execute(request, headers, url);
    }

    @Override
    public Response<String> post(String url, Content content, Header... headers) {
        var request = client.POST(url);
        if (content != null) {
            request.content(new StringContentProvider(content.body()), content.type());
        }
        return execute(request, headers, url);
    }

    @NotNull
    private RestClient.Response<String> execute(Request request, Header[] headers, String url) {
        try {
            if (headers != null) {
                for (var header : headers) {
                    for (var value : header.values())
                        request.header(header.name(), value);
                }
            }
            var response = request.send();
            return new Response<>(response.getStatus(), response.getContentAsString());
        } catch (Exception ex) {
            Throwable cause = ex;
            while (cause != null) {
                if (cause instanceof HttpResponseException hte) {
                    var response = hte.getResponse();
                    return new Response<>(response.getStatus(), response.getReason());
                }
                cause = cause.getCause();
            }
            throw new HttpException("POST", url, ex);
        }
    }
}
