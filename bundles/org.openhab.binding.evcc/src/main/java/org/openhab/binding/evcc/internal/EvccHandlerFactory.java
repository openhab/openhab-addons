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
package org.openhab.binding.evcc.internal;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evcc.internal.handler.EvccBatteryHandler;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.binding.evcc.internal.handler.EvccHeatingHandler;
import org.openhab.binding.evcc.internal.handler.EvccLoadpointHandler;
import org.openhab.binding.evcc.internal.handler.EvccPvHandler;
import org.openhab.binding.evcc.internal.handler.EvccSiteHandler;
import org.openhab.binding.evcc.internal.handler.EvccStatisticsHandler;
import org.openhab.binding.evcc.internal.handler.EvccVehicleHandler;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EvccHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Florian Hotze - Initial contribution
 * @author Marcel Goerentz - Rework the binding
 */
@NonNullByDefault
@Component(configurationPid = "binding.evcc", service = ThingHandlerFactory.class)
public class EvccHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClientFactory httpClientFactory;
    private final ChannelTypeRegistry channelTypeRegistry;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    @Activate
    public EvccHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference ChannelTypeRegistry channelTypeRegistry, @Reference TranslationProvider i18nProvider,
            @Reference LocaleProvider localeProvider) {
        this.httpClientFactory = httpClientFactory;
        this.channelTypeRegistry = channelTypeRegistry;
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID) || THING_TYPE_SERVER.equals(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID type = thing.getThingTypeUID();

        if (THING_TYPE_SERVER.equals(type)) {
            return new EvccBridgeHandler((Bridge) thing, httpClientFactory, i18nProvider, localeProvider);
        }

        if (THING_TYPE_SITE.equals(type)) {
            return new EvccSiteHandler(thing, channelTypeRegistry);
        }

        if (THING_TYPE_VEHICLE.equals(type)) {
            return new EvccVehicleHandler(thing, channelTypeRegistry);
        }

        if (THING_TYPE_LOADPOINT.equals(type)) {
            return new EvccLoadpointHandler(thing, channelTypeRegistry);
        }

        if (THING_TYPE_HEATING.equals(type)) {
            return new EvccHeatingHandler(thing, channelTypeRegistry);
        }

        if (THING_TYPE_BATTERY.equals(type)) {
            return new EvccBatteryHandler(thing, channelTypeRegistry);
        }

        if (THING_TYPE_PV.equals(type)) {
            return new EvccPvHandler(thing, channelTypeRegistry);
        }

        if (THING_TYPE_STATISTICS.equals(type)) {
            return new EvccStatisticsHandler(thing, channelTypeRegistry);
        }
        return null;
    }
}
