/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.internal;

import static org.openhab.binding.fronius.FroniusBindingConstants.*;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.fronius.handler.FroniusActiveDeviceInfoHandler;
import org.openhab.binding.fronius.handler.FroniusInverterRealtimeDataHandler;
import org.openhab.binding.fronius.handler.FroniusMeterRealtimeDataHandler;
import org.openhab.binding.fronius.handler.FroniusStorageRealtimeDataHandler;
import org.openhab.binding.fronius.handler.FroniusSymoBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FroniusHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gerrit Beine - Initial contribution
 */
public class FroniusHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(FroniusHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        logger.debug("Create Handler for Thing {}", thingTypeUID);

        if (thingTypeUID.equals(FRONIUS_SYMO_BRIDGE)) {
            return new FroniusSymoBridgeHandler((Bridge) thing);
        } else if (thingTypeUID.equals(ACTIVE_DEVICE_INFO_THING)) {
            return new FroniusActiveDeviceInfoHandler(thing);
        } else if (thingTypeUID.equals(INVERTER_REALTIME_DATA_THING)) {
            return new FroniusInverterRealtimeDataHandler(thing);
        } else if (thingTypeUID.equals(STORAGE_REALTIME_DATA_THING)) {
            return new FroniusStorageRealtimeDataHandler(thing);
        } else if (thingTypeUID.equals(METER_REALTIME_DATA_THING)) {
            return new FroniusMeterRealtimeDataHandler(thing);
        }

        logger.error("Thing not supported: {}", thingTypeUID);

        return null;
    }
}
