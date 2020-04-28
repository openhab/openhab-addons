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
package org.openhab.binding.evohome.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.evohome.internal.discovery.EvohomeDiscoveryService;
import org.openhab.binding.evohome.internal.handler.EvohomeAccountBridgeHandler;
import org.openhab.binding.evohome.internal.handler.EvohomeHeatingZoneHandler;
import org.openhab.binding.evohome.internal.handler.EvohomeTemperatureControlSystemHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides the thing factory for this binding
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */

@Component(service = ThingHandlerFactory.class, configurationPid = "binding.evohome")
public class EvohomeHandlerFactory extends BaseThingHandlerFactory {

    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private HttpClient httpClient;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return EvohomeBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(EvohomeBindingConstants.THING_TYPE_EVOHOME_ACCOUNT)) {
            EvohomeAccountBridgeHandler bridge = new EvohomeAccountBridgeHandler((Bridge) thing, httpClient);
            registerEvohomeDiscoveryService(bridge);
            return bridge;
        } else if (thingTypeUID.equals(EvohomeBindingConstants.THING_TYPE_EVOHOME_DISPLAY)) {
            return new EvohomeTemperatureControlSystemHandler(thing);
        } else if (thingTypeUID.equals(EvohomeBindingConstants.THING_TYPE_EVOHOME_HEATING_ZONE)) {
            return new EvohomeHeatingZoneHandler(thing);
        }

        return null;
    }

    private void registerEvohomeDiscoveryService(EvohomeAccountBridgeHandler evohomeBridgeHandler) {
        EvohomeDiscoveryService discoveryService = new EvohomeDiscoveryService(evohomeBridgeHandler);

        this.discoveryServiceRegs.put(evohomeBridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    @Override
    public ThingHandler registerHandler(Thing thing) {
        return super.registerHandler(thing);
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof EvohomeAccountBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                EvohomeDiscoveryService service = (EvohomeDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                if (service != null) {
                    service.deactivate();
                }
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }
}
