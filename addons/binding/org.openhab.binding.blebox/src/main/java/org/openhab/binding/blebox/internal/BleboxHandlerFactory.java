/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
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
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.blebox.BleboxBindingConstants;
import org.openhab.binding.blebox.handler.DimmerHandler;
import org.openhab.binding.blebox.handler.GateBoxHandler;
import org.openhab.binding.blebox.handler.LightBoxHandler;
import org.openhab.binding.blebox.handler.LightBoxSHandler;
import org.openhab.binding.blebox.handler.SwitchBoxDHandler;
import org.openhab.binding.blebox.handler.SwitchBoxHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link BleboxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Szymon Tokarski - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true)
public class BleboxHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        switch (thingTypeUID.getId()) {
            case BleboxBindingConstants.DIMMERBOX:
                return new DimmerHandler(thing);
            case BleboxBindingConstants.SWITCHBOX:
                return new SwitchBoxHandler(thing);
            case BleboxBindingConstants.SWITCHBOXD:
                return new SwitchBoxDHandler(thing);
            case BleboxBindingConstants.WLIGHTBOX:
                return new LightBoxHandler(thing);
            case BleboxBindingConstants.WLIGHTBOXS:
                return new LightBoxSHandler(thing);
            case BleboxBindingConstants.GATEBOX:
                return new GateBoxHandler(thing);
        }
        return null;
    }
}
