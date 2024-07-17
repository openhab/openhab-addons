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
package org.openhab.binding.bluetooth.airthings.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link AirthingsHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Kai Kreuzer - Added Airthings Wave Mini support
 * @author Davy Wong - Added Airthings Wave Gen 1 support
 * @author Arne Seime - Added Airthings Wave Radon / Wave 2 support
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.airthings")
public class AirthingsHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return AirthingsBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(AirthingsBindingConstants.THING_TYPE_AIRTHINGS_WAVE_PLUS)) {
            return new AirthingsWavePlusHandler(thing);
        }
        if (thingTypeUID.equals(AirthingsBindingConstants.THING_TYPE_AIRTHINGS_WAVE_MINI)) {
            return new AirthingsWaveMiniHandler(thing);
        }
        if (thingTypeUID.equals(AirthingsBindingConstants.THING_TYPE_AIRTHINGS_WAVE_GEN1)) {
            return new AirthingsWaveGen1Handler(thing);
        }
        if (thingTypeUID.equals(AirthingsBindingConstants.THING_TYPE_AIRTHINGS_WAVE_RADON)) {
            return new AirthingsWaveRadonHandler(thing);
        }
        return null;
    }
}
