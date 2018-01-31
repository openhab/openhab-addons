/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.internal;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

import static org.openhab.binding.osramlightify.LightifyBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.THING_TYPE_LIGHTIFY_GROUP;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.THING_TYPE_LIGHTIFY_MOTION_SENSOR;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;
import org.openhab.binding.osramlightify.handler.LightifyGroupHandler;
import org.openhab.binding.osramlightify.handler.LightifyMotionSensorHandler;

/**
 * The {@link org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory} implementation
 * to create new instances of the {@link LightifyBridgeHandler} or {@link LightifyDeviceHandler} based on
 * the requested {@link Thing} type.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifyHandlerFactory extends BaseThingHandlerFactory {

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new LightifyBridgeHandler((Bridge) thing);
        }
        if (SUPPORTED_DEVICE_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new LightifyDeviceHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_LIGHTIFY_GROUP)) {
            return new LightifyGroupHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_LIGHTIFY_MOTION_SENSOR)) {
            return new LightifyMotionSensorHandler(thing);
        }
        return null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID)
            || SUPPORTED_DEVICE_THING_TYPES_UIDS.contains(thingTypeUID)
            || thingTypeUID.equals(THING_TYPE_LIGHTIFY_GROUP)
            || thingTypeUID.equals(THING_TYPE_LIGHTIFY_MOTION_SENSOR);
    }
}
