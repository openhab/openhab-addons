/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeheatpump.internal;

import static org.openhab.binding.nibeheatpump.NibeHeatPumpBindingConstants.*;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.nibeheatpump.handler.NibeHeatPumpHandler;
import org.openhab.binding.nibeheatpump.internal.models.PumpModel;

/**
 * The {@link NibeHeatPumpHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class NibeHeatPumpHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_F1X45_UDP) || thingTypeUID.equals(THING_TYPE_F1X45_SERIAL)
                || thingTypeUID.equals(THING_TYPE_F1X45_SIMULATOR)) {
            return new NibeHeatPumpHandler(thing, parsePumpModel(thing));
        }

        return null;
    }

    private PumpModel parsePumpModel(Thing thing) {
        String[] pumpModelStrings = thing.getThingTypeUID().getId().toString().split("-");
        return PumpModel.getPumpModel(pumpModelStrings[0]);
    }
}
