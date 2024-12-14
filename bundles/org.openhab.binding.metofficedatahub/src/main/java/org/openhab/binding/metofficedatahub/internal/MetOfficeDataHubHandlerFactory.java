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
package org.openhab.binding.metofficedatahub.internal;

import static org.openhab.binding.metofficedatahub.internal.MetOfficeDataHubBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MetOfficeDataHubHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.metofficedatahub", service = ThingHandlerFactory.class)
public class MetOfficeDataHubHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE,
            THING_TYPE_SITE_SPEC_API);

    private final LocationProvider locationProvider;
    private final HttpClientFactory httpClientFactory;
    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;
    private final TimeZoneProvider timeZoneProvider;
    private final StorageService storageService;

    @Activate
    public MetOfficeDataHubHandlerFactory(@Reference LocationProvider locationProvider,
            @Reference HttpClientFactory httpClientFactory, @Reference TranslationProvider translationProvider,
            @Reference LocaleProvider localeProvider, @Reference TimeZoneProvider timeZoneProvider,
            @Reference StorageService storageService) {
        this.locationProvider = locationProvider;
        this.httpClientFactory = httpClientFactory;
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.timeZoneProvider = timeZoneProvider;
        this.storageService = storageService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new MetOfficeDataHubBridgeHandler((Bridge) thing, httpClientFactory, translationProvider,
                    localeProvider, storageService, timeZoneProvider);
        } else if (THING_TYPE_SITE_SPEC_API.equals(thingTypeUID)) {
            return new MetOfficeDataHubSiteHandler(thing, locationProvider, translationProvider, localeProvider,
                    timeZoneProvider);
        }

        return null;
    }
}
