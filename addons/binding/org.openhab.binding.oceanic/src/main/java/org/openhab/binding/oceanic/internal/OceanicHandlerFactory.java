/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.oceanic.internal;

import static org.openhab.binding.oceanic.OceanicBindingConstants.*;

import java.util.Collection;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.oceanic.handler.NetworkOceanicThingHandler;
import org.openhab.binding.oceanic.handler.SerialOceanicThingHandler;
import org.osgi.service.component.annotations.Component;

import com.google.common.collect.Lists;

/**
 * The {@link OceanicHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Karel Goderis - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.oceanic")
public class OceanicHandlerFactory extends BaseThingHandlerFactory {

    private static final Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(THING_TYPE_SERIAL,
            THING_TYPE_NETWORK);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_SERIAL)) {
            return new SerialOceanicThingHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_NETWORK)) {
            return new NetworkOceanicThingHandler(thing);
        }

        return null;
    }
}
