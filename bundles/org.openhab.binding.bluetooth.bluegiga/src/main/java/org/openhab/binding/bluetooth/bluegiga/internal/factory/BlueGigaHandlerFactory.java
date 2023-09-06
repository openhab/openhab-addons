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
package org.openhab.binding.bluetooth.bluegiga.internal.factory;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.bluegiga.BlueGigaAdapterConstants;
import org.openhab.binding.bluetooth.bluegiga.handler.BlueGigaBridgeHandler;
import org.openhab.core.io.transport.serial.SerialPortManager;
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
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link BlueGigaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Chris Jackson - Initial contribution
 * @author Kai Kreuzer - added support for adapter service registration
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.bluegiga")
public class BlueGigaHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set
            .of(BlueGigaAdapterConstants.THING_TYPE_BLUEGIGA);

    private final Map<ThingUID, ServiceRegistration<?>> serviceRegs = new HashMap<>();

    private Optional<SerialPortManager> serialPortManager = Optional.empty();

    @Reference
    protected void setSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = Optional.of(serialPortManager);
    }

    protected void unsetSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = Optional.empty();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    @Nullable
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(BlueGigaAdapterConstants.THING_TYPE_BLUEGIGA)) {
            BlueGigaBridgeHandler handler = new BlueGigaBridgeHandler((Bridge) thing, serialPortManager.get());
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
        if (thingHandler instanceof BluetoothAdapter bluetoothAdapter) {
            UID uid = bluetoothAdapter.getUID();
            ServiceRegistration<?> serviceReg = this.serviceRegs.remove(uid);
            if (serviceReg != null) {
                serviceReg.unregister();
            }
        }
    }
}
