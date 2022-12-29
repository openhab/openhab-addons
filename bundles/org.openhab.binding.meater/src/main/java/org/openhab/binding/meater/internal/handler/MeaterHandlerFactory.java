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
package org.openhab.binding.meater.internal.handler;

import static org.openhab.binding.meater.internal.MeaterBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;

/**
 * The {@link MeaterHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.meater", service = ThingHandlerFactory.class)
public class MeaterHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_MEATER_PROBE,
            THING_TYPE_BRIDGE);

    private final Gson gson;
    private final HttpClient httpClient;
    private final TimeZoneProvider timeZoneProvider;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    @Activate
    public MeaterHandlerFactory(@Reference TimeZoneProvider timeZoneProvider,
            final @Reference TranslationProvider i18nProvider, @Reference LocaleProvider localeProvider,
            @Reference HttpClientFactory httpClientFactory) {
        this.timeZoneProvider = timeZoneProvider;
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.gson = new Gson();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_MEATER_PROBE.equals(thingTypeUID)) {
            return new MeaterHandler(thing, timeZoneProvider);
        } else if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new MeaterBridgeHandler((Bridge) thing, httpClient, gson, i18nProvider, localeProvider);
        }

        return null;
    }
}
