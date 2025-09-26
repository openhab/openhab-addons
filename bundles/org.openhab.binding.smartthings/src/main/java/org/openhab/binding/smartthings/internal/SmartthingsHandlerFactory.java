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
package org.openhab.binding.smartthings.internal;

import static org.openhab.binding.smartthings.internal.SmartthingsBindingConstants.THING_TYPE_SMARTTHINGSCLOUD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.handler.SmartthingsBridgeHandler;
import org.openhab.binding.smartthings.internal.handler.SmartthingsCloudBridgeHandler;
import org.openhab.binding.smartthings.internal.handler.SmartthingsThingHandler;
import org.openhab.binding.smartthings.internal.type.SmartthingsTypeRegistry;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartthingsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
@Component(service = { ThingHandlerFactory.class }, configurationPid = "binding.smarthings")
public class SmartthingsHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SmartthingsHandlerFactory.class);

    private @Nullable SmartthingsBridgeHandler bridgeHandler = null;
    private @Nullable ThingUID bridgeUID;
    private List<SmartthingsThingHandler> thingHandlers = Collections.synchronizedList(new ArrayList<>());
    private @NonNullByDefault({}) HttpService httpService;
    private final HttpClientFactory httpClientFactory;
    private final SmartthingsAuthService authService;
    private final OAuthFactory oAuthFactory;
    private final SmartthingsTypeRegistry typeRegistry;
    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SmartthingsBindingConstants.BINDING_ID.equals(thingTypeUID.getBindingId());
    }

    @Activate
    public SmartthingsHandlerFactory(final @Reference HttpService httpService,
            final @Reference SmartthingsAuthService authService, final @Reference OAuthFactory oAuthFactory,
            final @Reference HttpClientFactory httpClientFactory,
            final @Reference SmartthingsTypeRegistry typeRegistery, final @Reference ClientBuilder clientBuilder,
            @Reference SseEventSourceFactory eventSourceFactory) {
        this.httpService = httpService;
        this.authService = authService;
        this.httpClientFactory = httpClientFactory;
        this.oAuthFactory = oAuthFactory;
        this.typeRegistry = typeRegistery;
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_SMARTTHINGSCLOUD)) {
            // This binding only supports one bridge. If the user tries to add a second bridge register and error and
            // ignore
            if (bridgeHandler != null) {
                logger.warn(
                        "The Smartthings binding only supports one bridge. Please change your configuration to only use one Bridge. This bridge {} will be ignored.",
                        thing.getUID().getAsString());
                return null;
            }

            bridgeHandler = new SmartthingsCloudBridgeHandler((Bridge) thing, this, authService, bundleContext,
                    httpService, oAuthFactory, httpClientFactory, typeRegistry, clientBuilder, eventSourceFactory);

            SmartthingsAccountHandler accountHandler = bridgeHandler;
            authService.setSmartthingsAccountHandler(accountHandler);
            authService.initialize();

            bridgeUID = thing.getUID();
            logger.debug("SmartthingsHandlerFactory created CloudBridgeHandler for {}", thingTypeUID.getAsString());
            return bridgeHandler;
        } else if (SmartthingsBindingConstants.BINDING_ID.equals(thing.getThingTypeUID().getBindingId())) {
            // Everything but the bridge is handled by this one handler
            // Make sure this thing belongs to the registered Bridge
            if (bridgeUID != null && !bridgeUID.equals(thing.getBridgeUID())) {
                logger.warn("Thing: {} is being ignored because it does not belong to the registered bridge.",
                        thing.getLabel());
                return null;
            }
            SmartthingsThingHandler thingHandler = new SmartthingsThingHandler(thing);
            thingHandlers.add(thingHandler);
            logger.debug("SmartthingsHandlerFactory created ThingHandler for {}, {}",
                    thing.getConfiguration().get("smartthingsName"), thing.getUID().getAsString());
            return thingHandler;
        }
        return null;
    }
}
