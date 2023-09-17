/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import static org.openhab.binding.smartthings.internal.SmartThingsBindingConstants.THING_TYPE_SMARTTHINGSCLOUD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.handler.SmartThingsBridgeHandler;
import org.openhab.binding.smartthings.internal.handler.SmartThingsCloudBridgeHandler;
import org.openhab.binding.smartthings.internal.handler.SmartThingsThingHandler;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
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
 * The {@link SmartThingsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
@Component(service = { ThingHandlerFactory.class }, configurationPid = "binding.smarthings")
public class SmartThingsHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SmartThingsHandlerFactory.class);

    private @Nullable SmartThingsBridgeHandler bridgeHandler = null;
    private @Nullable ThingUID bridgeUID;
    private List<SmartThingsThingHandler> thingHandlers = Collections.synchronizedList(new ArrayList<>());
    private @NonNullByDefault({}) HttpService httpService;
    private final HttpClientFactory httpClientFactory;
    private final SmartThingsAuthService authService;
    private final OAuthFactory oAuthFactory;
    private final SmartThingsTypeRegistry typeRegistry;
    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SmartThingsBindingConstants.BINDING_ID.equals(thingTypeUID.getBindingId());
    }

    @Activate
    public SmartThingsHandlerFactory(final @Reference HttpService httpService,
            final @Reference SmartThingsAuthService authService, final @Reference OAuthFactory oAuthFactory,
            final @Reference HttpClientFactory httpClientFactory,
            final @Reference SmartThingsTypeRegistry typeRegistery, final @Reference ClientBuilder clientBuilder,
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
                        "The SmartThings binding only supports one bridge. Please change your configuration to only use one Bridge. This bridge {} will be ignored.",
                        thing.getUID().getAsString());
                return bridgeHandler;
            }

            bridgeHandler = new SmartThingsCloudBridgeHandler((Bridge) thing, this, authService, bundleContext,
                    httpService, oAuthFactory, httpClientFactory, typeRegistry, clientBuilder, eventSourceFactory);

            SmartThingsAccountHandler accountHandler = bridgeHandler;
            authService.setSmartThingsAccountHandler(accountHandler);
            authService.initialize();

            bridgeUID = thing.getUID();
            logger.debug("SmartThingsHandlerFactory created CloudBridgeHandler for {}", thingTypeUID.getAsString());
            return bridgeHandler;
        } else if (SmartThingsBindingConstants.BINDING_ID.equals(thing.getThingTypeUID().getBindingId())) {
            ThingUID bridgeUID = this.bridgeUID;
            // Everything but the bridge is handled by this one handler
            // Make sure this thing belongs to the registered Bridge
            if (bridgeUID != null && !bridgeUID.equals(thing.getBridgeUID())) {
                logger.warn("Thing: {} is being ignored because it does not belong to the registered bridge.",
                        thing.getLabel());
                return null;
            }
            SmartThingsThingHandler thingHandler = new SmartThingsThingHandler(thing);
            thingHandlers.add(thingHandler);
            logger.debug("SmartThingsHandlerFactory created ThingHandler for {}, {}",
                    thing.getConfiguration().get("smartthingsName"), thing.getUID().getAsString());
            return thingHandler;
        }
        return null;
    }
}
