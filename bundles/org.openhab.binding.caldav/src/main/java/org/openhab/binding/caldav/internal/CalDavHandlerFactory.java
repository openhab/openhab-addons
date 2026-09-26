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
package org.openhab.binding.caldav.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.caldav.internal.handler.AccountHandler;
import org.openhab.binding.caldav.internal.handler.CalendarHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
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
 * Creates handlers with core-managed networking, time-zone and persistence services.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Lifecycle-managed service dependencies
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class)
public class CalDavHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(
            new ThingTypeUID(CalDavBindingConstants.BINDING_ID, CalDavBindingConstants.ACCOUNT_THING_TYPE),
            new ThingTypeUID(CalDavBindingConstants.BINDING_ID, CalDavBindingConstants.CALENDAR_THING_TYPE));

    private final HttpClientFactory httpFactory;
    private final TimeZoneProvider timeZoneProvider;
    private final Storage<String> storage;

    @Activate
    public CalDavHandlerFactory(@Reference HttpClientFactory httpFactory, @Reference TimeZoneProvider timeZoneProvider,
            @Reference StorageService storageService) {
        this.httpFactory = httpFactory;
        this.timeZoneProvider = timeZoneProvider;
        this.storage = storageService.getStorage("caldav-calendar-cache");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (!supportsThingType(thing.getThingTypeUID())) {
            return null;
        }
        if (thing instanceof Bridge bridge) {
            return new AccountHandler(bridge, httpFactory);
        }
        return new CalendarHandler(thing, timeZoneProvider, storage);
    }
}
