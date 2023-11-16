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
package org.openhab.binding.neeo.internal.net;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.openhab.binding.neeo.internal.NeeoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an HTTP session with a client
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class HttpRequest implements AutoCloseable {

    /** the logger */
    private final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    /** The client to use */
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
        } catch (IOException | IllegalStateException e) {
            return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, e.getMessage());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("An exception occurred while invoking a HTTP request: '{}'", e.getMessage());
            String message = e.getMessage();
            return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, message != null ? message : "");
        }
    }

    /**
     * Send post JSON command using the body
     *
     * @param uriString the non empty uri
     * @param body the non-null, possibly empty body
     * @return the {@link HttpResponse}
     */
    public HttpResponse sendPostJsonCommand(String uriString, String body) {
        NeeoUtil.requireNotEmpty(uriString, "uri cannot be empty");
        Objects.requireNonNull(body, "body cannot be null");

        logger.trace("sendPostJsonCommand: target={}, body={}", uriString, body);

        try {
            URI targetUri = new URI(uriString);
            if (!targetUri.isAbsolute()) {
                logger.warn("Absolute URI required but provided URI '{}' is non-absolute. ", uriString);
                return new HttpResponse(HttpStatus.NOT_ACCEPTABLE_406, "Absolute URI required");
            }
            final org.eclipse.jetty.client.api.Request request = httpClient.newRequest(targetUri);
            request.content(new StringContentProvider(body));
            request.header(HttpHeader.CONTENT_TYPE, "application/json");
            request.method(HttpMethod.POST);
            request.timeout(10, TimeUnit.SECONDS);
            ContentResponse refreshResponse = request.send();
            return new HttpResponse(refreshResponse);
            // IllegalArgumentException/ProcessingException catches issues with the URI being invalid
            // as well
        } catch (IOException | IllegalStateException | IllegalArgumentException | ProcessingException e) {
            return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, e.getMessage());
        } catch (URISyntaxException e) {
            return new HttpResponse(HttpStatus.NOT_ACCEPTABLE_406, e.getMessage());
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
