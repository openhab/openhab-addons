/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal;

import static org.openhab.binding.nibeuplink.NibeUplinkBindingConstants.*;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.nibeuplink.handler.F1145Handler;
import org.openhab.binding.nibeuplink.handler.F1155Handler;
import org.openhab.binding.nibeuplink.handler.F730Handler;
import org.openhab.binding.nibeuplink.handler.F750Handler;
import org.openhab.binding.nibeuplink.handler.VVM310Handler;
import org.openhab.binding.nibeuplink.handler.VVM320Handler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NibeUplinkHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Alexander Friese - initial contribution
 *
 */
@Component(service = ThingHandlerFactory.class, immediate = true)
public class NibeUplinkHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(NibeUplinkHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_VVM320)) {
            return new VVM320Handler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_VVM310)) {
            return new VVM310Handler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_F750)) {
            return new F750Handler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_F730)) {
            return new F730Handler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_F1145)) {
            return new F1145Handler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_F1155)) {
            return new F1155Handler(thing);
        } else {
            logger.warn("Unsupported Thing-Type: {}", thingTypeUID.getAsString());
        }

        return null;
    }
}
