/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.energyforecast.internal;

import static org.openhab.binding.energyforecast.internal.EnergyForecastBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.energyforecast.internal.handler.EnergyForecastHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EnergyForecastHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.energyforecast", service = ThingHandlerFactory.class)
public class EnergyForecastHandlerFactory extends BaseThingHandlerFactory {

    private final StorageService storageService;
    private final HttpClientFactory hcf;
    private final TimeZoneProvider tzp;

    @Activate
    public EnergyForecastHandlerFactory(final @Reference HttpClientFactory hcf,
            final @Reference StorageService storageService, final @Reference TimeZoneProvider tzp) {
        this.hcf = hcf;
        this.storageService = storageService;
        this.tzp = tzp;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ENERGY_FORECAST.equals(thingTypeUID)) {
            return new EnergyForecastHandler(thing, hcf.getCommonHttpClient(), storageService, tzp);
        }

        return null;
    }
}
