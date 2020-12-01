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
package org.openhab.binding.sony.internal.transports;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.sony.internal.scalarweb.gson.GsonUtilities;
import org.openhab.binding.sony.internal.scalarweb.models.api.ServiceProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This factory will be responsible for creating a {@link SonyTransport} for the caller. There are two transports that
 * can be used:
 * <ol>
 * <li>{@link #HTTP} the http transport which will allow GET/DELETE/POST calls to a URL</li>
 * <li>{@link #WEBSOCKET} a websocket transport which will use websockets to send JSON payloads (and receive them)</li>
 * </ol>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SonyTransportFactory {

    /** The transport type that allows algorithms to determine which to use */
    public static final String AUTO = "auto";

    /** The transport type for HTTP (note: the value matches what Sony uses for ease) */
    public static final String HTTP = "xhrpost:jsonizer";

    /** The transpor type for Websockets (note: the value matches what Sony uses for ease) */
    public static final String WEBSOCKET = "websocket:jsonizer";

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(SonyTransportFactory.class);

    /** The base URL of the factory */
    private final URL baseUrl;

    /** The default GSON to use */
    private final Gson gson;

    /** Potentially a websocket client to use */
    private final @Nullable WebSocketClient webSocketClient;

    /** Potentially a scheduler to use */
    private final @Nullable ScheduledExecutorService scheduler;

    /**
     * Constructs the transport factory
     * 
     * @param baseUrl a non-null base url
     * @param gson a non-null gson to use
     * @param webSocketClient a potentially null websocket client
     * @param scheduler a potentially null scheduler
     */
    public SonyTransportFactory(final URL baseUrl, final Gson gson, final @Nullable WebSocketClient webSocketClient,
            final @Nullable ScheduledExecutorService scheduler) {
        Objects.requireNonNull(baseUrl, "baseUrl cannot be null");
        Objects.requireNonNull(gson, "gson cannot be null");

        this.baseUrl = baseUrl;
        this.gson = gson;
        this.webSocketClient = webSocketClient;
        this.scheduler = scheduler;
    }

    /**
     * Attempts to create a sony transport suitable to the service protocol
     * 
     * @param serviceProtocol a non-null service protocol
     * @return the sony transport or null if none could be found
     */
    public @Nullable SonyTransport getSonyTransport(final ServiceProtocol serviceProtocol) {
        Objects.requireNonNull(serviceProtocol, "serviceProtocol cannot be null");
        String protocol;
        if (serviceProtocol.hasWebsocketProtocol()) {
            protocol = WEBSOCKET;
        } else if (serviceProtocol.hasHttpProtocol()) {
            protocol = HTTP;
        } else {
            protocol = AUTO;
        }
        final String serviceName = serviceProtocol.getServiceName();
        final SonyTransport transport = getSonyTransport(serviceName, protocol);
        return transport == null ? getSonyTransport(serviceName, AUTO) : transport;
    }

    /**
     * Attempts to create a sony transport suitable to the specified service using {@link #AUTO} for the protocol
     * 
     * @param serviceName a non-null, non-empty service name
     * @return the sony transport or null if none could be found
     */
    public @Nullable SonyTransport getSonyTransport(final String serviceName) {
        Validate.notEmpty(serviceName, "serviceName cannot be empty");

        return getSonyTransport(serviceName, AUTO);
    }

    /**
     * Attempts to create a sony transport suitable to the specified service using the specified protocl
     * 
     * @param serviceName a non-null, non-empty service name
     * @param protocol a non-null, non-empty protocol to use
     * @return the sony transport or null if none could be found
     */
    public @Nullable SonyTransport getSonyTransport(final String serviceName, final String protocol) {
        Validate.notEmpty(serviceName, "serviceName cannot be empty");
        Validate.notEmpty(protocol, "protocol cannot be empty");

        switch (protocol) {
            case AUTO:
                final SonyWebSocketTransport wst = createWebSocketTransport(serviceName);
                return wst == null ? createServiceHttpTransport(serviceName) : wst;

            case HTTP:
                return createServiceHttpTransport(serviceName);

            case WEBSOCKET:
                return createWebSocketTransport(serviceName);

            default:
                logger.debug("Unknown protocol: {} for service {}", protocol, serviceName);
                return null;
        }
    }

    /**
     * Helper method to create a websocket transport to the specified service name
     * 
     * @param serviceName a non-null, non-empty service name
     * @return the sony websocket transport or null if none could be found
     */
    private @Nullable SonyWebSocketTransport createWebSocketTransport(final String serviceName) {
        Validate.notEmpty(serviceName, "serviceName cannot be empty");

        final WebSocketClient localWebSocketClient = webSocketClient;
        if (localWebSocketClient == null) {
            logger.debug("No websocket client specified - cannot create an websocket transport");
            return null;
        }

        try {
            final String baseFile = baseUrl.getFile();
            final URI uri = new URI(
                    String.format("ws://%s:%d/%s", baseUrl.getHost(), baseUrl.getPort() > 0 ? baseUrl.getPort() : 10000,
                            baseFile + (baseFile.endsWith("/") ? "" : "/")
                                    + (serviceName.startsWith("/") ? serviceName.substring(1) : serviceName)))
                                            .normalize();
            return new SonyWebSocketTransport(localWebSocketClient, uri, gson, scheduler);
        } catch (URISyntaxException | InterruptedException | ExecutionException | TimeoutException | IOException e) {
            logger.debug("Exception occurred creating transport: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to create a HTTP transport to the specified service name
     * 
     * @param serviceName a non-null, non-empty service name
     * @return the sony http transport or null if none could be found
     */
    private @Nullable SonyHttpTransport createServiceHttpTransport(final String serviceName) {
        Validate.notEmpty(serviceName, "serviceName cannot be empty");

        final String base = baseUrl.toString();
        final String baseUrlString = base + (base.endsWith("/") ? "" : "/")
                + (serviceName.startsWith("/") ? serviceName.substring(1) : serviceName);

        try {
            return new SonyHttpTransport(baseUrlString, gson);
        } catch (final URISyntaxException e) {
            logger.debug("Exception occurred creating transport: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Public helper method to create an HTTP transport for the given base and gson
     * 
     * @param baseUrl a non-null, non-empty base URL
     * @param gson a non-null gson to use
     * @return a non-null sony http transport
     * @throws URISyntaxException if the baseUrl is malformed
     */
    public static SonyHttpTransport createHttpTransport(final String baseUrl, final Gson gson)
            throws URISyntaxException {
        Validate.notEmpty(baseUrl, "baseUrl cannot be empty");
        Objects.requireNonNull(gson, "gson cannot be null");
        return new SonyHttpTransport(baseUrl, gson);
    }

    /**
     * Public helper method to create an HTTP transport for the given base and default gson
     * 
     * @param baseUrl a non-null, non-empty base URL
     * @return a non-null sony http transport
     * @throws URISyntaxException if the baseUrl is malformed
     */
    public static SonyHttpTransport createHttpTransport(final String baseUrl) throws URISyntaxException {
        Validate.notEmpty(baseUrl, "baseUrl cannot be empty");
        return createHttpTransport(baseUrl, GsonUtilities.getApiGson());
    }

    /**
     * Public helper method to create an HTTP transport for the given base and default gson
     * 
     * @param baseUrl a non-null base URL
     * @return a non-null sony http transport
     * @throws URISyntaxException if the baseUrl is malformed
     */
    public static SonyHttpTransport createHttpTransport(final URL baseUrl) throws URISyntaxException {
        Objects.requireNonNull(baseUrl, "baseUrl cannot be null");
        return createHttpTransport(baseUrl.toExternalForm());
    }

    /**
     * Public helper method to create an HTTP transport for the given base and specified gson
     * 
     * @param baseUrl a non-null base URL
     * @param gson a non-null gson to use
     * @return a non-null sony http transport
     * @throws URISyntaxException if the baseUrl is malformed
     */
    public static SonyHttpTransport createHttpTransport(final URL baseUrl, final Gson gson) throws URISyntaxException {
        Objects.requireNonNull(baseUrl, "baseUrl cannot be null");
        Objects.requireNonNull(gson, "gson cannot be null");
        return createHttpTransport(baseUrl.toExternalForm(), gson);
    }

    /**
     * Public helper method to create an HTTP transport for the given base and specified serviceName
     * 
     * @param baseUrl a non-null base URL
     * @param serviceName
     * @param serviceName a non-null, non-empty service name
     * @throws URISyntaxException if the baseUrl is malformed
     */
    public static SonyHttpTransport createHttpTransport(final URL baseUrl, final String serviceName)
            throws URISyntaxException {
        Objects.requireNonNull(baseUrl, "baseUrl cannot be null");
        Validate.notEmpty(serviceName, "serviceName cannot be empty");
        final String base = baseUrl.toString();
        final String baseUrlString = base + (base.endsWith("/") ? "" : "/")
                + (serviceName.startsWith("/") ? serviceName.substring(1) : serviceName);
        return new SonyHttpTransport(baseUrlString, GsonUtilities.getApiGson());
    }
}
