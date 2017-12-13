/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal;

import static org.openhab.binding.tplinksmarthome.TPLinkSmartHomeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.tplinksmarthome.handler.SmartHomeHandler;
import org.openhab.binding.tplinksmarthome.internal.device.BulbDevice;
import org.openhab.binding.tplinksmarthome.internal.device.EnergySwitchDevice;
import org.openhab.binding.tplinksmarthome.internal.device.SmartHomeDevice;
import org.openhab.binding.tplinksmarthome.internal.device.SwitchDevice;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link TPLinkSmartHomeHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Christian Fischer - Initial contribution
 * @author Hilbrand Bouwkamp - Specific handlers for different type of devices.
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, immediate = true)
public class TPLinkSmartHomeHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Nullable
    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        SmartHomeDevice device;

        if (THING_TYPE_HS110.equals(thingTypeUID)) {
            device = new EnergySwitchDevice();
        } else if (THING_TYPE_LB130.equals(thingTypeUID)) {
            device = new BulbDevice(thingTypeUID, COLOR_TEMPERATURE_LB130_MIN, COLOR_TEMPERATURE_LB130_MAX);
        } else if (THING_TYPE_LB120.equals(thingTypeUID)) {
            device = new BulbDevice(thingTypeUID, COLOR_TEMPERATURE_LB120_MIN, COLOR_TEMPERATURE_LB120_MAX);
        } else if (SWITCH_THING_TYPES.contains(thingTypeUID)) {
            device = new SwitchDevice();
        } else if (BULB_THING_TYPES.contains(thingTypeUID)) {
            device = new BulbDevice(thingTypeUID);
        } else {
            return null;
        }
        return new SmartHomeHandler(thing, device);
    }
}
