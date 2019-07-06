/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.daikinairbase.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.daikinairbase.internal.handler.DaikinAirbaseAcUnitHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link DaikinAirbaseHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Tim Waterhouse <tim@timwaterhouse.com> - Initial contribution
 * @author Paul Smedley <paul@smedley.id.au> - mods for Daikin Airbase
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.daikinairbase")
@NonNullByDefault
public class DaikinAirbaseHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return DaikinAirbaseBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(DaikinAirbaseBindingConstants.THING_TYPE_AC_UNIT)) {
            return new DaikinAirbaseAcUnitHandler(thing);
        }

        return null;
    }
}
