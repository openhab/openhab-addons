/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jsupla.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.jsupla.handler.JSuplaCloudBridgeHandler;
import org.openhab.binding.jsupla.handler.SuplaDeviceHandler;
import org.openhab.binding.jsupla.internal.discovery.JSuplaDiscoveryService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

import static org.openhab.binding.jsupla.JSuplaBindingConstants.JSUPLA_SERVER_TYPE;
import static org.openhab.binding.jsupla.JSuplaBindingConstants.SUPLA_DEVICE_TYPE;
import static org.openhab.binding.jsupla.JSuplaBindingConstants.SUPPORTED_THING_TYPES_UIDS;

/**
 * The {@link JSuplaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Grzeslowski - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.jsupla")
@NonNullByDefault
public class JSuplaHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(JSuplaHandlerFactory.class);
    private @Nullable SuplaDeviceRegistry suplaDeviceRegistry;
    
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        // it's done cause tycho-compile raises possible null error
        final @Nullable SuplaDeviceRegistry suplaDeviceRegistryNonNull = suplaDeviceRegistry;
        if (SUPLA_DEVICE_TYPE.equals(thingTypeUID)) {
            final SuplaDeviceHandler suplaDeviceHandler = new SuplaDeviceHandler(thing);
            if (suplaDeviceRegistryNonNull != null) {
                suplaDeviceRegistryNonNull.addSuplaDevice(suplaDeviceHandler);
            } else {
                throw new IllegalStateException("suplaDeviceRegistry is null!");
            }
            return suplaDeviceHandler;
        } else if (JSUPLA_SERVER_TYPE.equals(thingTypeUID)) {
            JSuplaCloudBridgeHandler bridgeHandler = new JSuplaCloudBridgeHandler((Bridge) thing, suplaDeviceRegistry);
            final JSuplaDiscoveryService discovery = registerThingDiscovery(bridgeHandler);
            bridgeHandler.setJSuplaDiscoveryService(discovery);
            return bridgeHandler;
        }

        return null;
    }

    @Reference
    public void setSuplaDeviceRegistry(final SuplaDeviceRegistry suplaDeviceRegistry) {
        this.suplaDeviceRegistry = suplaDeviceRegistry;
    }

    public void unsetSuplaDeviceRegistry(final SuplaDeviceRegistry suplaDeviceRegistry) {
        this.suplaDeviceRegistry = null;
    }

    private synchronized JSuplaDiscoveryService registerThingDiscovery(JSuplaCloudBridgeHandler bridgeHandler) {
        logger.trace("Try to register Discovery service on BundleID: {} Service: {}",
                bundleContext.getBundle().getBundleId(), DiscoveryService.class.getName());

        final JSuplaDiscoveryService discoveryService = new JSuplaDiscoveryService(bridgeHandler);
        bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>());
        return discoveryService;
    }
}
