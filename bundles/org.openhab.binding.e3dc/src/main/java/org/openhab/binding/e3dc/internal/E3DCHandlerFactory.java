/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.e3dc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.e3dc.internal.handler.E3DCInfoHandler;
import org.openhab.io.transport.modbus.ModbusManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link E3DCHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.e3dc", service = ThingHandlerFactory.class)
public class E3DCHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(E3DCHandlerFactory.class);
    private ModbusManager modbusManagerRef;

    /**
     * This factory needs a reference to the ModbusManager wich is provided
     * by the org.openhab.io.transport.modbus bundle. Please make
     * sure it's installed and enabled before using this bundle
     *
     * @param manager reference to the ModbusManager. We use this for modbus communication
     */
    @Activate
    public E3DCHandlerFactory(@Reference ModbusManager manager) {
        modbusManagerRef = manager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        if (thingTypeUID.equals(E3DCBindingConstants.THING_TYPE_E3DC_INFO)
                || thingTypeUID.equals(E3DCBindingConstants.THING_TYPE_E3DC_POWER)
                || thingTypeUID.equals(E3DCBindingConstants.THING_TYPE_E3DC_EMERGENCY_POWER)
                || thingTypeUID.equals(E3DCBindingConstants.THING_TYPE_E3DC_STRING_DETAILS)
                || thingTypeUID.equals(E3DCBindingConstants.THING_TYPE_E3DC_WALLBOX)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (E3DCBindingConstants.THING_TYPE_E3DC_INFO.equals(thingTypeUID)) {
            return new E3DCInfoHandler(thing, modbusManagerRef);
        }

        return null;
    }
}
