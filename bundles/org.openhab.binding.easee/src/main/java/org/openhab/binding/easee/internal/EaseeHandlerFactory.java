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
package org.openhab.binding.easee.internal;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.easee.internal.handler.EaseeChargerHandler;
import org.openhab.binding.easee.internal.handler.EaseeMasterChargerHandler;
import org.openhab.binding.easee.internal.handler.EaseeSiteHandler;
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
 * The {@link EaseeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Alexander Friese - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.easee", service = ThingHandlerFactory.class)
public class EaseeHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(EaseeHandlerFactory.class);

    /**
     * the shared http client
     */
    private final HttpClient httpClient;
    private final LocaleProvider localeProvider;
    private final TranslationProvider i18nProvider;

    @Activate
    public EaseeHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference LocaleProvider localeProvider, final @Reference TranslationProvider i18nProvider) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.localeProvider = localeProvider;
        this.i18nProvider = i18nProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_SITE.equals(thingTypeUID)) {
            return new EaseeSiteHandler((Bridge) thing, httpClient, localeProvider, i18nProvider);
        } else if (THING_TYPE_MASTER_CHARGER.equals(thingTypeUID)) {
            return new EaseeMasterChargerHandler(thing, localeProvider, i18nProvider);
        } else if (THING_TYPE_CHARGER.equals(thingTypeUID)) {
            return new EaseeChargerHandler(thing, localeProvider, i18nProvider);
        } else {
            logger.warn("Unsupported Thing-Type: {}", thingTypeUID.getAsString());
        }

        return null;
    }
}
