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
package org.openhab.binding.ecowatt.internal;

import static org.openhab.binding.ecowatt.internal.EcowattBindingConstants.THING_TYPE_SIGNALS;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.ecowatt.internal.handler.EcowattHandler;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EcowattHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.ecowatt", service = ThingHandlerFactory.class)
public class EcowattHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_SIGNALS);

    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;
    private final TranslationProvider i18nProvider;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public EcowattHandlerFactory(@Reference OAuthFactory oAuthFactory, @Reference HttpClientFactory httpClientFactory,
            final @Reference TranslationProvider i18nProvider, final @Reference TimeZoneProvider timeZoneProvider) {
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.i18nProvider = i18nProvider;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_SIGNALS.equals(thingTypeUID)) {
            return new EcowattHandler(thing, oAuthFactory, httpClient, i18nProvider, timeZoneProvider);
        }

        return null;
    }
}
