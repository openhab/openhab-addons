/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.net;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.filter.LoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class wrapps an HttpRequest to provide additional functionality and centeralized utility features
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class HttpRequest implements AutoCloseable {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    /** The client used in communication */
    private final Client client;

    /** The headers to include in each request */
    private final Map<String, String> headers = new HashMap<>();

    /**
     * Instantiates a new http request
     */
    public HttpRequest() {
        // NOTE: assumes jersey client (no JAX compliant way of doing this)
        // NOTE2: jax 2.1 has a way but we don't use that
        final ClientConfig configuration = new ClientConfig();
        configuration.property(ClientProperties.CONNECT_TIMEOUT, 15000);
        configuration.property(ClientProperties.READ_TIMEOUT, 15000);

        client = ClientBuilder.newClient(configuration);

        if (logger.isDebugEnabled()) {
            client.register(new LoggingFilter(new Slf4LoggingAdapter(logger), true));
        }
    }

    /**
     * Register a new filter with the underlying {@link Client}
     *
     * @param obj the non object to register
     */
    public void register(final Object obj) {
        Objects.requireNonNull(obj, "obj cannot be null");
        client.register(obj);
    }

    /**
     * Send a get command to the specified URL, adding any headers for this request
     *
     * @param url the non-null, non-empty url
     * @param rqstHeaders the list of {@link Header} to add to the request
     * @return the non-null http response
     */
    public HttpResponse sendGetCommand(final String url, final Header... rqstHeaders) {
        Validate.notEmpty(url, "url cannot be empty");
        try {
            final Builder rqst = addHeaders(client.target(url).request(), rqstHeaders);
            final Response content = rqst.get();

            try {
                final HttpResponse httpResponse = new HttpResponse(content);
                return httpResponse;
            } finally {
                content.close();
            }
        } catch (ProcessingException | IllegalStateException | IOException e) {
            return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, e.getMessage());
        }
    }

    /**
     * Send post command for a body comprised of XML to a URL with potentially some request headers
     *
     * @param url the non-null, non-empty URL
     * @param body the non-null, possibly empty body (of XML)
     * @param rqstHeaders the list of {@link Header} to add to the request
     * @return the non-null http response
     */
    public HttpResponse sendPostXmlCommand(final String url, final String body, final Header... rqstHeaders) {
        Validate.notEmpty(url, "url cannot be empty");
        Objects.requireNonNull(body, "body cannot be null");

        return sendPostCommand(url, body, MediaType.TEXT_XML + ";charset=utf-8", rqstHeaders);
    }

    /**
     * Send post command for a body comprised of JSON to a URL with potentially some request headers
     *
     * @param url the non-null, non-empty URL
     * @param body the non-null, possibly empty body (of JSON)
     * @param rqstHeaders the list of {@link Header} to add to the request
     * @return the non-null http response
     */
    public HttpResponse sendPostJsonCommand(final String url, final String body, final Header... rqstHeaders) {
        Validate.notEmpty(url, "url cannot be empty");
        Objects.requireNonNull(body, "body cannot be null");

        return sendPostCommand(url, body, MediaType.APPLICATION_JSON, rqstHeaders);
    }

    /**
     * Send post command to the specified URL with the body/media type and potentially some request headers
     *
     * @param url the non-null, non-empty URL
     * @param body the non-null, possibly empty body (of JSON)
     * @param mediaType the non-null, non-empty media type
     * @param rqstHeaders the list of {@link Header} to add to the request
     * @return the non-null http response
     */
    private HttpResponse sendPostCommand(final String url, final String body, final String mediaType,
            final Header... rqstHeaders) {
        Validate.notEmpty(url, "url cannot be empty");
        Objects.requireNonNull(body, "body cannot be null");
        Validate.notEmpty(mediaType, "mediaType cannot be empty");

        try {
            final Builder rqst = addHeaders(client.target(url).request(mediaType), rqstHeaders);
            final Response content = rqst.post(Entity.entity(body, mediaType));

            try {
                final HttpResponse httpResponse = new HttpResponse(content);
                return httpResponse;
            } finally {
                content.close();
            }
        } catch (IOException | ProcessingException e) {
            return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, e.getMessage());
        }
    }

    /**
     * Send delete command to the specified URL with the body and potentially request headers
     *
     * @param url the non-null, non-empty URL
     * @param rqstHeaders the list of {@link Header} to add to the request
     * @return the non-null http response
     */
    public HttpResponse sendDeleteCommand(final String url, final Header... rqstHeaders) {
        Validate.notEmpty(url, "url cannot be empty");
        try {
            final Builder rqst = addHeaders(client.target(url).request(), rqstHeaders);
            final Response content = rqst.delete();

            try {
                final HttpResponse httpResponse = new HttpResponse(content);
                return httpResponse;
            } finally {
                content.close();
            }
        } catch (final IOException e) {
            return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, e.getMessage());
        }
    }

    /**
     * Helper method to add the headers (preventing duplicates) to the builder
     * 
     * @param bld a non-null builder
     * @param hdrs a possibly empty list of headers
     * @return the builder with the headers added
     */
    private Builder addHeaders(final Builder bld, final Header... hdrs) {
        Objects.requireNonNull(bld, "bld cannot be null");

        Builder localBuilder = bld;
        final Set<String> hdrNames = new HashSet<>();

        for (final Header hdr : hdrs) {
            final String hdrName = hdr.getName();
            if (!hdrNames.contains(hdrName)) {
                hdrNames.add(hdrName);
                localBuilder = localBuilder.header(hdrName, hdr.getValue());
            }
        }

        for (final Entry<String, String> h : headers.entrySet()) {
            final String hdrName = h.getKey();
            if (!hdrNames.contains(hdrName)) {
                hdrNames.add(hdrName);
                localBuilder = localBuilder.header(hdrName, h.getValue());
            }
        }
        return localBuilder;
    }

    /**
     * Adds a header to ALL request made
     *
     * @param name the non-null, non-empty header na,e
     * @param value the non-null, non-empty value
     */
    public void addHeader(final String name, final String value) {
        Validate.notEmpty(name, "name cannot be empty");
        Validate.notEmpty(value, "value cannot be empty");

        headers.put(name, value);
    }

    /**
     * Adds a header to ALL request made
     *
     * @param header the non-null header to add
     */
    public void addHeader(final Header header) {
        Objects.requireNonNull(header, "header cannot be null");
        addHeader(header.getName(), header.getValue());
    }

    @Override
    public void close() {
        client.close();
    }
}
