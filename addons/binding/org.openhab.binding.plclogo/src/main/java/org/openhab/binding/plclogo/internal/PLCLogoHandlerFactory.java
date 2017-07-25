/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plclogo.internal;

import static org.openhab.binding.plclogo.PLCLogoBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.plclogo.handler.PLCAnalogBlockHandler;
import org.openhab.binding.plclogo.handler.PLCBridgeHandler;
import org.openhab.binding.plclogo.handler.PLCDigitalBlockHandler;

/**
 * The {@link PLCLogoHandlerFactory} is responsible for creating things and
 * thing handlers supported by PLCLogo binding.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
public class PLCLogoHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>();

    /**
     * Constructor.
     */
    public PLCLogoHandlerFactory() {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_DEVICE);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_DIGITAL);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_ANALOG);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_DEVICE.equals(thing.getThingTypeUID()) && (thing instanceof Bridge)) {
            return new PLCBridgeHandler((Bridge) thing);
        } else if (THING_TYPE_DIGITAL.equals(thing.getThingTypeUID())) {
            return new PLCDigitalBlockHandler(thing);
        } else if (THING_TYPE_ANALOG.equals(thing.getThingTypeUID())) {
            return new PLCAnalogBlockHandler(thing);
        }

        return null;
    }

}
