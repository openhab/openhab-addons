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
package org.openhab.binding.astro.internal;

import static org.openhab.binding.astro.internal.AstroBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.handler.MoonHandler;
import org.openhab.binding.astro.internal.handler.SunHandler;
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
 * The {@link AstroHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.astro", service = ThingHandlerFactory.class)
public class AstroHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_SUN, THING_TYPE_MOON);
    private static final Map<String, AstroThingHandler> ASTRO_THING_HANDLERS = new HashMap<>();
    private final CronScheduler scheduler;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public AstroHandlerFactory(final @Reference CronScheduler scheduler,
            final @Reference TimeZoneProvider timeZoneProvider) {
        this.scheduler = scheduler;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        AstroThingHandler thingHandler = null;
        if (thingTypeUID.equals(THING_TYPE_SUN)) {
            thingHandler = new SunHandler(thing, scheduler, timeZoneProvider);
        } else if (thingTypeUID.equals(THING_TYPE_MOON)) {
            thingHandler = new MoonHandler(thing, scheduler, timeZoneProvider);
        }
        if (thingHandler != null) {
            ASTRO_THING_HANDLERS.put(thing.getUID().toString(), thingHandler);
        }
        return thingHandler;
    }

    @Override
    public void unregisterHandler(Thing thing) {
        super.unregisterHandler(thing);
        ASTRO_THING_HANDLERS.remove(thing.getUID().toString());
    }

    public static @Nullable AstroThingHandler getHandler(String thingUid) {
        return ASTRO_THING_HANDLERS.get(thingUid);
    }
}
