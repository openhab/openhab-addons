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
package org.openhab.binding.bluelink.internal;

import static org.openhab.binding.bluelink.internal.BluelinkBindingConstants.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.bluelink.internal.handler.BluelinkAccountHandler;
import org.openhab.binding.bluelink.internal.handler.BluelinkVehicleHandler;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BluelinkHandlerFactory} creates handlers for Bluelink things.
 *
 * @author Marcus Better - Initial contribution
 * @author Carlo Dischler - HTTP client per account
 */
@NonNullByDefault
@Component(configurationPid = "binding.bluelink", service = ThingHandlerFactory.class)
public class BluelinkHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(BluelinkHandlerFactory.class);

    private final HttpClientFactory httpClientFactory;
    private final TimeZoneProvider timeZoneProvider;
    private final LocaleProvider localeProvider;
    // each account gets its own client so that login cookies are never shared between accounts
    private final Map<ThingHandler, HttpClient> httpClients = new ConcurrentHashMap<>();

    @Activate
    public BluelinkHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference TimeZoneProvider timeZoneProvider, final @Reference LocaleProvider localeProvider) {
        this.httpClientFactory = httpClientFactory;
        this.timeZoneProvider = timeZoneProvider;
        this.localeProvider = localeProvider;
    }

    @Override
    @Deactivate
    protected void deactivate(final ComponentContext componentContext) {
        httpClients.values().forEach(this::stopHttpClient);
        httpClients.clear();
        super.deactivate(componentContext);
    }

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            final HttpClient httpClient = startHttpClient();
            final BluelinkAccountHandler handler = new BluelinkAccountHandler((Bridge) thing, httpClient,
                    timeZoneProvider, localeProvider);
            httpClients.put(handler, httpClient);
            return handler;
        }
        if (THING_TYPE_VEHICLE.equals(thingTypeUID)) {
            return new BluelinkVehicleHandler(thing);
        }
        return null;
    }

    @Override
    protected void removeHandler(final ThingHandler thingHandler) {
        final HttpClient httpClient = httpClients.remove(thingHandler);
        if (httpClient != null) {
            stopHttpClient(httpClient);
        }
    }

    private HttpClient startHttpClient() {
        final HttpClient httpClient = httpClientFactory.createHttpClient(BINDING_ID);
        httpClient.setRequestBufferSize(HTTP_REQUEST_BUFFER_SIZE);
        try {
            httpClient.start();
        } catch (final Exception e) {
            stopHttpClient(httpClient);
            throw new IllegalStateException("Failed to start HTTP client", e);
        }
        return httpClient;
    }

    private void stopHttpClient(final HttpClient httpClient) {
        try {
            httpClient.stop();
        } catch (final Exception e) {
            logger.debug("Failed to stop HTTP client", e);
        }
    }
}
