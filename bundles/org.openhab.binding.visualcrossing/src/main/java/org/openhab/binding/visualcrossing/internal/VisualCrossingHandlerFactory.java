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
package org.openhab.binding.visualcrossing.internal;

import static org.openhab.binding.visualcrossing.internal.VisualCrossingBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link VisualCrossingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.visualcrossing", service = ThingHandlerFactory.class)
public class VisualCrossingHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(WEATHER_THING_TYPE);
    private final HttpClientFactory httpClientFactory;
    private final LocaleProvider localeProvider;
    private final LocationProvider locationProvider;

    @Activate
    public VisualCrossingHandlerFactory(@Reference final HttpClientFactory httpClientFactory,
            @Reference final LocaleProvider localeProvider, @Reference final LocationProvider locationProvider) {
        this.httpClientFactory = httpClientFactory;
        this.localeProvider = localeProvider;
        this.locationProvider = locationProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (WEATHER_THING_TYPE.equals(thingTypeUID)) {
            return new VisualCrossingHandler(thing, httpClientFactory, localeProvider, locationProvider);
        }

        return null;
    }
}
