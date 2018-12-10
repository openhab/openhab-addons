/*
 * Copyright (c) 2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.http.handler.HttpThingHandler;
import org.openhab.binding.http.model.HttpHandlerFactoryConfig;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
@NonNullByDefault
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

    private final Map<ThingUID, HttpThingHandler> handlers = new HashMap<>();

    private HttpClient httpClient;
    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private HttpHandlerFactoryConfig config = new HttpHandlerFactoryConfig();

    @Override
    protected ThingHandler createHandler(final Thing thing) {
        if (supportsThingType(thing.getThingTypeUID())) {
            final HttpThingHandler handler = new HttpThingHandler(
                    thing, this.httpClient, this.itemChannelLinkRegistry,
                    this.config.getConnectTimeout(), this.config.getRequestTimeout()
            );
            this.handlers.put(thing.getUID(), handler);
            return handler;
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
        final Map<String, Object> properties = dictionaryToMap(componentContext.getProperties());
        this.config = new Configuration(properties).as(HttpHandlerFactoryConfig.class);
        this.handlers.forEach((uid, handler) -> handler.updateBindingConfig(this.config.getConnectTimeout(), this.config.getRequestTimeout()));
    }

    @Reference
    @SuppressWarnings("unused")
    protected void setHttpClientFactory(final HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @SuppressWarnings("unused")
    protected void unsetHttpClientFactory(final HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }

    @Reference
    @SuppressWarnings("unused")
    protected void setItemChannelLinkRegistry(final ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    @SuppressWarnings("unused")
    protected void unsetItemChannelLinkRegistry(final ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = null;
    }

    private <K, V> Map<K, V> dictionaryToMap(final Dictionary<K, V> dict) {
        final Map<K, V> map = new HashMap<>(dict.size());
        final Enumeration<K> keys = dict.keys();
        while (keys.hasMoreElements()) {
            final K key = keys.nextElement();
            map.put(key, dict.get(key));
        }
        return map;
    }
}
