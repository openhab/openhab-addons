/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.livisismarthome.internal;

import static org.openhab.binding.livisismarthome.internal.LivisiBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.livisismarthome.internal.handler.LivisiBridgeHandler;
import org.openhab.binding.livisismarthome.internal.handler.LivisiDeviceHandler;
import org.openhab.binding.livisismarthome.internal.util.Translator;
import org.openhab.binding.livisismarthome.internal.util.TranslatorImpl;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LivisiHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Oliver Kuhl - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored to use openHAB http and oauth2 libraries
 * @author Sven Strohschein - Renamed from Innogy to Livisi
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.livisismarthome")
@NonNullByDefault
public class LivisiHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(LivisiHandlerFactory.class);

    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;
    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;

    @Activate
    public LivisiHandlerFactory(@Reference OAuthFactory oAuthFactory, @Reference HttpClientFactory httpClientFactory,
            @Reference TranslationProvider translationProvider, @Reference LocaleProvider localeProvider) {
        this.oAuthFactory = oAuthFactory;
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_BRIDGE.equals(thingTypeUID) || SUPPORTED_DEVICE_THING_TYPES.contains(thingTypeUID)) {
            Translator translator = new TranslatorImpl(translationProvider, localeProvider, getBundleContext());
            if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
                return new LivisiBridgeHandler((Bridge) thing, oAuthFactory, httpClient, translator);
            } else if (SUPPORTED_DEVICE_THING_TYPES.contains(thingTypeUID)) {
                return new LivisiDeviceHandler(thing, translator);
            }
        }
        logger.debug("Unsupported thing {}.", thingTypeUID);
        return null;
    }
}
