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
package org.openhab.binding.webthing.internal.client;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.webthing.internal.client.dto.Property;
import org.openhab.binding.webthing.internal.client.dto.WebThingDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The implementation of the client-side Webthing representation. This is based on HTTP. Bindings to alternative
 * application protocols such as CoAP may be defined in the future (which may be implemented by another class)
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public class ConsumedThingImpl implements ConsumedThing {
    private static final Duration DEFAULT_PING_PERIOD = Duration.ofSeconds(80);
    private final Logger logger = LoggerFactory.getLogger(ConsumedThingImpl.class);
    private final URI webThingURI;
    private final Gson gson = new Gson();
    private final HttpClient httpClient;
    private final Consumer<String> errorHandler;
    private final WebThingDescription description;
    private final WebSocketConnection websocketDownstream;
    private final AtomicBoolean isOpen = new AtomicBoolean(true);

    /**
     * constructor
     *
     * @param webSocketClient the web socket client to use
     * @param httpClient the http client to use
     * @param webThingURI the identifier of a WebThing resource
     * @param executor executor to use
     * @param errorHandler the error handler
     * @throws IOException it the WebThing can not be connected
     */
    ConsumedThingImpl(WebSocketClient webSocketClient, HttpClient httpClient, URI webThingURI,
            ScheduledExecutorService executor, Consumer<String> errorHandler) throws IOException {
        this(httpClient, webThingURI, executor, errorHandler, WebSocketConnectionFactory.instance(webSocketClient));
    }

    /**
     * constructor
     *
     * @param httpClient the http client to use
     * @param webthingUrl the identifier of a WebThing resource
     * @param executor executor to use
     * @param errorHandler the error handler
     * @param webSocketConnectionFactory the Websocket connectino fctory to be used
     * @throws IOException if the WebThing can not be connected
     */
    ConsumedThingImpl(HttpClient httpClient, URI webthingUrl, ScheduledExecutorService executor,
            Consumer<String> errorHandler, WebSocketConnectionFactory webSocketConnectionFactory) throws IOException {
        this(httpClient, webthingUrl, executor, errorHandler, webSocketConnectionFactory, DEFAULT_PING_PERIOD);
    }

    /**
     * constructor
     *
     * @param httpClient the http client to use
     * @param webthingUrl the identifier of a WebThing resource
     * @param executor executor to use
     * @param errorHandler the error handler
     * @param webSocketConnectionFactory the Websocket connectino fctory to be used
     * @param pingPeriod the ping period tothe the healthiness of the connection
     * @throws IOException if the WebThing can not be connected
     */
    ConsumedThingImpl(HttpClient httpClient, URI webthingUrl, ScheduledExecutorService executor,
            Consumer<String> errorHandler, WebSocketConnectionFactory webSocketConnectionFactory, Duration pingPeriod)
            throws IOException {
        this.webThingURI = webthingUrl;
        this.httpClient = httpClient;
        this.errorHandler = errorHandler;
        this.description = new DescriptionLoader(httpClient).loadWebthingDescription(webThingURI,
                Duration.ofSeconds(20));

        // opens a websocket downstream to be notified if a property value will be changed
        var optionalEventStreamUri = this.description.getEventStreamUri();
        if (optionalEventStreamUri.isPresent()) {
            this.websocketDownstream = webSocketConnectionFactory.create(optionalEventStreamUri.get(), executor,
                    this::onError, pingPeriod);
        } else {
            throw new IOException("WebThing " + webThingURI + " does not support websocket uri. WebThing description: "
                    + this.description);
        }
    }

    private Optional<URI> getPropertyUri(String propertyName) {
        var optionalProperty = description.getProperty(propertyName);
        if (optionalProperty.isPresent()) {
            var propertyDescription = optionalProperty.get();
            for (var link : propertyDescription.links) {
                if ((link.rel != null) && (link.href != null) && "property".equals(link.rel)) {
                    return Optional.of(webThingURI.resolve(link.href));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isAlive() {
        return isOpen.get() && this.websocketDownstream.isAlive();
    }

    @Override
    public void close() {
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
    public void observeProperty(String propertyName, BiConsumer<String, Object> listener) {
        this.websocketDownstream.observeProperty(propertyName, listener);

        // it may take a long time before the observed property value will be changed. For this reason
        // read and notify the current property value (as starting point)
        try {
            var value = readProperty(propertyName);
            listener.accept(propertyName, value);
        } catch (PropertyAccessException pae) {
            logger.warn("could not read WebThing {} property {}", webThingURI, propertyName, pae);
        }
    }

    @Override
    public Object readProperty(String propertyName) throws PropertyAccessException {
        var optionalPropertyUri = getPropertyUri(propertyName);
        if (optionalPropertyUri.isPresent()) {
            var propertyUri = optionalPropertyUri.get();
            try {
                var response = httpClient.newRequest(propertyUri).timeout(30, TimeUnit.SECONDS)
                        .accept("application/json").send();
                if (response.getStatus() < 200 || response.getStatus() >= 300) {
                    onError("WebThing " + webThingURI + " disconnected");
                    throw new PropertyAccessException("could not read " + propertyName + " (" + propertyUri + ")");
                }
                var body = response.getContentAsString();
                var properties = gson.fromJson(body, Map.class);
                if (properties == null) {
                    onError("WebThing " + webThingURI + " erroneous");
                    throw new PropertyAccessException("could not read " + propertyName + " (" + propertyUri
                            + "). Response does not include any property (" + propertyUri + "): " + body);
                } else {
                    var value = properties.get(propertyName);
                    if (value != null) {
                        return value;
                    } else {
                        onError("WebThing " + webThingURI + " erroneous");
                        throw new PropertyAccessException("could not read " + propertyName + " (" + propertyUri
                                + "). Response does not include " + propertyName + "(" + propertyUri + "): " + body);
                    }
                }
            } catch (ExecutionException | TimeoutException | InterruptedException e) {
                onError("WebThing resource " + webThingURI + " disconnected");
                throw new PropertyAccessException("could not read " + propertyName + " (" + propertyUri + ").", e);
            }
        } else {
            onError("WebThing " + webThingURI + " does not support " + propertyName);
            throw new PropertyAccessException("WebThing " + webThingURI + " does not support " + propertyName);
        }
    }

    @Override
    public void writeProperty(String propertyName, Object newValue) throws PropertyAccessException {
        var optionalPropertyUri = getPropertyUri(propertyName);
        if (optionalPropertyUri.isPresent()) {
            var propertyUri = optionalPropertyUri.get();
            var optionalProperty = description.getProperty(propertyName);
            if (optionalProperty.isPresent()) {
                try {
                    if (optionalProperty.get().readOnly) {
                        throw new PropertyAccessException("could not write " + propertyName + " (" + propertyUri
                                + ") with " + newValue + ". Property is readOnly");
                    } else {
                        logger.debug("updating {} with {}", propertyName, newValue);
                        Map<String, Object> payload = Map.of(propertyName, newValue);
                        var json = gson.toJson(payload);
                        var response = httpClient.newRequest(propertyUri).method("PUT")
                                .content(new StringContentProvider(json), "application/json")
                                .timeout(30, TimeUnit.SECONDS).send();
                        if (response.getStatus() < 200 || response.getStatus() >= 300) {
                            onError("WebThing " + webThingURI + "could not write " + propertyName + " (" + propertyUri
                                    + ") with " + newValue);
                            throw new PropertyAccessException(
                                    "could not write " + propertyName + " (" + propertyUri + ") with " + newValue);
                        }
                    }
                } catch (ExecutionException | TimeoutException | InterruptedException e) {
                    onError("WebThing resource " + webThingURI + " disconnected");
                    throw new PropertyAccessException(
                            "could not write " + propertyName + " (" + propertyUri + ") with " + newValue, e);
                }
            } else {
                throw new PropertyAccessException("could not write " + propertyName + " (" + propertyUri + ") with "
                        + newValue + " WebTing does not support a property named " + propertyName);
            }
        } else {
            onError("WebThing " + webThingURI + " does not support " + propertyName);
            throw new PropertyAccessException("WebThing " + webThingURI + " does not support " + propertyName);
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
