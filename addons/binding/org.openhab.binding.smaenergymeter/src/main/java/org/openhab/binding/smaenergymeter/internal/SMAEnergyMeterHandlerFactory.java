/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smaenergymeter.internal;

import static org.openhab.binding.smaenergymeter.SMAEnergyMeterBindingConstants.*;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.smaenergymeter.handler.SMAEnergyMeterHandler;

/**
 * The {@link SMAEnergyMeterHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Osman Basha - Initial contribution
 */
public class SMAEnergyMeterHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ENERGY_METER)) {
            return new SMAEnergyMeterHandler(thing);
        }

        return null;
    }

}
