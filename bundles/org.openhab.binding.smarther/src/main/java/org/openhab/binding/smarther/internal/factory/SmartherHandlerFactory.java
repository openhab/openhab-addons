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
package org.openhab.binding.smarther.internal.factory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthFactory;
import org.eclipse.smarthome.core.scheduler.CronScheduler;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.smarther.internal.SmartherBindingConstants;
import org.openhab.binding.smarther.internal.account.SmartherAccountService;
import org.openhab.binding.smarther.internal.handler.SmartherBridgeHandler;
import org.openhab.binding.smarther.internal.handler.SmartherDynamicStateDescriptionProvider;
import org.openhab.binding.smarther.internal.handler.SmartherModuleHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SmartherHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Fabio Possieri - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.smarther")
@NonNullByDefault
public class SmartherHandlerFactory extends BaseThingHandlerFactory {

    private @NonNullByDefault({}) OAuthFactory oAuthFactory;
    private @NonNullByDefault({}) SmartherAccountService authService;
    private @NonNullByDefault({}) HttpClient httpClient;
    private @NonNullByDefault({}) CronScheduler cronScheduler;
    private @NonNullByDefault({}) SmartherDynamicStateDescriptionProvider dynamicStateDescriptionProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SmartherBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SmartherBindingConstants.THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            final SmartherBridgeHandler handler = new SmartherBridgeHandler((Bridge) thing, oAuthFactory, httpClient);
            authService.addSmartherAccountHandler(handler);
            return handler;
        } else if (SmartherBindingConstants.THING_TYPE_MODULE.equals(thingTypeUID)) {
            return new SmartherModuleHandler(thing, cronScheduler, dynamicStateDescriptionProvider);
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof SmartherBridgeHandler) {
            authService.removeSmartherAccountHandler((SmartherBridgeHandler) thingHandler);
        }
    }

    @Reference
    protected void setOAuthFactory(OAuthFactory oAuthFactory) {
        this.oAuthFactory = oAuthFactory;
    }

    protected void unsetOAuthFactory(OAuthFactory oAuthFactory) {
        this.oAuthFactory = null;
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }

    @Reference
    protected void setAuthService(SmartherAccountService service) {
        this.authService = service;
    }

    protected void unsetAuthService(SmartherAccountService service) {
        this.authService = null;
    }

    @Reference
    protected void setCronScheduler(CronScheduler scheduler) {
        this.cronScheduler = scheduler;
    }

    protected void unsetCronScheduler(CronScheduler scheduler) {
        this.cronScheduler = null;
    }

    @Reference
    protected void setDynamicStateDescriptionProvider(SmartherDynamicStateDescriptionProvider provider) {
        this.dynamicStateDescriptionProvider = provider;
    }

    protected void unsetDynamicStateDescriptionProvider(SmartherDynamicStateDescriptionProvider provider) {
        this.dynamicStateDescriptionProvider = null;
    }

}
