/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.freeathome.internal;

import static org.openhab.binding.freeathome.internal.FreeAtHomeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.freeathome.internal.handler.FreeAtHomeBridgeHandler;
import org.openhab.binding.freeathome.internal.handler.FreeAtHomeDeviceHandler;
import org.openhab.binding.freeathome.internal.type.FreeAtHomeChannelTypeProvider;
import org.openhab.binding.freeathome.internal.type.FreeAtHomeThingTypeProvider;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
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
 * The {@link FreeAtHomeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Andras Uhrin - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.freeathome", service = ThingHandlerFactory.class)
public class FreeAtHomeHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeHandlerFactory.class);

    private final HttpClient httpClient;
    private final FreeAtHomeChannelTypeProvider channelTypeProvider;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    @Activate
    public FreeAtHomeHandlerFactory(@Reference FreeAtHomeThingTypeProvider thingTypeProvider,
            @Reference FreeAtHomeChannelTypeProvider channelTypeProvider, @Reference TranslationProvider i18nProvider,
            @Reference LocaleProvider localeProvider, @Reference HttpClientFactory httpClientFactory,
            ComponentContext componentContext) {
        super.activate(componentContext);
        this.channelTypeProvider = channelTypeProvider;
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;

        // create httpClient
        httpClient = httpClientFactory.createHttpClient(FreeAtHomeBindingConstants.BINDING_ID);

        // Configure client
        httpClient.setFollowRedirects(false);
        httpClient.setMaxConnectionsPerDestination(1);
        httpClient.setMaxRequestsQueuedPerDestination(50);

        // Set timeouts
        httpClient.setIdleTimeout(-1);
        httpClient.setConnectTimeout(5000);

        try {
            // Start HttpClient.
            httpClient.start();
        } catch (Exception ex) {
            logger.error("Could not create HttpClient: {}", ex.getMessage());

            throw new IllegalStateException("Could not create HttpClient", ex);
        }
    }

    @Deactivate
    public void deactivate() {
        try {
            httpClient.stop();
        } catch (Exception ex) {
            logger.warn("Failed to stop HttpClient: {}", ex.getMessage());
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (BRIDGE_TYPE_UID.equals(thingTypeUID)) {
            return new FreeAtHomeBridgeHandler((Bridge) thing, httpClient);
        } else if (DEVICE_TYPE_UID.equals(thingTypeUID)) {
            return new FreeAtHomeDeviceHandler(thing, channelTypeProvider, i18nProvider, localeProvider);
        }

        return null;
    }
}
