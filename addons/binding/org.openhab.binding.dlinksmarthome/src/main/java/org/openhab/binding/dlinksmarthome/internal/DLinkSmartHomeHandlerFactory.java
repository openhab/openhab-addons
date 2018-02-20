/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dlinksmarthome.internal;

import static org.openhab.binding.dlinksmarthome.DLinkSmartHomeBindingConstants.*;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.dlinksmarthome.handler.DLinkMotionSensorHandler;

/**
 * The {@link DLinkSmartHomeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Mike Major - Initial contribution
 */
public class DLinkSmartHomeHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(final Thing thing) {

        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_DCHS150)) {
            return new DLinkMotionSensorHandler(thing);
        }

        return null;
    }
}
