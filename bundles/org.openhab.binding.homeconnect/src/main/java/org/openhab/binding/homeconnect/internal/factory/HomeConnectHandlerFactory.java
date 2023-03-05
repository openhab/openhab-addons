/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.homeconnect.internal.factory;

import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.*;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectBridgeHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectCoffeeMakerHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectCooktopHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectDishwasherHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectDryerHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectFridgeFreezerHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectHoodHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectOvenHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectWasherDryerHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectWasherHandler;
import org.openhab.binding.homeconnect.internal.servlet.HomeConnectServlet;
import org.openhab.binding.homeconnect.internal.type.HomeConnectDynamicStateDescriptionProvider;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;

/**
 * The {@link HomeConnectHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.homeconnect", service = ThingHandlerFactory.class)
public class HomeConnectHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClient httpClient;
    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;
    private final OAuthFactory oAuthFactory;
    private final HomeConnectDynamicStateDescriptionProvider dynamicStateDescriptionProvider;
    private final HomeConnectServlet homeConnectServlet;

    @Activate
    public HomeConnectHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference ClientBuilder clientBuilder, @Reference SseEventSourceFactory eventSourceFactory,
            @Reference OAuthFactory oAuthFactory,
            @Reference HomeConnectDynamicStateDescriptionProvider dynamicStateDescriptionProvider,
            @Reference HomeConnectServlet homeConnectServlet) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
        this.oAuthFactory = oAuthFactory;
        this.dynamicStateDescriptionProvider = dynamicStateDescriptionProvider;
        this.homeConnectServlet = homeConnectServlet;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_DEVICE_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_API_BRIDGE.equals(thingTypeUID)) {
            return new HomeConnectBridgeHandler((Bridge) thing, httpClient, clientBuilder, eventSourceFactory,
                    oAuthFactory, homeConnectServlet);
        } else if (THING_TYPE_DISHWASHER.equals(thingTypeUID)) {
            return new HomeConnectDishwasherHandler(thing, dynamicStateDescriptionProvider);
        } else if (THING_TYPE_OVEN.equals(thingTypeUID)) {
            return new HomeConnectOvenHandler(thing, dynamicStateDescriptionProvider);
        } else if (THING_TYPE_WASHER.equals(thingTypeUID)) {
            return new HomeConnectWasherHandler(thing, dynamicStateDescriptionProvider);
        } else if (THING_TYPE_WASHER_DRYER.equals(thingTypeUID)) {
            return new HomeConnectWasherDryerHandler(thing, dynamicStateDescriptionProvider);
        } else if (THING_TYPE_DRYER.equals(thingTypeUID)) {
            return new HomeConnectDryerHandler(thing, dynamicStateDescriptionProvider);
        } else if (THING_TYPE_FRIDGE_FREEZER.equals(thingTypeUID)) {
            return new HomeConnectFridgeFreezerHandler(thing, dynamicStateDescriptionProvider);
        } else if (THING_TYPE_COFFEE_MAKER.equals(thingTypeUID)) {
            return new HomeConnectCoffeeMakerHandler(thing, dynamicStateDescriptionProvider);
        } else if (THING_TYPE_HOOD.equals(thingTypeUID)) {
            return new HomeConnectHoodHandler(thing, dynamicStateDescriptionProvider);
        } else if (THING_TYPE_COOKTOP.equals(thingTypeUID)) {
            return new HomeConnectCooktopHandler(thing, dynamicStateDescriptionProvider);
        }

        return null;
    }
}
