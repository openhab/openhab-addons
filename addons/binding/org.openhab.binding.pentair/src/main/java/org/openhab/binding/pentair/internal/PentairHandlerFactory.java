/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pentair.internal;

import static org.openhab.binding.pentair.internal.PentairBindingConstants.*;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.pentair.internal.handler.PentairEasyTouchHandler;
import org.openhab.binding.pentair.internal.handler.PentairIPBridgeHandler;
import org.openhab.binding.pentair.internal.handler.PentairIntelliChlorHandler;
import org.openhab.binding.pentair.internal.handler.PentairIntelliFloHandler;
import org.openhab.binding.pentair.internal.handler.PentairSerialBridgeHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link PentairHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jeff James - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.pentair")
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
