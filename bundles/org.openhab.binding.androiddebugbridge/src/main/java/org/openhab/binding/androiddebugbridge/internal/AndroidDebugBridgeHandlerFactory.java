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
package org.openhab.binding.androiddebugbridge.internal;

import static org.openhab.binding.androiddebugbridge.internal.AndroidDebugBridgeBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * The {@link AndroidDebugBridgeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.androiddebugbridge", service = ThingHandlerFactory.class)
public class AndroidDebugBridgeHandlerFactory extends BaseThingHandlerFactory {
    @Nullable
    private AndroidDebugBridgeDiscoveryService discoveryService;
    @Nullable
    private ServiceRegistration<?> discoveryServiceRegistration;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_ANDROID_DEVICE.equals(thingTypeUID)) {
            return new AndroidDebugBridgeHandler(thing, new AndroidDebugBridgeDevice());
        }
        return null;
    }

    private synchronized void registerDiscoveryService(AndroidDebugBridgeBindingConfiguration config) {
        AndroidDebugBridgeDiscoveryService androidADBDiscoveryService = discoveryService;
        if (androidADBDiscoveryService != null) {
            androidADBDiscoveryService.updateConfig(config);
        } else {
            androidADBDiscoveryService = new AndroidDebugBridgeDiscoveryService();
            androidADBDiscoveryService.updateConfig(config);
            discoveryService = androidADBDiscoveryService;
            discoveryServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                    androidADBDiscoveryService, null);
        }
    }

    private void unregisterDiscoveryService() {
        var adbDiscoveryServiceRegistration = discoveryServiceRegistration;
        if (adbDiscoveryServiceRegistration != null) {
            adbDiscoveryServiceRegistration.unregister();
            discoveryServiceRegistration = null;
        }
    }

    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> config) {
        super.activate(componentContext);
        modified(config);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        AndroidDebugBridgeBindingConfiguration bindingConfiguration = new Configuration(config)
                .as(AndroidDebugBridgeBindingConfiguration.class);
        if (bindingConfiguration != null) {
            if (discoveryService == null) {
                registerDiscoveryService(bindingConfiguration);
            } else {
                discoveryService.updateConfig(bindingConfiguration);
            }
        }
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        unregisterDiscoveryService();
        super.deactivate(componentContext);
    }
}
