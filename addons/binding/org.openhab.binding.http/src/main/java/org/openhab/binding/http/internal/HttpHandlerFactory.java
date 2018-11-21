/*
 * Copyright (c) 2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.internal;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.http.handler.HttpThingHandler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.openhab.binding.http.HttpBindingConstants.BINDING_ID;
import static org.openhab.binding.http.HttpBindingConstants.CONFIG_CONNECT_TIMEOUT;
import static org.openhab.binding.http.HttpBindingConstants.CONFIG_REQUEST_TIMEOUT;
import static org.openhab.binding.http.HttpBindingConstants.DEFAULT_CONNECT_TIMEOUT;
import static org.openhab.binding.http.HttpBindingConstants.DEFAULT_REQUEST_TIMEOUT;
import static org.openhab.binding.http.HttpBindingConstants.THING_TYPE_COLOR;
import static org.openhab.binding.http.HttpBindingConstants.THING_TYPE_CONTACT;
import static org.openhab.binding.http.HttpBindingConstants.THING_TYPE_DATETIME;
import static org.openhab.binding.http.HttpBindingConstants.THING_TYPE_DIMMER;
import static org.openhab.binding.http.HttpBindingConstants.THING_TYPE_IMAGE;
import static org.openhab.binding.http.HttpBindingConstants.THING_TYPE_LOCATION;
import static org.openhab.binding.http.HttpBindingConstants.THING_TYPE_NUMBER;
import static org.openhab.binding.http.HttpBindingConstants.THING_TYPE_PLAYER;
import static org.openhab.binding.http.HttpBindingConstants.THING_TYPE_ROLLERSHUTTER;
import static org.openhab.binding.http.HttpBindingConstants.THING_TYPE_STRING;
import static org.openhab.binding.http.HttpBindingConstants.THING_TYPE_SWITCH;

/**
 * Handler factory for creating handlers for things of type http.
 *
 * @author Brian J. Tarricone
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.http")
public class HttpHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THINGS = new HashSet<>(Arrays.asList(
            THING_TYPE_COLOR,
            THING_TYPE_CONTACT,
            THING_TYPE_DATETIME,
            THING_TYPE_DIMMER,
            THING_TYPE_IMAGE,
            THING_TYPE_LOCATION,
            THING_TYPE_NUMBER,
            THING_TYPE_PLAYER,
            THING_TYPE_ROLLERSHUTTER,
            THING_TYPE_STRING,
            THING_TYPE_SWITCH
    ));

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<ThingUID, HttpThingHandler> handlers = new HashMap<>();

    private Optional<HttpClient> httpClient = Optional.empty();
    private Optional<ItemChannelLinkRegistry> itemChannelLinkRegistry = Optional.empty();
    private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private Duration requestTimeout = DEFAULT_REQUEST_TIMEOUT;

    @Override
    protected ThingHandler createHandler(final Thing thing) {
        if (supportsThingType(thing.getThingTypeUID())) {
            if (this.httpClient.isPresent() && this.itemChannelLinkRegistry.isPresent()) {
                final HttpThingHandler handler = new HttpThingHandler(thing, this.httpClient.get(), this.itemChannelLinkRegistry.get(), this.connectTimeout, this.requestTimeout);
                this.handlers.put(thing.getUID(), handler);
                return handler;
            } else {
                throw new IllegalStateException("Cannot instantiate HttpThingHandler without a HTTP client or item registry");
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THINGS.contains(thingTypeUID);
    }

    @Override
    public void unregisterHandler(final Thing thing) {
        super.unregisterHandler(thing);
        this.handlers.remove(thing.getUID());
    }

    @Override
    protected void activate(final ComponentContext componentContext) {
        super.activate(componentContext);
        final Dictionary<String, Object> properties = componentContext.getProperties();
        this.connectTimeout = parseDuration(properties, CONFIG_CONNECT_TIMEOUT).orElse(DEFAULT_CONNECT_TIMEOUT);
        this.requestTimeout = parseDuration(properties, CONFIG_REQUEST_TIMEOUT).orElse(DEFAULT_REQUEST_TIMEOUT);
        this.handlers.forEach((uid, handler) -> handler.updateBindingConfig(this.connectTimeout, this.requestTimeout));
    }

    @Reference
    @SuppressWarnings("unused")
    protected void setHttpClientFactory(final HttpClientFactory httpClientFactory) {
        this.httpClient = Optional.of(httpClientFactory.getCommonHttpClient());
    }

    @SuppressWarnings("unused")
    protected void unsetHttpClientFactory(final HttpClientFactory httpClientFactory) {
        this.httpClient = Optional.empty();
    }

    @Reference
    @SuppressWarnings("unused")
    protected void setItemChannelLinkRegistry(final ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = Optional.of(itemChannelLinkRegistry);
    }

    @SuppressWarnings("unused")
    protected void unsetItemChannelLinkRegistry(final ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = Optional.empty();
    }

    private Optional<Duration> parseDuration(final Dictionary<String, Object> properties, final String key) {
        return Optional.ofNullable(properties.get(key)).flatMap(s -> {
            try {
                return Optional.of(Duration.ofMillis(Long.valueOf(s.toString())));
            } catch (final NumberFormatException e) {
                logger.warn("[{}] invalid requestTimeout value supplied ({}); falling back to default", BINDING_ID, s);
                return Optional.empty();
            }
        });
    }

}
