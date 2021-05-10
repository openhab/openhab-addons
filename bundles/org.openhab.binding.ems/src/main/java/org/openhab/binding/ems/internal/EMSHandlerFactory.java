/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.ems.internal;

import static org.openhab.binding.ems.internal.EMSBindingConstants.THING_TYPE_EMS;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ems.internal.handler.EMSHandler;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EMSHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.ems", service = ThingHandlerFactory.class)
public class EMSHandlerFactory extends BaseThingHandlerFactory {

    LocationProvider locationProvider;

    public EMSHandlerFactory(final @Reference LocationProvider lp, final @Reference TimeZoneProvider timeZoneProvider) {
        locationProvider = lp;
    }

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_EMS);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_EMS.equals(thingTypeUID)) {
            return new EMSHandler(thing, locationProvider.getLocation());
        }

        return null;
    }
}
