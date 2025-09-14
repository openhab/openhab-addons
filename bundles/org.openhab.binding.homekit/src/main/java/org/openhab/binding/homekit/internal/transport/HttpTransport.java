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
package org.openhab.binding.homekit.internal.transport;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;

/**
 * Handles HTTP transport for HomeKit communication.
 * It provides methods for sending GET, POST, and PUT requests with appropriate headers and content types.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HttpTransport {

    private final HttpClient httpClient;

    public HttpTransport(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Sends a GET request to the specified URL and endpoint, expecting a response of the given content type.
     *
     * @param url the target URL
     * @param endpoint the endpoint path
     * @param contentType the expected content type of the response
     *
     * @return the response body
     *
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     * @throws IOException
     */
    public byte[] get(String baseUrl, String endpoint, String contentType)
            throws IOException, InterruptedException, TimeoutException, ExecutionException {
        String url = baseUrl + "/" + endpoint;
        Request request = httpClient.newRequest(url).timeout(5, TimeUnit.SECONDS).method(HttpMethod.GET)
                .header(HttpHeader.ACCEPT, contentType);

        ContentResponse response = request.send();
        if (response.getStatus() != 200) {
            throw new IOException("GET %s HTTP %d".formatted(url, response.getStatus()));
        }

        return response.getContent();
    }

    /**
     * Sends a POST request with the given payload and content type to the specified URL and endpoint.
     *
     * @param url the target URL
     * @param endpoint the endpoint path
     * @param contentType the content type of the request
     * @param content the request body
     *
     * @return the response body
     *
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     * @throws IOException
     */
    public byte[] post(String baseUrl, String endpoint, String contentType, byte[] content)
            throws IOException, InterruptedException, TimeoutException, ExecutionException {
        String url = baseUrl + "/" + endpoint;
        Request request = httpClient.newRequest(url).timeout(5, TimeUnit.SECONDS).method(HttpMethod.POST)
                .header(HttpHeader.CONTENT_TYPE, contentType).content(new BytesContentProvider(content));

        ContentResponse response = request.send();
        if (response.getStatus() != 200) {
            throw new IOException("POST %s HTTP %d".formatted(url, response.getStatus()));
        }

        return response.getContent();
    }

    /**
     * Sends a PUT request with the given payload and content type to the specified URL and endpoint.
     *
     * @param url the target URL
     * @param endpoint the endpoint path
     * @param contentType the content type of the request
     * @param content the request body
     *
     * @return the response body
     *
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     * @throws IOException
     */
    public byte[] put(String baseUrl, String endpoint, String contentType, byte[] content)
            throws IOException, InterruptedException, TimeoutException, ExecutionException {
        String url = baseUrl + "/" + endpoint;
        Request request = httpClient.newRequest(url).timeout(5, TimeUnit.SECONDS).method(HttpMethod.POST)
                .header(HttpHeader.ACCEPT, contentType).header(HttpHeader.CONTENT_TYPE, contentType)
                .content(new BytesContentProvider(content));

        ContentResponse response = request.send();
        if (response.getStatus() != 200) {
            throw new IOException("PUT %s error: HTTP %d".formatted(url, response.getStatus()));
        }

        return response.getContent();
    }
}
