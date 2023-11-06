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
package org.openhab.binding.deutschebahn.internal;

import static org.openhab.binding.deutschebahn.internal.DeutscheBahnBindingConstants.TIMETABLE_TYPE;
import static org.openhab.binding.deutschebahn.internal.DeutscheBahnBindingConstants.TRAIN_TYPE;

import java.util.Date;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deutschebahn.internal.timetable.TimetablesV1Impl;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link DeutscheBahnHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.deutschebahn", service = ThingHandlerFactory.class)
public class DeutscheBahnHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(TIMETABLE_TYPE, TRAIN_TYPE);

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (TIMETABLE_TYPE.equals(thingTypeUID)) {
            return new DeutscheBahnTimetableHandler((Bridge) thing, TimetablesV1Impl::new, Date::new, null);
        } else if (TRAIN_TYPE.equals(thingTypeUID)) {
            return new DeutscheBahnTrainHandler(thing);
        }

        return null;
    }
}
