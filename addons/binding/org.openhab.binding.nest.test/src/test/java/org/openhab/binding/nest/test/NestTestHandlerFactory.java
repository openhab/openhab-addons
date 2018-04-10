/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.test;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.nest.handler.NestBridgeHandler;
import org.openhab.binding.nest.internal.discovery.NestDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link NestTestHandlerFactory} is responsible for creating test things and thing handlers.
 *
 * @author Wouter Born - Increase test coverage
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.nest.test")
public class NestTestHandlerFactory extends BaseThingHandlerFactory {

    private String redirectUrl = "http://localhost";

    private Map<ThingUID, ServiceRegistration<?>> discoveryService = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return NestTestBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(NestTestBridgeHandler.THING_TYPE_TEST_BRIDGE)) {
            NestTestBridgeHandler handler = new NestTestBridgeHandler((Bridge) thing, redirectUrl);
            NestDiscoveryService service = new NestDiscoveryService(handler);
            // Register the discovery service.
            discoveryService.put(handler.getThing().getUID(),
                    bundleContext.registerService(DiscoveryService.class.getName(), service, new Hashtable<>()));

            return handler;
        }
        return null;
    }

    /**
     * Removes the handler for the specific thing. This also handles disabling the discovery
     * service when the bridge is removed.
     */
    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof NestBridgeHandler) {
            ServiceRegistration<?> reg = discoveryService.get(thingHandler.getThing().getUID());
            if (reg != null) {
                // Unregister the discovery service.
                NestDiscoveryService service = (NestDiscoveryService) bundleContext.getService(reg.getReference());
                service.deactivate();
                reg.unregister();
                discoveryService.remove(thingHandler.getThing().getUID());
            }
        }
        super.removeHandler(thingHandler);
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
