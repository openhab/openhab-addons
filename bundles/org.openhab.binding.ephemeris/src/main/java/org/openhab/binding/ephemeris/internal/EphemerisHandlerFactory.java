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
package org.openhab.binding.ephemeris.internal;

import static org.openhab.binding.ephemeris.internal.EphemerisBindingConstants.*;

import java.io.File;
import java.time.ZoneId;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ephemeris.internal.handler.DaysetHandler;
import org.openhab.binding.ephemeris.internal.handler.DefaultHandler;
import org.openhab.binding.ephemeris.internal.handler.FileHandler;
import org.openhab.binding.ephemeris.internal.handler.WeekendHandler;
import org.openhab.core.ephemeris.EphemerisManager;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EphemerisHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.ephemeris", service = ThingHandlerFactory.class)
public class EphemerisHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_FILE, THING_TYPE_DEFAULT,
            THING_TYPE_DAYSET, THING_TYPE_WEEKEND);

    private final EphemerisManager ephemerisManager;
    private final ZoneId zoneId;

    @Activate
    public EphemerisHandlerFactory(final @Reference EphemerisManager ephemerisManager,
            final @Reference TimeZoneProvider timeZoneProvider) {
        this.ephemerisManager = ephemerisManager;
        this.zoneId = timeZoneProvider.getTimeZone();

        File folder = new File(BINDING_DATA_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_FILE.equals(thingTypeUID)) {
            return new FileHandler(thing, ephemerisManager, zoneId);
        } else if (THING_TYPE_DEFAULT.equals(thingTypeUID)) {
            return new DefaultHandler(thing, ephemerisManager, zoneId);
        } else if (THING_TYPE_DAYSET.equals(thingTypeUID)) {
            return new DaysetHandler(thing, ephemerisManager, zoneId);
        } else if (THING_TYPE_WEEKEND.equals(thingTypeUID)) {
            return new WeekendHandler(thing, ephemerisManager, zoneId);
        }

        return null;
    }
}
