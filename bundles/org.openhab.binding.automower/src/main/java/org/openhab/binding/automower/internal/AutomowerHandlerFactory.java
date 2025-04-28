/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.automower.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.automower.internal.bridge.AutomowerBridgeHandler;
import org.openhab.binding.automower.internal.discovery.AutomowerDiscoveryService;
import org.openhab.binding.automower.internal.things.AutomowerHandler;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AutomowerHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Markus Pfleger - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.automower", service = ThingHandlerFactory.class)
public class AutomowerHandlerFactory extends BaseThingHandlerFactory {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.unmodifiableSet(Stream
            .of(AutomowerBridgeHandler.SUPPORTED_THING_TYPES.stream(), AutomowerHandler.SUPPORTED_THING_TYPES.stream())
            .flatMap(Function.identity()).collect(Collectors.toSet()));

    private final OAuthFactory oAuthFactory;
    protected final @NonNullByDefault({}) HttpClient httpClient;
    private @Nullable ServiceRegistration<?> automowerDiscoveryServiceRegistration;
    private final TimeZoneProvider timeZoneProvider;
    private final WebSocketClient webSocketClient;
    private final Logger logger = LoggerFactory.getLogger(AutomowerHandlerFactory.class);
    private Map<String, AutomowerHandler> automowerHandlers = new HashMap<>();

    @Activate
    public AutomowerHandlerFactory(@Reference OAuthFactory oAuthFactory, @Reference HttpClientFactory httpClientFactory,
            @Reference TimeZoneProvider timeZoneProvider, @Reference WebSocketFactory webSocketFactory) {
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.timeZoneProvider = timeZoneProvider;
        this.webSocketClient = webSocketFactory.getCommonWebSocketClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (AutomowerBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            AutomowerBridgeHandler handler = new AutomowerBridgeHandler((Bridge) thing, oAuthFactory, httpClient,
                    webSocketClient, automowerHandlers);
            registerAutomowerDiscoveryService(handler);
            return handler;
        }

        if (AutomowerHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            AutomowerHandler handler = new AutomowerHandler(thing, timeZoneProvider);
            String key = handler.getThing().getUID().getId();
            automowerHandlers.put(key, handler);
            logger.trace("Adding handler {} to map of handlers", key);
            return handler;
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof AutomowerBridgeHandler) {
            if (automowerDiscoveryServiceRegistration != null) {
                automowerDiscoveryServiceRegistration.unregister();
            }
        } else if (thingHandler instanceof AutomowerHandler) {
            String key = thingHandler.getThing().getUID().getId();
            logger.trace("Removing handler {} to map of handlers", key);
            automowerHandlers.remove(key);
        }
    }

    private void registerAutomowerDiscoveryService(AutomowerBridgeHandler handler) {
        AutomowerDiscoveryService discoveryService = new AutomowerDiscoveryService(handler);
        this.automowerDiscoveryServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                discoveryService, new Hashtable<>());
    }
}
