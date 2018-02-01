/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.eightdevices.internal;

import static org.openhab.binding.eightdevices.EightDevicesBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.eightdevices.handler.EightDevicesHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link EightDevicesHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Nedas - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.eightdevices")
@NonNullByDefault
public class EightDevicesHandlerFactory extends BaseThingHandlerFactory {
    // private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.add(THING_TYPE_3700);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(THING_TYPE_3700, THING_TYPE_3800, THING_TYPE_4400, THING_TYPE_4500));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(THING_TYPE_3700)) {
            return new EightDevicesHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_3800)) {
            return new EightDevicesHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_4400)) {
            return new EightDevicesHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_4500)) {
            return new EightDevicesHandler(thing);
        }

        return null;
    }
}
