/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pentair.internal;

import static org.openhab.binding.pentair.PentairBindingConstants.*;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.pentair.handler.PentairEasyTouchHandler;
import org.openhab.binding.pentair.handler.PentairIPBridgeHandler;
import org.openhab.binding.pentair.handler.PentairIntelliChlorHandler;
import org.openhab.binding.pentair.handler.PentairIntelliFloHandler;
import org.openhab.binding.pentair.handler.PentairSerialBridgeHandler;

/**
 * The {@link PentairHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jeff James - Initial contribution
 */

public class PentairHandlerFactory extends BaseThingHandlerFactory {
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(IP_BRIDGE_THING_TYPE)) {
            return new PentairIPBridgeHandler((Bridge) thing);
        } else if (thingTypeUID.equals(SERIAL_BRIDGE_THING_TYPE)) {
            return new PentairSerialBridgeHandler((Bridge) thing);
        } else if (thingTypeUID.equals(EASYTOUCH_THING_TYPE)) {
            return new PentairEasyTouchHandler(thing);
        } else if (thingTypeUID.equals(INTELLIFLO_THING_TYPE)) {
            return new PentairIntelliFloHandler(thing);
        } else if (thingTypeUID.equals(INTELLICHLOR_THING_TYPE)) {
            return new PentairIntelliChlorHandler(thing);
        }

        return null;
    }
}
