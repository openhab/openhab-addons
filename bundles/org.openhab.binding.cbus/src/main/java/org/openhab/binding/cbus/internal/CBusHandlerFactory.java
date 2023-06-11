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
package org.openhab.binding.cbus.internal;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cbus.CBusBindingConstants;
import org.openhab.binding.cbus.handler.CBusCGateHandler;
import org.openhab.binding.cbus.handler.CBusDaliHandler;
import org.openhab.binding.cbus.handler.CBusLightHandler;
import org.openhab.binding.cbus.handler.CBusNetworkHandler;
import org.openhab.binding.cbus.handler.CBusTemperatureHandler;
import org.openhab.binding.cbus.handler.CBusTriggerHandler;
import org.openhab.binding.cbus.internal.discovery.CBusGroupDiscovery;
import org.openhab.binding.cbus.internal.discovery.CBusNetworkDiscovery;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link CBusHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Scott Linton - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.cbus")
public class CBusHandlerFactory extends BaseThingHandlerFactory {

    private @Nullable ServiceRegistration<?> cbusCGateHandlerServiceReg = null;
    private @Nullable ServiceRegistration<?> cbusNetworkHandlerServiceReg = null;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return CBusBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (CBusBindingConstants.BRIDGE_TYPE_CGATE.equals(thingTypeUID)) {
            CBusCGateHandler handler = new CBusCGateHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        }
        if (CBusBindingConstants.BRIDGE_TYPE_NETWORK.equals(thingTypeUID)) {
            CBusNetworkHandler handler = new CBusNetworkHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        }
        if (CBusBindingConstants.THING_TYPE_LIGHT.equals(thingTypeUID)) {
            return new CBusLightHandler(thing);
        }
        if (CBusBindingConstants.THING_TYPE_TEMPERATURE.equals(thingTypeUID)) {
            return new CBusTemperatureHandler(thing);
        }
        if (CBusBindingConstants.THING_TYPE_TRIGGER.equals(thingTypeUID)) {
            return new CBusTriggerHandler(thing);
        }
        if (CBusBindingConstants.THING_TYPE_DALI.equals(thingTypeUID)) {
            return new CBusDaliHandler(thing);
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof CBusCGateHandler) {
            ServiceRegistration<?> serviceReg = this.cbusCGateHandlerServiceReg;
            if (serviceReg != null) {
                serviceReg.unregister();
            }
        } else if (thingHandler instanceof CBusNetworkHandler) {
            ServiceRegistration<?> serviceReg = this.cbusNetworkHandlerServiceReg;
            if (serviceReg != null) {
                serviceReg.unregister();
            }
        }
        super.removeHandler(thingHandler);
    }

    private void registerDeviceDiscoveryService(CBusCGateHandler cbusCgateHandler) {
        CBusNetworkDiscovery discoveryService = new CBusNetworkDiscovery(cbusCgateHandler);
        cbusCGateHandlerServiceReg = super.bundleContext.registerService(DiscoveryService.class.getName(),
                discoveryService, new Hashtable<String, Object>());
    }

    private void registerDeviceDiscoveryService(CBusNetworkHandler cbusNetworkHandler) {
        CBusGroupDiscovery discoveryService = new CBusGroupDiscovery(cbusNetworkHandler);
        cbusNetworkHandlerServiceReg = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
    }
}
