/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.coolmasternet.internal;

import static org.openhab.binding.coolmasternet.CoolMasterNetBindingConstants.*;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.coolmasternet.handler.HVACHandler;

import com.google.common.collect.Sets;

/**
 * The {@link CoolMasterNetHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Angus Gratton
 */
public class CoolMasterNetHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets
            .union(Collections.singleton(THING_TYPE_HVAC), Collections.singleton(THING_TYPE_CONTROLLER));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_CONTROLLER)) {
            return new ControllerHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_TYPE_HVAC)) {
            return new HVACHandler(thing);
        }

        return null;
    }

}
