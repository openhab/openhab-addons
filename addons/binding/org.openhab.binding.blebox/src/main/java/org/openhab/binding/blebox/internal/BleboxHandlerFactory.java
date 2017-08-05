/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.internal;

import static org.openhab.binding.blebox.BleboxBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.blebox.BleboxBindingConstants;
import org.openhab.binding.blebox.handler.DimmerHandler;
import org.openhab.binding.blebox.handler.GateBoxHandler;
import org.openhab.binding.blebox.handler.LightBoxHandler;
import org.openhab.binding.blebox.handler.LightBoxSHandler;
import org.openhab.binding.blebox.handler.SwitchBoxDHandler;
import org.openhab.binding.blebox.handler.SwitchBoxHandler;

/**
 * The {@link BleboxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class BleboxHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(BleboxBindingConstants.DIMMERBOX_THING_TYPE)) {
            return new DimmerHandler(thing);
        } else if (thingTypeUID.equals(BleboxBindingConstants.SWITCHBOX_THING_TYPE)) {
            return new SwitchBoxHandler(thing);
        } else if (thingTypeUID.equals(BleboxBindingConstants.SWITCHBOXD_THING_TYPE)) {
            return new SwitchBoxDHandler(thing);
        } else if (thingTypeUID.equals(BleboxBindingConstants.WLIGHTBOX_THING_TYPE)) {
            return new LightBoxHandler(thing);
        } else if (thingTypeUID.equals(BleboxBindingConstants.WLIGHTBOXS_THING_TYPE)) {
            return new LightBoxSHandler(thing);
        } else if (thingTypeUID.equals(BleboxBindingConstants.GATEBOX_THING_TYPE)) {
            return new GateBoxHandler(thing);
        }

        return null;
    }
}
