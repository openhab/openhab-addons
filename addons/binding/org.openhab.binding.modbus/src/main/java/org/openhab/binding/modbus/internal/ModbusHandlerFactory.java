/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus.internal;

import static org.openhab.binding.modbus.ModbusBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.modbus.handler.EndpointHandler;
import org.openhab.binding.modbus.handler.SerialHandler;
import org.openhab.binding.modbus.handler.SlaveHandler;
import org.openhab.binding.modbus.handler.TcpHandler;

import com.google.common.collect.Sets;

/**
 * The {@link ModbusHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author vores8 - Initial contribution
 */
public class ModbusHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.union(TcpHandler.SUPPORTED_THING_TYPES_UIDS,
            Sets.union(SerialHandler.SUPPORTED_THING_TYPES_UIDS,
                    Sets.union(SlaveHandler.SUPPORTED_THING_TYPES_UIDS, EndpointHandler.SUPPORTED_THING_TYPES_UIDS)));
    private static final Map<String, ThingHandler> modbusThingHandlers = new HashMap<String, ThingHandler>();
    private ThingHandler thingHandler = null;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_TCP)) {
            thingHandler = new TcpHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_TYPE_SERIAL)) {
            thingHandler = new SerialHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_TYPE_ENDPOINT)) {
            thingHandler = new EndpointHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SLAVE)) {
            thingHandler = new SlaveHandler((Bridge) thing);
        }
        if (thingHandler != null) {
            modbusThingHandlers.put(thing.getUID().toString(), thingHandler);
        }

        return thingHandler;
    }
}
