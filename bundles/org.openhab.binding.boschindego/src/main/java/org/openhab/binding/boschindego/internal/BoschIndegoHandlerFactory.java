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
package org.openhab.binding.boschindego.internal;

import static org.openhab.binding.boschindego.internal.BoschIndegoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.boschindego.internal.handler.BoschAccountHandler;
import org.openhab.binding.boschindego.internal.handler.BoschIndegoHandler;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
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
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link BoschIndegoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jonas Fleck - Initial contribution
 * @author Jacob Laursen - Replaced authorization by OAuth2
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.boschindego")
public class BoschIndegoHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClient httpClient;
    private final OAuthFactory oAuthFactory;
    private final BoschIndegoTranslationProvider translationProvider;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public BoschIndegoHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory, final @Reference TranslationProvider i18nProvider,
            final @Reference LocaleProvider localeProvider, final @Reference TimeZoneProvider timeZoneProvider,
            ComponentContext componentContext) {
        super.activate(componentContext);
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.oAuthFactory = oAuthFactory;
        this.translationProvider = new BoschIndegoTranslationProvider(i18nProvider, localeProvider);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return BoschIndegoBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            return new BoschAccountHandler((Bridge) thing, httpClient, oAuthFactory);
        } else if (THING_TYPE_INDEGO.equals(thingTypeUID)) {
            return new BoschIndegoHandler(thing, httpClient, translationProvider, timeZoneProvider);
        }

        return null;
    }
}
