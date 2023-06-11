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
package org.openhab.io.neeo.internal.net;

import java.io.IOException;
import java.util.Objects;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.filter.LoggingFilter;
import org.openhab.io.neeo.internal.NeeoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an HTTP session with a client
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class HttpRequest implements AutoCloseable {

    /** the logger */
    private final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    /** The client to use */
    private final Client client;

    /**
     * Instantiates a new request
     */
    public HttpRequest(ClientBuilder clientBuilder) {
        client = clientBuilder.build();

        if (logger.isDebugEnabled()) {
            client.register(new LoggingFilter(new Slf4LoggingAdapter(logger), true));
        }
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
            final Builder request = client.target(uri).request();

            final Response content = request.get();

            try {
                return new HttpResponse(content);
            } finally {
                content.close();
            }
        } catch (IOException | IllegalStateException | ProcessingException e) {
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
            final Builder request = client.target(uri).request(MediaType.APPLICATION_JSON);

            final Response content = request.post(Entity.entity(body, MediaType.APPLICATION_JSON));

            try {
                return new HttpResponse(content);
            } finally {
                content.close();
            }
        } catch (IOException | IllegalStateException | ProcessingException e) {
            String message = e.getMessage();
            return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, message != null ? message : "");
        }
    }

    @Override
    public void close() {
        client.close();
    }
}
