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
package org.openhab.binding.pegelonline.internal;

import static org.openhab.binding.pegelonline.internal.PegelOnlineBindingConstants.STATION_THING;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pegelonline.internal.handler.PegelOnlineHandler;
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
 * The {@link PegelOnlineHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.pegelonline", service = ThingHandlerFactory.class)
public class PegelOnlineHandlerFactory extends BaseThingHandlerFactory {
    private final HttpClientFactory httpClientFactory;

    @Activate
    public PegelOnlineHandlerFactory(final @Reference HttpClientFactory hcf, final @Reference LocationProvider lp) {
        httpClientFactory = hcf;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return PegelOnlineBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (STATION_THING.equals(thingTypeUID)) {
            return new PegelOnlineHandler(thing, httpClientFactory.getCommonHttpClient());
        }
        return null;
    }
}
