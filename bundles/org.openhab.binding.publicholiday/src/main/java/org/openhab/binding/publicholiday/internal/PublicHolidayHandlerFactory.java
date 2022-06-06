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
package org.openhab.binding.publicholiday.internal;

import static org.openhab.binding.publicholiday.internal.PublicHolidayBindingConstants.THING_TYPE;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * The {@link PublicHolidayHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Martin Güthle - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.publicholiday", service = ThingHandlerFactory.class)
public class PublicHolidayHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE);
    private final CronScheduler scheduler;

    @Activate
    public PublicHolidayHandlerFactory(final @Reference CronScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE.equals(thingTypeUID)) {
            return new PublicHolidayHandler(thing, scheduler);
        }

        return null;
    }
}
