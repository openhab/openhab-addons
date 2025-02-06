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
package org.openhab.binding.mybmw.internal;

import static org.openhab.binding.mybmw.internal.MyBMWConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mybmw.internal.handler.MyBMWBridgeHandler;
import org.openhab.binding.mybmw.internal.handler.MyBMWCommandOptionProvider;
import org.openhab.binding.mybmw.internal.handler.VehicleHandler;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;

/**
 * The {@link MyBMWHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - changed localeProvider handling
 */
@NonNullByDefault
@Component(configurationPid = "binding.mybmw", service = ThingHandlerFactory.class)
public class MyBMWHandlerFactory extends BaseThingHandlerFactory {
    private final HttpClientFactory httpClientFactory;
    private final OAuthFactory oAuthFactory;
    private final MyBMWCommandOptionProvider commandOptionProvider;
    private final NetworkAddressService networkAddressService;
    private final HttpService httpService;
    private final LocationProvider locationProvider;
    private final TimeZoneProvider timeZoneProvider;
    private final LocaleProvider localeProvider;

    @Activate
    public MyBMWHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory,
            final @Reference MyBMWCommandOptionProvider commandOptionProvider,
            final @Reference NetworkAddressService networkAddressService, final @Reference HttpService httpService,
            final @Reference LocaleProvider localeProvider, final @Reference LocationProvider locationProvider,
            final @Reference TimeZoneProvider timeZoneProvider) {
        this.httpClientFactory = httpClientFactory;
        this.oAuthFactory = oAuthFactory;
        this.commandOptionProvider = commandOptionProvider;
        this.networkAddressService = networkAddressService;
        this.httpService = httpService;
        this.locationProvider = locationProvider;
        this.timeZoneProvider = timeZoneProvider;
        this.localeProvider = localeProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_SET.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_CONNECTED_DRIVE_ACCOUNT.equals(thingTypeUID)) {
            return new MyBMWBridgeHandler((Bridge) thing, httpClientFactory, oAuthFactory, httpService,
                    networkAddressService, localeProvider);
        } else if (SUPPORTED_THING_SET.contains(thingTypeUID)) {
            return new VehicleHandler(thing, commandOptionProvider, locationProvider, timeZoneProvider,
                    thingTypeUID.getId());
        }
        return null;
    }
}
