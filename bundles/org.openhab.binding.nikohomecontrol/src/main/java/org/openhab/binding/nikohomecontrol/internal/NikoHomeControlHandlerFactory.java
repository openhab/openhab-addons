/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal;

import static org.openhab.binding.nikohomecontrol.internal.NikoHomeControlBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.discovery.NikoHomeControlDiscoveryService;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlActionHandler;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlBridgeHandler;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlBridgeHandler1;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlBridgeHandler2;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlEnergyMeterHandler;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlThermostatHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link NikoHomeControlHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Mark Herwege - Initial Contribution
 */

@Component(service = ThingHandlerFactory.class, configurationPid = "binding.nikohomecontrol")
@NonNullByDefault
public class NikoHomeControlHandlerFactory extends BaseThingHandlerFactory {

    private @NonNullByDefault({}) NetworkAddressService networkAddressService;

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID) || BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (BRIDGE_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            NikoHomeControlBridgeHandler handler;
            if (BRIDGEII_THING_TYPE.equals(thing.getThingTypeUID())) {
                handler = new NikoHomeControlBridgeHandler2((Bridge) thing, networkAddressService);
            } else {
                handler = new NikoHomeControlBridgeHandler1((Bridge) thing);
            }
            registerNikoHomeControlDiscoveryService(handler);
            return handler;
        } else if (THING_TYPE_THERMOSTAT.equals(thing.getThingTypeUID())) {
            return new NikoHomeControlThermostatHandler(thing);
        } else if (THING_TYPE_ENERGYMETER.equals(thing.getThingTypeUID())) {
            return new NikoHomeControlEnergyMeterHandler(thing);
        } else if (ACTION_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            return new NikoHomeControlActionHandler(thing);
        }

        return null;
    }

    private synchronized void registerNikoHomeControlDiscoveryService(NikoHomeControlBridgeHandler bridgeHandler) {
        NikoHomeControlDiscoveryService nhcDiscoveryService = new NikoHomeControlDiscoveryService(bridgeHandler);
        discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), nhcDiscoveryService, new Hashtable<>()));
        nhcDiscoveryService.activate();
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof NikoHomeControlBridgeHandler) {
            ServiceRegistration<?> serviceReg = discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                NikoHomeControlDiscoveryService service = (NikoHomeControlDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                serviceReg.unregister();
                if (service != null) {
                    service.deactivate();
                }
            }
        }
    }

    @Reference
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
    }
}
