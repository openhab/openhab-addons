/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mpower.internal;

import static org.openhab.binding.mpower.MpowerBindingConstants.*;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.mpower.MpowerBindingConstants;
import org.openhab.binding.mpower.handler.MpowerHandler;
import org.openhab.binding.mpower.handler.MpowerSocketHandler;

/**
 * The {@link MpowerHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author magcode - Initial contribution
 */
public class MpowerHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return MpowerBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_MPOWER.equals(thing.getThingTypeUID())) {
            return new MpowerHandler((Bridge) thing);
        } else if (THING_TYPE_SOCKET.equals(thing.getThingTypeUID())) {
            return new MpowerSocketHandler(thing);
        }
        return null;
    }
}