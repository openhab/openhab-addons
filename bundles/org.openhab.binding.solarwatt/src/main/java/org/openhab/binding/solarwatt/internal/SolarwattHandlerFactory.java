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
package org.openhab.binding.solarwatt.internal;

import static org.openhab.binding.solarwatt.internal.SolarwattBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.solarwatt.internal.channel.SolarwattChannelTypeProvider;
import org.openhab.binding.solarwatt.internal.handler.EnergyManagerHandler;
import org.openhab.binding.solarwatt.internal.handler.LocationHandler;
import org.openhab.binding.solarwatt.internal.handler.SimpleDeviceHandler;
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
 * The {@link SolarwattHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.solarwatt", service = ThingHandlerFactory.class)
public class SolarwattHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ENERGY_MANAGER,
            THING_TYPE_INVERTER, THING_TYPE_POWERMETER, THING_TYPE_EVSTATION, THING_TYPE_BATTERYCONVERTER,
            THING_TYPE_LOCATION, THING_TYPE_PVPLANT, THING_TYPE_GRIDFLOW, THING_TYPE_SMARTHEATER);

    private final HttpClient commonHttpClient;
    private final SolarwattChannelTypeProvider channelTypeProvider;

    @Activate
    public SolarwattHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference SolarwattChannelTypeProvider channelTypeProvider) {
        this.commonHttpClient = httpClientFactory.getCommonHttpClient();
        this.channelTypeProvider = channelTypeProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ENERGY_MANAGER.equals(thingTypeUID)) {
            // energy manager is a separate device as it is the bridge
            return new EnergyManagerHandler((Bridge) thing, this.channelTypeProvider, this.commonHttpClient);
        } else if (THING_TYPE_LOCATION.equals(thingTypeUID)) {
            return new LocationHandler(thing, this.channelTypeProvider);
        } else if (this.supportsThingType(thingTypeUID)) {
            // standard device handling
            return new SimpleDeviceHandler(thing, this.channelTypeProvider);
        }

        return null;
    }
}
