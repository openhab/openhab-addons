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
package org.openhab.binding.meteoalerte.internal;

import static org.openhab.binding.meteoalerte.internal.MeteoAlerteBindingConstants.*;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meteoalerte.internal.db.DepartmentDbService;
import org.openhab.binding.meteoalerte.internal.handler.MeteoAlerteBridgeHandler;
import org.openhab.binding.meteoalerte.internal.handler.MeteoAlerteHandler;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
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
 * The {@link MeteoAlerteHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(service = ThingHandlerFactory.class)
@NonNullByDefault
public class MeteoAlerteHandlerFactory extends BaseThingHandlerFactory {
    private final Gson gson;
    private final MeteoAlertIconProvider iconProvider;
    private final LocationProvider locationProvider;
    private final DepartmentDbService dbService;

    @Activate
    public MeteoAlerteHandlerFactory(@Reference TimeZoneProvider timeZoneProvider,
            @Reference LocationProvider locationProvider, @Reference DepartmentDbService dbService,
            @Reference MeteoAlertIconProvider iconProvider) {
        this.iconProvider = iconProvider;
        this.locationProvider = locationProvider;
        this.dbService = dbService;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(ZonedDateTime.class,
                        (JsonDeserializer<ZonedDateTime>) (json, type, context) -> ZonedDateTime
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

        return BRIDGE_TYPE_API.equals(thingTypeUID)
                ? new MeteoAlerteBridgeHandler((Bridge) thing, gson, locationProvider, dbService)
                : THING_TYPE_DEPARTEMENT.equals(thingTypeUID) ? new MeteoAlerteHandler(thing, iconProvider) : null;
    }
}
