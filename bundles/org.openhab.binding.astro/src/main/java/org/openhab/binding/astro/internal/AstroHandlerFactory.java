/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.scheduler.CronScheduler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.handler.MoonHandler;
import org.openhab.binding.astro.internal.handler.SunHandler;
import org.openhab.binding.astro.internal.util.PropertyUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link AstroHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@Component(configurationPid = "binding.astro", service = ThingHandlerFactory.class)
public class AstroHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream
            .concat(SunHandler.SUPPORTED_THING_TYPES.stream(), MoonHandler.SUPPORTED_THING_TYPES.stream())
            .collect(Collectors.toSet());
    private static final Map<String, AstroThingHandler> ASTRO_THING_HANDLERS = new HashMap<>();
    private CronScheduler scheduler;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        AstroThingHandler thingHandler = null;
        if (thingTypeUID.equals(THING_TYPE_SUN)) {
            thingHandler = new SunHandler(thing, scheduler);
        } else if (thingTypeUID.equals(THING_TYPE_MOON)) {
            thingHandler = new MoonHandler(thing, scheduler);
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

    @Reference
    protected void setTimeZoneProvider(TimeZoneProvider timeZone) {
        PropertyUtils.setTimeZone(timeZone);
    }

    protected void unsetTimeZoneProvider(TimeZoneProvider timeZone) {
        PropertyUtils.unsetTimeZone();
    }

    public static AstroThingHandler getHandler(String thingUid) {
        return ASTRO_THING_HANDLERS.get(thingUid);
    }

    @Reference
    protected void setCronScheduler(CronScheduler scheduler) {
        this.scheduler = scheduler;
    }

    protected void unsetCronScheduler(CronScheduler scheduler) {
        this.scheduler = null;
    }
}
