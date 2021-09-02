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
package org.openhab.binding.airquality.internal;

import static org.openhab.binding.airquality.internal.AirQualityBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airquality.internal.api.ApiBridge;
import org.openhab.binding.airquality.internal.config.AirQualityBindingConfiguration;
import org.openhab.binding.airquality.internal.handler.AirQualityHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirQualityHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Kuba Wolanin - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.airquality")
@NonNullByDefault
public class AirQualityHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(AirQualityHandlerFactory.class);
    private final TimeZoneProvider timeZoneProvider;
    private final LocationProvider locationProvider;
    private final AirQualityBindingConfiguration configuration = new AirQualityBindingConfiguration();
    private final ApiBridge apiBridge = new ApiBridge(configuration);

    @SuppressWarnings("unchecked")
    @Activate
    public AirQualityHandlerFactory(final @Reference TimeZoneProvider timeZoneProvider,
            final @Reference LocationProvider locationProvider, final ComponentContext componentContext) {
        this.timeZoneProvider = timeZoneProvider;
        this.locationProvider = locationProvider;
        modified((Map<@Nullable String, @Nullable Object>) componentContext.getProperties());
    }

    @Modified
    public void modified(@Nullable Map<@Nullable String, @Nullable Object> config) {
        try {
            configuration.update(config != null ? new Configuration(config).as(AirQualityBindingConfiguration.class)
                    : configuration);
            logger.debug("Updated binding configuration to {}", configuration);
        } catch (AirQualityException e) {
            logger.warn("Error in configuration : {}", e.getMessage());
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        return THING_TYPE_AQI.equals(thingTypeUID)
                ? new AirQualityHandler(thing, apiBridge, timeZoneProvider, locationProvider)
                : null;
    }
}
