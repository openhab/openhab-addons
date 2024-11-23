/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.openhab.binding.salus.internal.rest.exceptions.HttpSalusApiException;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class HttpClient implements RestClient {
    private static final int TIMEOUT = 10;
    private static final int IDLE_TIMEOUT = TIMEOUT;
    private final org.eclipse.jetty.client.HttpClient client;

    public HttpClient(org.eclipse.jetty.client.HttpClient client) {
        this.client = requireNonNull(client, "client");
        if (this.client.isStopped()) {
            throw new IllegalStateException("HttpClient is stopped");
        }
    }

    @Override
    public @Nullable String get(String url, @Nullable Header... headers) throws SalusApiException {
        var request = requireNonNull(client.newRequest(url));
        return execute(request, headers, url);
    }

    @Override
    public @Nullable String post(String url, @Nullable Content content, @Nullable Header... headers)
            throws SalusApiException {
        var request = requireNonNull(client.POST(url));
        if (content != null) {
            request.content(new StringContentProvider(content.body()), content.type());
        }
        return execute(request, headers, url);
    }

    @SuppressWarnings("ConstantValue")
    private @Nullable String execute(Request request, @Nullable Header[] headers, String url) throws SalusApiException {
        try {
            if (headers != null) {
                for (var header : headers) {
                    if (header == null) {
                        continue;
                    }
                    for (var value : header.values()) {
                        request.header(header.name(), value);
                    }
                }
            }
            request.timeout(TIMEOUT, SECONDS);
            request.idleTimeout(IDLE_TIMEOUT, SECONDS);
            var response = request.send();
            var status = response.getStatus();
            if (status < 200 || status >= 399) {
                throw new HttpSalusApiException(status, response.getReason());
            }
            return response.getContentAsString();
        } catch (RuntimeException | TimeoutException | ExecutionException | InterruptedException ex) {
            Throwable cause = ex;
            while (cause != null) {
                if (cause instanceof HttpResponseException hte) {
                    var response = hte.getResponse();
                    throw new HttpSalusApiException(response.getStatus(), response.getReason(), hte);
                }
                cause = cause.getCause();
            }
            throw new SalusApiException("Error while executing request to " + url, ex);
        }
    }
}
