/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeheatpump.internal;

import static org.openhab.binding.nibeheatpump.NibeHeatPumpBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.nibeheatpump.handler.NibeHeatPumpHandler;
import org.openhab.binding.nibeheatpump.internal.models.PumpModel;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link NibeHeatPumpHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Pauli Anttila - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.nibeheatpump")
public class NibeHeatPumpHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new NibeHeatPumpHandler(thing, parsePumpModel(thing));
        }

        return null;
    }

    private PumpModel parsePumpModel(Thing thing) {
        String[] pumpModelStrings = thing.getThingTypeUID().getId().split("-");
        return PumpModel.getPumpModel(pumpModelStrings[0]);
    }
}
