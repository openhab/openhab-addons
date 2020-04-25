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
package org.openhab.binding.nibeheatpump.internal;

import static org.openhab.binding.nibeheatpump.internal.NibeHeatPumpBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.nibeheatpump.internal.handler.NibeHeatPumpHandler;
import org.openhab.binding.nibeheatpump.internal.models.PumpModel;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link NibeHeatPumpHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Pauli Anttila - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.nibeheatpump")
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
