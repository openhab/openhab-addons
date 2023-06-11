/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.bluez.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.UID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link BlueZHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Connor Petty - Added DeviceManagerFactory
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.bluetooth.bluez")
public class BlueZHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(BlueZAdapterConstants.THING_TYPE_BLUEZ);

    private final Map<ThingUID, ServiceRegistration<?>> serviceRegs = new HashMap<>();

    private final DeviceManagerFactory deviceManagerFactory;

    @Activate
    public BlueZHandlerFactory(@Reference DeviceManagerFactory deviceManagerFactory) {
        this.deviceManagerFactory = deviceManagerFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(BlueZAdapterConstants.THING_TYPE_BLUEZ)) {
            BlueZBridgeHandler handler = new BlueZBridgeHandler((Bridge) thing, deviceManagerFactory);
            registerBluetoothAdapter(handler);
            return handler;
        } else {
            return null;
        }
    }

    private synchronized void registerBluetoothAdapter(BluetoothAdapter adapter) {
        this.serviceRegs.put(adapter.getUID(),
                bundleContext.registerService(BluetoothAdapter.class.getName(), adapter, new Hashtable<>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof BluetoothAdapter) {
            UID uid = ((BluetoothAdapter) thingHandler).getUID();
            ServiceRegistration<?> serviceReg = this.serviceRegs.remove(uid);
            if (serviceReg != null) {
                serviceReg.unregister();
            }
        }
    }
}
