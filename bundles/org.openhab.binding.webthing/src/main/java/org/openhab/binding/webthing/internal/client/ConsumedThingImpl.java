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
package org.openhab.binding.webthing.internal.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.webthing.internal.client.dto.Property;
import org.openhab.binding.webthing.internal.client.dto.WebThingDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The implementation of the client-side Webthing representation. This is based on HTTP. Bindings to alternative
 * application protocols such as CoAP may be defined in the future
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public class ConsumedThingImpl implements ConsumedThing {
    private static final Duration DEFAULT_PING_PERIOD = Duration.ofSeconds(80);
    private final Logger logger = LoggerFactory.getLogger(ConsumedThingImpl.class);
    private final URI webThingURI;
    private final Consumer<String> errorHandler;
    private final WebThingDescription description;
    private final HttpClient httpClient;
    private final WebSocketConnection websocketDownstream;
    private final AtomicBoolean isOpen = new AtomicBoolean(true);

    /**
     * constructor
     *
     * @param webThingURI the identifier of a WebThing resource
     * @param executor executor to use
     * @param errorHandler the error handler
     * @throws IOException it the WebThing can not be connected
     */
    ConsumedThingImpl(URI webThingURI, ScheduledExecutorService executor, Consumer<String> errorHandler)
            throws IOException {
        this(webThingURI, executor, errorHandler,
                HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build(),
                WebSocketConnectionFactory.instance());
    }

    /**
     * constructor
     *
     * @param webthingUrl the identifier of a WebThing resource
     * @param executor executor to use
     * @param errorHandler the error handler
     * @param httpClient the http client to use
     * @param webSocketConnectionFactory the Websocket connectino fctory to be used
     * @throws IOException if the WebThing can not be connected
     */
    ConsumedThingImpl(URI webthingUrl, ScheduledExecutorService executor, Consumer<String> errorHandler,
            HttpClient httpClient, WebSocketConnectionFactory webSocketConnectionFactory) throws IOException {
        this(webthingUrl, executor, errorHandler, httpClient, webSocketConnectionFactory, DEFAULT_PING_PERIOD);
    }

    /**
     * constructor
     *
     * @param webthingUrl the identifier of a WebThing resource
     * @param executor executor to use
     * @param errorHandler the error handler
     * @param httpClient the http client to use
     * @param webSocketConnectionFactory the Websocket connectino fctory to be used
     * @param pingPeriod the ping period tothe the healthiness of the connection
     * @throws IOException if the WebThing can not be connected
     */
    ConsumedThingImpl(URI webthingUrl, ScheduledExecutorService executor, Consumer<String> errorHandler,
            HttpClient httpClient, WebSocketConnectionFactory webSocketConnectionFactory, Duration pingPeriod)
            throws IOException {
        this.webThingURI = webthingUrl;
        this.httpClient = httpClient;
        this.errorHandler = errorHandler;
        this.description = new DescriptionLoader(httpClient).loadWebthingDescription(webThingURI,
                Duration.ofSeconds(20));

        // opens a websocket downstream to be notified if a property value will be changed
        this.websocketDownstream = webSocketConnectionFactory.create(this.getEventStreamUri(), executor, this::onError,
                pingPeriod);
    }

    private URI getPropertyUri(String propertyName) {
        var optionalProperty = description.getProperty(propertyName);
        if (optionalProperty.isPresent()) {
            var propertyDescription = optionalProperty.get();
            for (var link : propertyDescription.links) {
                if ((link.rel != null) && (link.href != null) && link.rel.equals("property")) {
                    return webThingURI.resolve(link.href);
                }
            }
        }
        throw new RuntimeException(
                "WebThing " + webThingURI + " does not support a property uri. WebThing description: " + description);
    }

    private URI getEventStreamUri() {
        for (var link : this.description.links) {
            var href = link.href;
            if (href != null) {
                var rel = Optional.ofNullable(link.rel).orElse("<undefined>");
                if (rel.equals("alternate")) {
                    return URI.create(href);
                }
            }
        }
        throw new RuntimeException("webthing " + webThingURI + " does not support websocket uri. WebThing description: "
                + this.description);
    }

    @Override
    public void close() {
        logger.debug("WebThing {} closed called", webThingURI);
        isOpen.set(false);
        this.websocketDownstream.close();
    }

    void onError(String reason) {
        logger.debug("WebThing {} error occurred. {}", webThingURI, reason);
        if (isOpen.get()) {
            errorHandler.accept(reason);
        }
        close();
    }

    @Override
    public WebThingDescription getThingDescription() {
        return this.description;
    }

    @Override
    public void observeProperty(String propertyName, PropertyChangedListener listener) {
        this.websocketDownstream.observeProperty(propertyName, listener);

        // it may take a long time before the observed property value will be changed. For this reason
        // read and notify the current property value (as starting point)
        try {
            var value = readProperty(propertyName);
            listener.onPropertyValueChanged(propertyName, value);
        } catch (PropertyAccessException pae) {
            logger.warn("could not read WebThing {} property {}", webThingURI, propertyName, pae);
        }
    }

    @Override
    public Object readProperty(String propertyName) throws PropertyAccessException {
        var propertyUri = getPropertyUri(propertyName);
        try {
            var request = HttpRequest.newBuilder().timeout(Duration.ofSeconds(30)).GET().uri(propertyUri).build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                onError("WebThing " + webThingURI + " disconnected");
                throw new PropertyAccessException("could not read " + propertyName + " (" + propertyUri
                        + "). Got error response " + response.body());
            }
            var properties = new Gson().fromJson(response.body(), Map.class);
            var value = properties.get(propertyName);
            if (value != null) {
                return value;
            } else {
                throw new PropertyAccessException("could not read " + propertyName + " (" + propertyUri
                        + "). Response does not include " + propertyName + "(" + propertyUri + "): " + response.body());
            }
        } catch (InterruptedException | IOException e) {
            onError("WebThing resource " + webThingURI + " disconnected");
            throw new PropertyAccessException("could not read " + propertyName + " (" + propertyUri + ").", e);
        }
    }

    @Override
    public void writeProperty(String propertyName, Object newValue) throws PropertyAccessException {
        var property = description.properties.get(propertyName);
        var propertyUri = getPropertyUri(propertyName);
        try {
            if (property.readOnly) {
                throw new PropertyAccessException("could not write " + propertyName + " (" + propertyUri + ") with "
                        + newValue + ". Property is readOnly");
            } else {
                logger.debug("updating {} with {}", propertyName, newValue);
                Map<String, Object> payload = Map.of(propertyName, newValue);
                var json = new Gson().toJson(payload);
                var request = HttpRequest.newBuilder().timeout(Duration.ofSeconds(30))
                        .PUT(HttpRequest.BodyPublishers.ofString(json)).uri(propertyUri).build();
                var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new PropertyAccessException("could not write " + propertyName + " (" + propertyUri + ") with "
                            + newValue + ". Got error response " + response.body());
                }
            }
        } catch (InterruptedException | IOException e) {
            onError("WebThing resource " + webThingURI + " disconnected");
            throw new PropertyAccessException(
                    "could not write " + propertyName + " (" + propertyUri + ") with " + newValue, e);
        }
    }

    /**
     * Gets the property description
     *
     * @param propertyName the propertyName
     * @return the description (meta data) of the property
     */
    public @Nullable Property getPropertyDescription(String propertyName) {
        return description.properties.get(propertyName);
    }

    @Override
    public String toString() {
        return "WebThing " + description.title + " (" + webThingURI + ")";
    }
}
