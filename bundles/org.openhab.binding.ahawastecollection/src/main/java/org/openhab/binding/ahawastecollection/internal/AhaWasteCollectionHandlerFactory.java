/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.ahawastecollection.internal;

import static org.openhab.binding.ahawastecollection.internal.AhaWasteCollectionBindingConstants.THING_TYPE_SCHEDULE;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link AhaWasteCollectionHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.ahawastecollection", service = ThingHandlerFactory.class)
public class AhaWasteCollectionHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_SCHEDULE);
    private final CronScheduler scheduler;
    private final TimeZoneProvider timeZoneProvider;

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Activate
    public AhaWasteCollectionHandlerFactory(final @Reference CronScheduler scheduler,
            final @Reference TimeZoneProvider timeZoneProvider) {
        this.scheduler = scheduler;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_SCHEDULE.equals(thingTypeUID)) {
            return new AhaWasteCollectionHandler(thing, this.scheduler, this.timeZoneProvider,
                    AhaCollectionScheduleImpl::new, null);
        }
        return null;
    }
}
