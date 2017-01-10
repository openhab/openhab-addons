/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homepilot.handler;

import static org.openhab.binding.homepilot.HomePilotBindingConstants.BINDING_ID;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

/**
 * The {@link HomePilotHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author stundzig - Initial contribution
 */
public class HomePilotHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return BINDING_ID.equals(thingTypeUID.getBindingId());
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (thing instanceof Bridge) {
            return new HomePilotBridgeHandler((Bridge) thing);
        } else {
            return new HomePilotThingHandler(thing);
        }
    }
}
