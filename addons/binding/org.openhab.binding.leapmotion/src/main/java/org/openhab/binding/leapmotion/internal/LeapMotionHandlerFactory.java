/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.leapmotion.internal;

import static org.openhab.binding.leapmotion.LeapMotionBindingConstants.THING_TYPE_CONTROLLER;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.leapmotion.handler.LeapMotionHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link LeapMotionHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.leapmotion")
@NonNullByDefault
public class LeapMotionHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_CONTROLLER);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_CONTROLLER)) {
            return new LeapMotionHandler(thing);
        }

        return null;
    }
}
