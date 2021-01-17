/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.awattar.internal.handler;

import static org.openhab.binding.awattar.internal.aWATTarBindingConstants.*;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link aWATTarHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Wolfgang Klimt - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.awattar", service = ThingHandlerFactory.class)
public class aWATTarHandlerFactory extends BaseThingHandlerFactory {
    private Logger logger = LoggerFactory.getLogger(aWATTarHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_PRICE, THING_TYPE_BESTPRICE,
            THING_TYPE_BRIDGE);
    private final HttpClient httpClient;
    private final LocaleProvider localeProvider;
    private final TranslationProvider i18nProvider;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public aWATTarHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference LocaleProvider localeProvider, final @Reference TranslationProvider i18nProvider,
            final @Reference TimeZoneProvider timeZoneProvider) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.localeProvider = localeProvider;
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
        logger.trace("Creating Handler for Thing {}, UID {}", thing, thingTypeUID);

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new aWATTarBridgeHandler((Bridge) thing, httpClient);
        }
        if (THING_TYPE_PRICE.equals(thingTypeUID)) {
            return new aWATTarPriceHandler(thing);
        }
        if (THING_TYPE_BESTPRICE.equals(thingTypeUID)) {
            return new aWATTarBestpriceHandler(thing, timeZoneProvider);
        }

        logger.warn("Unknown thing type {}, not creating handler!", thingTypeUID);
        return null;
    }
}
