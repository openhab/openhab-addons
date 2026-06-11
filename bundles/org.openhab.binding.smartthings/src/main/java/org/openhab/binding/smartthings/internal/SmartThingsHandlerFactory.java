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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.handler.SmartThingsAccountHandler;
import org.openhab.binding.smartthings.internal.handler.SmartThingsBridgeHandler;
import org.openhab.binding.smartthings.internal.handler.SmartThingsThingHandler;
import org.openhab.binding.smartthings.internal.type.SmartThingsThingTypeProvider;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.rest.WebhookService;
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
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
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
@Component(service = { ThingHandlerFactory.class }, configurationPid = "binding.smartthings")
public class SmartThingsHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SmartThingsHandlerFactory.class);

    private final List<SmartThingsThingHandler> thingHandlers = Collections.synchronizedList(new ArrayList<>());
    private final Map<ThingUID, SmartThingsBridgeHandler> bridgeHandlers = new ConcurrentHashMap<>();
    private @NonNullByDefault({}) HttpService httpService;

    private final HttpClientFactory httpClientFactory;
    private final SmartThingsAuthService authService;
    private final TranslationProvider translationProvider;
    private final OAuthFactory oAuthFactory;
    private final SmartThingsTypeRegistry typeRegistry;
    private final SmartThingsThingTypeProvider thingTypeProvider;
    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;
    private volatile @Nullable WebhookService webHookService;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SmartThingsBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)
                || thingTypeProvider.getInternalThingType(thingTypeUID) != null;
    }

    @Activate
    public SmartThingsHandlerFactory(final @Reference HttpService httpService,
            final @Reference SmartThingsAuthService authService,
            final @Reference TranslationProvider translationProvider, final @Reference OAuthFactory oAuthFactory,
            final @Reference HttpClientFactory httpClientFactory, final @Reference SmartThingsTypeRegistry typeRegistry,
            final @Reference ClientBuilder clientBuilder, final @Reference SseEventSourceFactory eventSourceFactory,
            final @Reference SmartThingsThingTypeProvider thingTypeProvider) {
        this.httpService = httpService;
        this.authService = authService;
        this.translationProvider = translationProvider;
        this.httpClientFactory = httpClientFactory;
        this.oAuthFactory = oAuthFactory;
        this.typeRegistry = typeRegistry;
        this.thingTypeProvider = thingTypeProvider;
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setWebHookService(WebhookService webHookService) {
        this.webHookService = webHookService;
        for (SmartThingsBridgeHandler handler : bridgeHandlers.values()) {
            handler.setWebHookService(webHookService);
        }
    }

    protected void unsetWebHookService(WebhookService webHookService) {
        if (webHookService.equals(this.webHookService)) {
            for (SmartThingsBridgeHandler handler : bridgeHandlers.values()) {
                handler.unsetWebHookService(webHookService);
            }
            this.webHookService = null;
        }
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ACCOUNT)) {
            ThingUID bridgeUID = thing.getUID();
            SmartThingsBridgeHandler bridgeHandler = new SmartThingsAccountHandler((Bridge) thing, this, authService,
                    translationProvider, bundleContext, httpService, oAuthFactory, httpClientFactory, typeRegistry,
                    clientBuilder, eventSourceFactory, webHookService);
            bridgeHandlers.put(bridgeUID, bridgeHandler);
            authService.setSmartThingsOAuthHandler(bridgeUID, bridgeHandler);

            logger.debug("SmartThingsHandlerFactory created SmartThingsAccountHandler for {}",
                    thingTypeUID.getAsString());
            return bridgeHandler;
        } else if (canCreateThingHandler(thing)) {
            ThingUID bridgeUID = thing.getBridgeUID();
            // Everything but the bridge is handled by this one handler
            // Make sure this thing belongs to a registered bridge
            if (bridgeUID == null || !bridgeHandlers.containsKey(bridgeUID)) {
                logger.warn("Thing: {} is being ignored because it does not belong to a registered bridge.",
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

    private boolean canCreateThingHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (SmartThingsBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return true;
        }

        ThingUID bridgeUID = thing.getBridgeUID();
        SmartThingsBridgeHandler bridgeHandler = bridgeUID != null ? bridgeHandlers.get(bridgeUID) : null;
        return bridgeHandler != null && bridgeHandler.useDynamicThings()
                && thingTypeProvider.getInternalThingType(thingTypeUID) != null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof SmartThingsBridgeHandler bridgeHandler) {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            bridgeHandlers.remove(bridgeUID, bridgeHandler);
            authService.unsetSmartThingsOAuthHandler(bridgeUID, bridgeHandler);
            synchronized (thingHandlers) {
                thingHandlers.removeIf(handler -> bridgeUID.equals(handler.getThing().getBridgeUID()));
            }
        } else if (thingHandler instanceof SmartThingsThingHandler smartThingsHandler) {
            thingHandlers.remove(smartThingsHandler);
        }
        super.removeHandler(thingHandler);
    }
}
