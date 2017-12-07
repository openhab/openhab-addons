/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal;

import static org.openhab.binding.icloud.BindingConstants.*;

import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.icloud.handler.BridgeHandler;
import org.openhab.binding.icloud.handler.DeviceHandler;

/**
 * The {@link BridgeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Patrik Gfeller - Initial contribution
 */
public class BridgeHandlerFactory extends BaseThingHandlerFactory {
    private LocationProvider locationProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    protected void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    protected void unsetLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = null;
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ICLOUD)) {
            return new BridgeHandler((Bridge) thing);
        }

        if (thingTypeUID.equals(THING_TYPE_ICLOUDDEVICE)) {
            return new DeviceHandler(thing, locationProvider);
        }
        return null;
    }
}
