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
package org.openhab.binding.bluetooth.roaming.internal;

import static org.openhab.binding.bluetooth.roaming.internal.RoamingBindingConstants.THING_TYPE_ROAMING;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.UID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link RoamingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.roaming", service = ThingHandlerFactory.class)
public class RoamingHandlerFactory extends BaseThingHandlerFactory {

    private final Map<ThingUID, ServiceRegistration<?>> serviceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return RoamingBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ROAMING.equals(thingTypeUID)) {
            RoamingBridgeHandler handler = new RoamingBridgeHandler((Bridge) thing);
            registerRoamingBluetoothAdapter(handler);
            return handler;
        }

        return null;
    }

    private synchronized void registerRoamingBluetoothAdapter(RoamingBluetoothAdapter adapter) {
        this.serviceRegs.put(adapter.getUID(),
                bundleContext.registerService(RoamingBluetoothAdapter.class.getName(), adapter, new Hashtable<>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof RoamingBluetoothAdapter bluetoothAdapter) {
            UID uid = bluetoothAdapter.getUID();
            ServiceRegistration<?> serviceReg = this.serviceRegs.remove(uid);
            if (serviceReg != null) {
                serviceReg.unregister();
            }
        }
    }
}
