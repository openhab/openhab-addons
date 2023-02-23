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
package org.openhab.binding.bticinosmarther.internal.factory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.bticinosmarther.internal.SmartherBindingConstants;
import org.openhab.binding.bticinosmarther.internal.account.SmartherAccountService;
import org.openhab.binding.bticinosmarther.internal.handler.SmartherBridgeHandler;
import org.openhab.binding.bticinosmarther.internal.handler.SmartherDynamicStateDescriptionProvider;
import org.openhab.binding.bticinosmarther.internal.handler.SmartherModuleHandler;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code SmartherHandlerFactory} class is responsible for creating things and thing handlers.
 *
 * @author Fabio Possieri - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.bticinosmarther")
@NonNullByDefault
public class SmartherHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SmartherHandlerFactory.class);

    private final OAuthFactory oAuthFactory;
    private final SmartherAccountService authService;
    private final HttpClient httpClient;
    private final CronScheduler cronScheduler;
    private final SmartherDynamicStateDescriptionProvider dynamicStateDescriptionProvider;

    @Activate
    public SmartherHandlerFactory(@Reference OAuthFactory oAuthFactory, @Reference SmartherAccountService authService,
            @Reference HttpClientFactory httpClientFactory, @Reference CronScheduler cronScheduler,
            @Reference SmartherDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        this.oAuthFactory = oAuthFactory;
        this.authService = authService;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.cronScheduler = cronScheduler;
        this.dynamicStateDescriptionProvider = dynamicStateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SmartherBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SmartherBindingConstants.THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            final SmartherBridgeHandler handler = new SmartherBridgeHandler((Bridge) thing, oAuthFactory, httpClient);
            this.authService.addSmartherAccountHandler(handler);
            return handler;
        } else if (SmartherBindingConstants.THING_TYPE_MODULE.equals(thingTypeUID)) {
            return new SmartherModuleHandler(thing, cronScheduler, dynamicStateDescriptionProvider);
        } else {
            logger.debug("Unsupported thing {}", thing.getThingTypeUID());
            return null;
        }
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof SmartherBridgeHandler) {
            authService.removeSmartherAccountHandler((SmartherBridgeHandler) thingHandler);
        }
    }
}
