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
package org.openhab.binding.meross.internal.api;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;

/**
 * The {@link MerossLocalHttpConnector} class is responsible for handling the LAN http connection to individual devices.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class MerossLocalHttpConnector extends MerossHttpConnector {

    public MerossLocalHttpConnector(@Nullable HttpClient httpClient, String apiBaseUrl) {
        super(httpClient, apiBaseUrl);
    }

    /**
     * @param content
     * @param uri The uri
     * @param path The path (endpoint)
     * @return The http response
     * @throws IOException if it fails to return the http response
     */
    @Override
    protected ContentResponse postResponse(String content, String uri, String path) throws IOException {
        HttpClient httpClient = this.httpClient;
        if (httpClient == null) {
            throw new IOException("Internal error: http client not set");
        }
        Request request = httpClient.newRequest(URI.create(uri + path)).method(HttpMethod.POST)
                .content(new StringContentProvider(content), "application/json")
                .timeout(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        try {
            return request.send();
        } catch (TimeoutException e) {
            throw new IOException("Timeout while posting data", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Error while posting data", e);
        }
    }
}
