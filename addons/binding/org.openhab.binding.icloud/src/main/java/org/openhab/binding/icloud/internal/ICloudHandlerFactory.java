/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal;

import static org.openhab.binding.icloud.BindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.icloud.handler.ICloudAccountBridgeHandler;
import org.openhab.binding.icloud.handler.ICloudDeviceHandler;
import org.openhab.binding.icloud.internal.discovery.DeviceDiscovery;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link ICloudHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.icloud")
public class ICloudHandlerFactory extends BaseThingHandlerFactory {
    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegistrations = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ICLOUD)) {
            ICloudAccountBridgeHandler bridgeHandler = new ICloudAccountBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(bridgeHandler);
            return bridgeHandler;
        }

        if (thingTypeUID.equals(THING_TYPE_ICLOUDDEVICE)) {
            return new ICloudDeviceHandler(thing);
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof ICloudAccountBridgeHandler) {
            unregisterDeviceDiscoveryService((ICloudAccountBridgeHandler) thingHandler);
        }
    }

    private synchronized void registerDeviceDiscoveryService(ICloudAccountBridgeHandler bridgeHandler) {
        DeviceDiscovery discoveryService = new DeviceDiscovery(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegistrations.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    private synchronized void unregisterDeviceDiscoveryService(ICloudAccountBridgeHandler bridgeHandler) {
        ServiceRegistration<?> serviceRegistration = this.discoveryServiceRegistrations
                .get(bridgeHandler.getThing().getUID());
        if (serviceRegistration != null) {
            DeviceDiscovery discoveryService = (DeviceDiscovery) bundleContext
                    .getService(serviceRegistration.getReference());
            if (discoveryService != null) {
                discoveryService.deactivate();
            }
            serviceRegistration.unregister();
            discoveryServiceRegistrations.remove(bridgeHandler.getThing().getUID());
        }
    }
}
