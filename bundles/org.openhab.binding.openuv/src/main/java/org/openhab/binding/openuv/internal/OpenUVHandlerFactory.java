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
package org.openhab.binding.openuv.internal;

import static org.openhab.binding.openuv.internal.OpenUVBindingConstants.*;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openuv.internal.handler.OpenUVBridgeHandler;
import org.openhab.binding.openuv.internal.handler.OpenUVReportHandler;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

/**
 * The {@link OpenUVHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.openuv")
public class OpenUVHandlerFactory extends BaseThingHandlerFactory {

    private final LocationProvider locationProvider;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;
    private final Gson gson;

    @Activate
    public OpenUVHandlerFactory(@Reference TimeZoneProvider timeZoneProvider,
            @Reference LocationProvider locationProvider, @Reference TranslationProvider i18nProvider,
            @Reference LocaleProvider localeProvider) {
        this.locationProvider = locationProvider;
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(ZonedDateTime.class,
                        (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime
                                .parse(json.getAsJsonPrimitive().getAsString())
                                .withZoneSameInstant(timeZoneProvider.getTimeZone()))
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        return APIBRIDGE_THING_TYPE.equals(thingTypeUID)
                ? new OpenUVBridgeHandler((Bridge) thing, locationProvider, i18nProvider, localeProvider, gson)
                : LOCATION_REPORT_THING_TYPE.equals(thingTypeUID) ? new OpenUVReportHandler(thing) : null;
    }
}
