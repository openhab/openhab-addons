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

import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meteoalerte.internal.db.DepartmentDbService;
import org.openhab.binding.meteoalerte.internal.deserialization.MeteoAlerteDeserializer;
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

/**
 * The {@link MeteoAlerteHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(service = ThingHandlerFactory.class)
@NonNullByDefault
public class MeteoAlerteHandlerFactory extends BaseThingHandlerFactory {
    private final MeteoAlertIconProvider iconProvider;
    private final LocationProvider locationProvider;
    private final DepartmentDbService dbService;
    private final MeteoAlerteDeserializer deserializer;
    private final ZoneId zoneId;

    @Activate
    public MeteoAlerteHandlerFactory(final @Reference TimeZoneProvider timeZoneProvider,
            final @Reference LocationProvider locationProvider, final @Reference DepartmentDbService dbService,
            final @Reference MeteoAlertIconProvider iconProvider,
            final @Reference MeteoAlerteDeserializer deserializer) {
        this.iconProvider = iconProvider;
        this.locationProvider = locationProvider;
        this.deserializer = deserializer;
        this.dbService = dbService;
        this.zoneId = timeZoneProvider.getTimeZone();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        return BRIDGE_TYPE_API.equals(thingTypeUID)
                ? new MeteoAlerteBridgeHandler((Bridge) thing, deserializer, locationProvider, dbService)
                : THING_TYPE_DEPARTEMENT.equals(thingTypeUID) ? new MeteoAlerteHandler(thing, zoneId, iconProvider)
                        : null;
    }
}
