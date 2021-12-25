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
package org.openhab.binding.mybmw.internal;

import static org.openhab.binding.mybmw.internal.MyBMWConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mybmw.internal.handler.MyBMWBridgeHandler;
import org.openhab.binding.mybmw.internal.handler.MyBMWOptionProvider;
import org.openhab.binding.mybmw.internal.handler.VehicleHandler;
import org.openhab.binding.mybmw.internal.utils.Converter;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
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

/**
 * The {@link MyBMWHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.mybmw", service = ThingHandlerFactory.class)
public class MyBMWHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClientFactory httpClientFactory;
    private final MyBMWOptionProvider optionProvider;
    private String localeLanguage;

    @Activate
    public MyBMWHandlerFactory(final @Reference HttpClientFactory hcf, final @Reference MyBMWOptionProvider op,
            final @Reference LocaleProvider lp, final @Reference TimeZoneProvider timeZoneProvider) {
        httpClientFactory = hcf;
        optionProvider = op;
        localeLanguage = lp.getLocale().getLanguage().toUpperCase();
        Converter.setTimeZoneProvider(timeZoneProvider);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_SET.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_CONNECTED_DRIVE_ACCOUNT.equals(thingTypeUID)) {
            return new MyBMWBridgeHandler((Bridge) thing, httpClientFactory, localeLanguage);
        } else if (SUPPORTED_THING_SET.contains(thingTypeUID)) {
            VehicleHandler vh = new VehicleHandler(thing, optionProvider, thingTypeUID.getId(), localeLanguage);
            return vh;
        }
        return null;
    }
}
