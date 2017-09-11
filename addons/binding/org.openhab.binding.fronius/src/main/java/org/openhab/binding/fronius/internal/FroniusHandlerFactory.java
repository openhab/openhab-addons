/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.internal;

import static org.openhab.binding.fronius.FroniusBindingConstants.*;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.fronius.handler.FroniusSymo;
import org.openhab.binding.fronius.handler.FroniusSymoHybrid;

/**
 * The {@link FroniusHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gerrit Beine - Initial contribution
 */
public class FroniusHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(FRONIUS_SYMO)) {
            return new FroniusSymo(thing);
        } else if (thingTypeUID.equals(FRONIUS_SYMO_HYBRID)) {
            return new FroniusSymoHybrid(thing);
        }

        return null;
    }
}
