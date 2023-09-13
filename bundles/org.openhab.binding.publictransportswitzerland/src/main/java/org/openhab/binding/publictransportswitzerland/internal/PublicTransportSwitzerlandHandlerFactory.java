/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.publictransportswitzerland.internal;

import static org.openhab.binding.publictransportswitzerland.internal.PublicTransportSwitzerlandBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.publictransportswitzerland.internal.stationboard.PublicTransportSwitzerlandStationboardHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link PublicTransportSwitzerlandHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jeremy Stucki - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.publictransportswitzerland", service = ThingHandlerFactory.class)
public class PublicTransportSwitzerlandHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_STATIONBOARD);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_STATIONBOARD.equals(thingTypeUID)) {
            return new PublicTransportSwitzerlandStationboardHandler(thing);
        }

        return null;
    }
}
