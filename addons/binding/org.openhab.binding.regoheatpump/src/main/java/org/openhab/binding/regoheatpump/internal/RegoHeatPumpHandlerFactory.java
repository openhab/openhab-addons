/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.regoheatpump.internal;

import static org.openhab.binding.regoheatpump.RegoHeatPumpBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.regoheatpump.handler.IpHusdataHandler;
import org.openhab.binding.regoheatpump.handler.IpRego6xxHeatPumpHandler;
import org.openhab.binding.regoheatpump.handler.SerialHusdataHandler;
import org.openhab.binding.regoheatpump.handler.SerialRego6xxHeatPumpHandler;

/**
 * The {@link RegoHeatPumpHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Boris Krivonog - Initial contribution
 */
public class RegoHeatPumpHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream
            .of(THING_TYPE_IP_REGO6XX, THING_TYPE_SERIAL_REGO6XX, THING_TYPE_IP_HUSDATA, THING_TYPE_SERIAL_HUSDATA)
            .collect(Collectors.toSet()));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_IP_REGO6XX)) {
            return new IpRego6xxHeatPumpHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_SERIAL_REGO6XX)) {
            return new SerialRego6xxHeatPumpHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_IP_HUSDATA)) {
            return new IpHusdataHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_SERIAL_HUSDATA)) {
            return new SerialHusdataHandler(thing);
        }

        return null;
    }
}
