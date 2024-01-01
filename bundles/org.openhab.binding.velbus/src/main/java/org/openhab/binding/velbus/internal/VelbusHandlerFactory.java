/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.velbus.internal;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velbus.internal.handler.VelbusBlindsHandler;
import org.openhab.binding.velbus.internal.handler.VelbusBridgeHandler;
import org.openhab.binding.velbus.internal.handler.VelbusDimmerHandler;
import org.openhab.binding.velbus.internal.handler.VelbusNetworkBridgeHandler;
import org.openhab.binding.velbus.internal.handler.VelbusRelayHandler;
import org.openhab.binding.velbus.internal.handler.VelbusRelayWithInputHandler;
import org.openhab.binding.velbus.internal.handler.VelbusSensorHandler;
import org.openhab.binding.velbus.internal.handler.VelbusSensorWithAlarmClockHandler;
import org.openhab.binding.velbus.internal.handler.VelbusSerialBridgeHandler;
import org.openhab.binding.velbus.internal.handler.VelbusVMB1TSHandler;
import org.openhab.binding.velbus.internal.handler.VelbusVMB4ANHandler;
import org.openhab.binding.velbus.internal.handler.VelbusVMB7INHandler;
import org.openhab.binding.velbus.internal.handler.VelbusVMBELHandler;
import org.openhab.binding.velbus.internal.handler.VelbusVMBELOHandler;
import org.openhab.binding.velbus.internal.handler.VelbusVMBGPHandler;
import org.openhab.binding.velbus.internal.handler.VelbusVMBGPOHandler;
import org.openhab.binding.velbus.internal.handler.VelbusVMBMeteoHandler;
import org.openhab.binding.velbus.internal.handler.VelbusVMBPIROHandler;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link VelbusHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.velbus")
public class VelbusHandlerFactory extends BaseThingHandlerFactory {
    private final SerialPortManager serialPortManager;

    @Activate
    public VelbusHandlerFactory(final @Reference SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID) || SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        ThingHandler thingHandler = null;

        if (BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID)) {
            VelbusBridgeHandler velbusBridgeHandler = thingTypeUID.equals(NETWORK_BRIDGE_THING_TYPE)
                    ? new VelbusNetworkBridgeHandler((Bridge) thing)
                    : new VelbusSerialBridgeHandler((Bridge) thing, serialPortManager);
            thingHandler = velbusBridgeHandler;
        } else if (VelbusRelayHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusRelayHandler(thing);
        } else if (VelbusRelayWithInputHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusRelayWithInputHandler(thing);
        } else if (VelbusDimmerHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusDimmerHandler(thing);
        } else if (VelbusBlindsHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusBlindsHandler(thing);
        } else if (VelbusSensorHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusSensorHandler(thing);
        } else if (VelbusSensorWithAlarmClockHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusSensorWithAlarmClockHandler(thing);
        } else if (VelbusVMBMeteoHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMBMeteoHandler(thing);
        } else if (VelbusVMBGPHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMBGPHandler(thing);
        } else if (VelbusVMBGPOHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMBGPOHandler(thing);
        } else if (VelbusVMBPIROHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMBPIROHandler(thing);
        } else if (VelbusVMB7INHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMB7INHandler(thing);
        } else if (VelbusVMBELHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMBELHandler(thing);
        } else if (VelbusVMBELOHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMBELOHandler(thing);
        } else if (VelbusVMB4ANHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMB4ANHandler(thing);
        } else if (VelbusVMB1TSHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMB1TSHandler(thing);
        }

        return thingHandler;
    }
}
