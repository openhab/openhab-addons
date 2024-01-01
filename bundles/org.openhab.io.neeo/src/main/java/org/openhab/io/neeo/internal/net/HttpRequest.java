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
package org.openhab.io.neeo.internal.net;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.ProcessingException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.io.neeo.internal.NeeoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an HTTP session with a httpClient
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class HttpRequest implements AutoCloseable {

    /** the logger */
    private final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    /** The httpClient to use */
    private final HttpClient httpClient;

    /**
     * Instantiates a new request
     */
    public HttpRequest(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Send a get command to the specified uri
     *
     * @param uri the non-null uri
     * @return the {@link HttpResponse}
     */
    public HttpResponse sendGetCommand(String uri) {
        NeeoUtil.requireNotEmpty(uri, "uri cannot be empty");
        try {
            final org.eclipse.jetty.client.api.Request request = httpClient.newRequest(uri);
            request.method(HttpMethod.GET);
            request.timeout(10, TimeUnit.SECONDS);
            ContentResponse refreshResponse = request.send();
            return new HttpResponse(refreshResponse);
        } catch (IOException | IllegalStateException | ProcessingException e) {
            String message = e.getMessage();
            return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, message != null ? message : "");
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("An exception occurred while invoking a HTTP request: '{}'", e.getMessage());
            String message = e.getMessage();
            return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, message != null ? message : "");
        }
    }

    /**
     * Send post JSON command using the body
     *
     * @param uri the non empty uri
     * @param body the non-null, possibly empty body
     * @return the {@link HttpResponse}
     */
    public HttpResponse sendPostJsonCommand(String uri, String body) {
        NeeoUtil.requireNotEmpty(uri, "uri cannot be empty");
        Objects.requireNonNull(body, "body cannot be null");

        try {
            final org.eclipse.jetty.client.api.Request request = httpClient.newRequest(uri);
            request.content(new StringContentProvider(body));
            request.header(HttpHeader.CONTENT_TYPE, "application/json");
            request.method(HttpMethod.POST);
            request.timeout(10, TimeUnit.SECONDS);
            ContentResponse refreshResponse = request.send();
            return new HttpResponse(refreshResponse);
        } catch (IOException | IllegalStateException | ProcessingException e) {
            String message = e.getMessage();
            return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, message != null ? message : "");
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("An exception occurred while invoking a HTTP request: '{}'", e.getMessage());
            String message = e.getMessage();
            return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, message != null ? message : "");
        }
    }

    @Override
    public void close() {
    }
}
