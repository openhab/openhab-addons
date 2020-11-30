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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
    private static final Duration DEFAULT_PING_PERIOD = Duration.ofSeconds(90);
    private final Logger logger = LoggerFactory.getLogger(ConsumedThingImpl.class);
    private final URI webThingURI;
    private final ConnectionListener connectionListener;
    private final WebThingDescription description;
    private final HttpClient httpClient;
    private final Map<String, Property> propertyMap;
    private final WebSocketConnection websocketDownstream;

    /**
     * constructor
     *
     * @param webThingURI the identifier of a WebThing resource
     * @param connectionListener the connection listerner to observe theconnection state of the Wbthing connection
     * @throws IOException it the WebThing can not be connected
     */
    ConsumedThingImpl(URI webThingURI, ConnectionListener connectionListener) throws IOException {
        this(webThingURI, connectionListener,
                HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build(),
                WebSocketConnectionFactory.instance());
    }

    /**
     * constructor
     *
     * @param webthingUrl the identifier of a WebThing resource
     * @param connectionListener the connection listener to observe the connection state of the WebThing connection
     * @param httpClient the http client to use
     * @param webSocketConnectionFactory the Websocket connectino fctory to be used
     * @throws IOException if the WebThing can not be connected
     */
    ConsumedThingImpl(URI webthingUrl, ConnectionListener connectionListener, HttpClient httpClient,
            WebSocketConnectionFactory webSocketConnectionFactory) throws IOException {
        this(webthingUrl, connectionListener, httpClient, webSocketConnectionFactory, DEFAULT_PING_PERIOD);
    }

    /**
     * constructor
     *
     * @param webthingUrl the identifier of a WebThing resource
     * @param connectionListener the connection listener to observe the connection state of the WebThing connection
     * @param httpClient the http client to use
     * @param webSocketConnectionFactory the Websocket connectino fctory to be used
     * @param pingPeriod the ping period tothe the healthiness of the connection
     * @throws IOException if the WebThing can not be connected
     */
    private ConsumedThingImpl(URI webthingUrl, ConnectionListener connectionListener, HttpClient httpClient,
            WebSocketConnectionFactory webSocketConnectionFactory, Duration pingPeriod) throws IOException {
        this.webThingURI = webthingUrl;
        this.httpClient = httpClient;
        this.connectionListener = connectionListener;
        this.description = new DescriptionLoader(httpClient).loadWebthingDescription(webThingURI,
                Duration.ofSeconds(20));
        this.propertyMap = parseProperties(this.description);
        this.websocketDownstream = webSocketConnectionFactory.create(this, this.getEventStreamUri(), connectionListener,
                pingPeriod);
    }

    private static Map<String, Property> parseProperties(WebThingDescription description) {
        Map<String, Property> propertyMap = new HashMap<>();
        for (var propertyName : description.properties.keySet()) {
            propertyMap.put(propertyName, description.properties.get(propertyName));
        }
        return Collections.unmodifiableMap(propertyMap);
    }

    private URI getPropertyUri(String propertyName) {
        var propertyDescription = description.properties.get(propertyName);
        if (propertyDescription != null) {
            for (var link : propertyDescription.links) {
                if ((link.rel != null) && (link.href != null) && link.rel.equals("property")) {
                    return webThingURI.resolve(link.href);
                }
            }
        }
        throw new RuntimeException("WebThing resource " + webThingURI
                + " does not support a property uri. WebThing description: " + description);
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
        throw new RuntimeException("webthing resource " + webThingURI
                + " does not support websocket uri. WebThing description: " + this.description);
    }

    @Override
    public void destroy() {
        this.websocketDownstream.close();
    }

    @Override
    public WebThingDescription getThingDescription() {
        return this.description;
    }

    @Override
    public void observeProperty(String propertyName, PropertyChangedListener listener) throws IOException {
        this.websocketDownstream.observeProperty(propertyName, listener);
        var value = readProperty(propertyName);
        listener.onPropertyValueChanged(this, propertyName, value);
    }

    @Override
    public Object readProperty(String propertyName) {
        var propertyUri = getPropertyUri(propertyName);
        try {
            var request = HttpRequest.newBuilder().timeout(Duration.ofSeconds(30)).GET().uri(propertyUri).build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                connectionListener.onDisconnected("WebThing resource " + webThingURI + " disconnected");
                throw new IOException("Got error response: " + response.body());
            }
            var propertValue = new Gson().fromJson(response.body(), Map.class);
            if (propertValue.containsKey(propertyName)) {
                return propertValue.get(propertyName);
            } else {
                throw new IOException(
                        "response does not include " + propertyName + "(" + propertyUri + "): " + response.body());
            }
        } catch (InterruptedException | IOException e) {
            connectionListener.onDisconnected("WebThing resource " + webThingURI + " disconnected");
            throw new RuntimeException("could not read " + propertyName + " (" + propertyUri + "). " + e.getMessage());
        }
    }

    @Override
    public void writeProperty(String propertyName, Object newValue) {
        var property = this.propertyMap.get(propertyName);
        var propertyUri = getPropertyUri(propertyName);
        try {
            if (property.readOnly) {
                throw new IOException(propertyName + " is readOnly");
            } else {
                logger.debug("updating {} with {}", propertyName, newValue);
                Map<String, Object> payload = Map.of(propertyName, newValue);
                var json = new Gson().toJson(payload);
                var request = HttpRequest.newBuilder().timeout(Duration.ofSeconds(30))
                        .PUT(HttpRequest.BodyPublishers.ofString(json)).uri(propertyUri).build();
                var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException("Got error response: " + response.body());
                }
            }
        } catch (InterruptedException | IOException e) {
            connectionListener.onDisconnected("WebThing resource " + webThingURI + " disconnected");
            throw new RuntimeException("could not write " + propertyName + " (" + propertyUri + ") with " + newValue
                    + " " + e.getMessage());
        }
    }

    /**
     * Gets the property description
     *
     * @param propertyName the propertyName
     * @return the description (meta data) of the property
     */
    public Property getPropertyDescription(String propertyName) {
        return propertyMap.get(propertyName);
    }

    @Override
    public String toString() {
        return "WebThing " + description.title + " (" + webThingURI + ")";
    }
}
