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
package org.openhab.binding.airquality.internal;

import static org.openhab.binding.airquality.internal.AirQualityBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airquality.internal.handler.AirQualityBridgeHandler;
import org.openhab.binding.airquality.internal.handler.AirQualityStationHandler;
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
 * The {@link AirQualityHandlerFactory} is responsible for creating thing and thing
 * handler.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.airquality")
@NonNullByDefault
public class AirQualityHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(BRIDGE_TYPE_API, THING_TYPE_STATION);

    private final TimeZoneProvider timeZoneProvider;
    private final LocationProvider locationProvider;

    @Activate
    public AirQualityHandlerFactory(final @Reference TimeZoneProvider timeZoneProvider,
            final @Reference LocationProvider locationProvider) {
        this.timeZoneProvider = timeZoneProvider;
        this.locationProvider = locationProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        return THING_TYPE_STATION.equals(thingTypeUID)
                ? new AirQualityStationHandler(thing, timeZoneProvider, locationProvider)
                : BRIDGE_TYPE_API.equals(thingTypeUID) ? new AirQualityBridgeHandler((Bridge) thing, locationProvider)
                        : null;
    }
}
