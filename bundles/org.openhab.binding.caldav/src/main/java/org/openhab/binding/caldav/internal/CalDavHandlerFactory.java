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
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Creates handlers with core-managed networking, time-zone and persistence services.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Lifecycle-managed service dependencies
 * @author Andreas Vilippus - Calendar color channel creation
 * @author Andreas Vilippus - Registry access for handlerless calendar representation updates
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
    private final ThingRegistry thingRegistry;

    /**
     * Creates handlers using Core networking, time-zone, storage and Thing registry services.
     *
     * @param httpFactory Core factory for account HTTP clients
     * @param timeZoneProvider provider of the configured openHAB time zone
     * @param storageService Core service supplying persistent calendar cache storage
     * @param thingRegistry registry of configured calendar Things
     */
    @Activate
    public CalDavHandlerFactory(@Reference HttpClientFactory httpFactory, @Reference TimeZoneProvider timeZoneProvider,
            @Reference StorageService storageService, @Reference ThingRegistry thingRegistry) {
        this.httpFactory = httpFactory;
        this.timeZoneProvider = timeZoneProvider;
        this.storage = storageService.getStorage("caldav-calendar-cache");
        this.thingRegistry = thingRegistry;
    }

    /**
     * Checks whether this factory supports the requested CalDAV Thing type.
     *
     * @param thingTypeUID requested Thing type
     * @return whether the requested Thing type is supported
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    /**
     * Creates a CalDAV Thing and supplies the ungrouped calendar color channel when needed.
     *
     * @param thingTypeUID requested Thing type
     * @param configuration captured configuration used by the handler
     * @param thingUID requested Thing UID, or null for Core-generated identification
     * @param bridgeUID owning account bridge UID, if present
     * @return the created Thing, or null if the type cannot be created
     */
    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        Thing created = super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        if (created != null && supportsThingType(thingTypeUID)
                && CalDavBindingConstants.CALENDAR_THING_TYPE.equals(thingTypeUID.getId())
                && created.getChannel("calendar-color") == null) {
            // The XML schema allows either channels or channel groups. Preserve the existing groups and add this
            // ungrouped channel when creating a calendar; update instructions cover previously stored Things.
            return ThingBuilder.create(created)
                    .withChannel(ChannelBuilder.create(new ChannelUID(created.getUID(), "calendar-color"), "String")
                            .withType(new ChannelTypeUID(CalDavBindingConstants.BINDING_ID, "calendar-color")).build())
                    .build();
        }
        return created;
    }

    /**
     * Creates an account or calendar handler with the required Core services.
     *
     * @param thing calendar or account Thing to handle
     * @return the appropriate handler, or null for an unsupported Thing type
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (!supportsThingType(thing.getThingTypeUID())) {
            return null;
        }
        if (thing instanceof Bridge bridge) {
            return new AccountHandler(bridge, httpFactory, thingRegistry, timeZoneProvider);
        }
        return new CalendarHandler(thing, timeZoneProvider, storage);
    }
}
