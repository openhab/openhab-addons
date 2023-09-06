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
package org.openhab.binding.fineoffsetweatherstation.internal;

import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.THING_TYPE_GATEWAY;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.THING_TYPE_SENSOR;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.discovery.FineOffsetGatewayDiscoveryService;
import org.openhab.binding.fineoffsetweatherstation.internal.handler.FineOffsetGatewayHandler;
import org.openhab.binding.fineoffsetweatherstation.internal.handler.FineOffsetSensorHandler;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
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
 * The {@link FineOffsetWeatherStationHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.fineoffsetweatherstation", service = ThingHandlerFactory.class)
public class FineOffsetWeatherStationHandlerFactory extends BaseThingHandlerFactory {

    private final FineOffsetGatewayDiscoveryService gatewayDiscoveryService;
    private final ChannelTypeRegistry channelTypeRegistry;
    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public FineOffsetWeatherStationHandlerFactory(@Reference FineOffsetGatewayDiscoveryService gatewayDiscoveryService,
            @Reference ChannelTypeRegistry channelTypeRegistry, @Reference TranslationProvider translationProvider,
            @Reference LocaleProvider localeProvider, @Reference TimeZoneProvider timeZoneProvider) {
        this.gatewayDiscoveryService = gatewayDiscoveryService;
        this.channelTypeRegistry = channelTypeRegistry;
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_GATEWAY.equals(thingTypeUID) && thing instanceof Bridge) {
            return new FineOffsetGatewayHandler((Bridge) thing, gatewayDiscoveryService, channelTypeRegistry,
                    translationProvider, localeProvider, timeZoneProvider);
        }
        if (THING_TYPE_SENSOR.equals(thingTypeUID)) {
            return new FineOffsetSensorHandler(thing);
        }

        return null;
    }
}
