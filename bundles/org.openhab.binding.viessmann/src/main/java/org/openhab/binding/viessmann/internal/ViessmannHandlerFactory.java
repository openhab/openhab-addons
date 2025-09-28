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
package org.openhab.binding.viessmann.internal;

import static org.openhab.binding.viessmann.internal.ViessmannBindingConstants.*;

import java.util.Dictionary;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.viessmann.internal.handler.DeviceHandler;
import org.openhab.binding.viessmann.internal.handler.ViessmannAccountHandler;
import org.openhab.binding.viessmann.internal.handler.ViessmannBridgeHandler;
import org.openhab.binding.viessmann.internal.handler.ViessmannGatewayHandler;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ViessmannHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.viessmann", service = ThingHandlerFactory.class)
public class ViessmannHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(ViessmannHandlerFactory.class);

    private final HttpClientFactory httpClientFactory;
    private final StorageService storageService;
    private final BindingServlet bindingServlet;
    private final ViessmannDynamicStateDescriptionProvider stateDescriptionProvider;
    private final ItemChannelLinkRegistry linkRegistry;
    private final LocaleProvider localeProvider;
    private final TranslationProvider i18Provider;
    private @Nullable String callbackUrl;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_GATEWAY,
            THING_TYPE_BRIDGE, THING_TYPE_DEVICE);

    @Activate
    public ViessmannHandlerFactory(final @Reference HttpService httpService,
            final @Reference HttpClientFactory httpClientFactory, final @Reference StorageService storageService,
            final @Reference ViessmannDynamicStateDescriptionProvider stateDescriptionProvider,
            final @Reference ItemChannelLinkRegistry linkRegistry, final @Reference LocaleProvider localeProvider,
            final @Reference TranslationProvider i18Provider) {
        this.httpClientFactory = httpClientFactory;
        this.bindingServlet = new BindingServlet(httpService);
        this.storageService = storageService;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.linkRegistry = linkRegistry;
        this.localeProvider = localeProvider;
        this.i18Provider = i18Provider;
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        Dictionary<String, Object> properties = componentContext.getProperties();
        callbackUrl = (String) properties.get("callbackUrl");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        bindingServlet.dispose();
        super.deactivate(componentContext);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            Storage<String> storage = storageService.getStorage(thing.getUID().toString(),
                    String.class.getClassLoader());
            return new ViessmannAccountHandler((Bridge) thing, storage, httpClientFactory.getCommonHttpClient(),
                    createCallbackUrl(), linkRegistry);
        } else if (THING_TYPE_GATEWAY.equals(thingTypeUID)) {
            return new ViessmannGatewayHandler((Bridge) thing, linkRegistry);
        } else if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            Storage<String> storage = storageService.getStorage(thing.getUID().toString(),
                    String.class.getClassLoader());
            return new ViessmannBridgeHandler((Bridge) thing, storage, httpClientFactory.getCommonHttpClient(),
                    createCallbackUrl(), linkRegistry);
        } else if (THING_TYPE_DEVICE.equals(thingTypeUID)) {
            return new DeviceHandler(thing, stateDescriptionProvider, linkRegistry, i18Provider, localeProvider);
        }
        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
    }

    private @Nullable String createCallbackUrl() {
        if (callbackUrl != null && !callbackUrl.isBlank()) {
            return callbackUrl;
        } else {
            // we do not use SSL as it can cause certificate validation issues.
            final int port = HttpServiceUtil.getHttpServicePort(bundleContext);
            if (port == -1) {
                logger.warn("Cannot find port of the http service.");
                return null;
            }
            return "http://localhost:" + port;
        }
    }
}
