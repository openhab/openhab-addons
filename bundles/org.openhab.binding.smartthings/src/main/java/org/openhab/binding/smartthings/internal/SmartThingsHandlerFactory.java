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

import static org.openhab.binding.smartthings.internal.SmartThingsBindingConstants.THING_TYPE_ACCOUNT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.handler.SmartThingsAccountHandler;
import org.openhab.binding.smartthings.internal.handler.SmartThingsBridgeHandler;
import org.openhab.binding.smartthings.internal.handler.SmartThingsThingHandler;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.io.openhabcloud.WebhookService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartThingsHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Bob Raker - Initial contribution
 * @author Laurent Arnal - Refactor for new version of the binding
 */
@NonNullByDefault
@Component(service = { ThingHandlerFactory.class }, configurationPid = "binding.smarthings")
public class SmartThingsHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SmartThingsHandlerFactory.class);

    private List<SmartThingsThingHandler> thingHandlers = Collections.synchronizedList(new ArrayList<>());

    private @Nullable SmartThingsBridgeHandler bridgeHandler = null;
    private @Nullable ThingUID bridgeUID;
    private @NonNullByDefault({}) HttpService httpService;

    private final HttpClientFactory httpClientFactory;
    private final SmartThingsAuthService authService;
    private final TranslationProvider translationProvider;
    private final OAuthFactory oAuthFactory;
    private final SmartThingsTypeRegistry typeRegistry;
    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;
    private final WebhookService webHookService;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SmartThingsBindingConstants.BINDING_ID.equals(thingTypeUID.getBindingId());
    }

    @Activate
    public SmartThingsHandlerFactory(final @Reference HttpService httpService,
            final @Reference SmartThingsAuthService authService,
            final @Reference TranslationProvider translationProvider, final @Reference OAuthFactory oAuthFactory,
            final @Reference HttpClientFactory httpClientFactory,
            final @Reference SmartThingsTypeRegistry typeRegistery, final @Reference ClientBuilder clientBuilder,
            final @Reference SseEventSourceFactory eventSourceFactory, final @Reference WebhookService webHookService) {
        this.httpService = httpService;
        this.authService = authService;
        this.translationProvider = translationProvider;
        this.httpClientFactory = httpClientFactory;
        this.oAuthFactory = oAuthFactory;
        this.typeRegistry = typeRegistery;
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
        this.webHookService = webHookService;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ACCOUNT)) {
            // This binding only supports one bridge. If the user tries to add a second bridge register and error
            // and ignore
            if (bridgeHandler != null) {
                logger.warn(
                        "The SmartThings binding only supports one bridge. Please change your configuration to only use one Bridge. This bridge {} will be ignored.",
                        thing.getUID().getAsString());
                return null;
            }

            bridgeHandler = new SmartThingsAccountHandler((Bridge) thing, this, authService, translationProvider,
                    bundleContext, httpService, oAuthFactory, httpClientFactory, typeRegistry, clientBuilder,
                    eventSourceFactory, webHookService);

            SmartThingsBridgeHandler accountHandler = bridgeHandler;
            authService.setSmartThingsAccountHandler(accountHandler);

            bridgeUID = thing.getUID();
            logger.debug("SmartThingsHandlerFactory created SmartThingsAccountHandler for {}",
                    thingTypeUID.getAsString());
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
