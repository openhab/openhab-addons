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
package org.openhab.binding.warmup.internal.handler;

import static org.openhab.binding.warmup.internal.WarmupBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
import org.openhab.binding.warmup.internal.discovery.WarmupDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link WarmupHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author James Melville - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.warmup", service = ThingHandlerFactory.class)
public class WarmupHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClient httpClient;
    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Activate
    public WarmupHandlerFactory(@Reference final HttpClientFactory factory) {
        httpClient = factory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            MyWarmupAccountHandler bridge = new MyWarmupAccountHandler((Bridge) thing, httpClient);
            registerDiscoveryService(bridge);
            return bridge;
        } else if (THING_TYPE_ROOM.equals(thingTypeUID)) {
            return new RoomHandler(thing);
        }
        return null;
    }

    private synchronized void registerDiscoveryService(MyWarmupAccountHandler bridge) {
        WarmupDiscoveryService discoveryService = new WarmupDiscoveryService(bridge);
        this.discoveryServiceRegs.put(bridge.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
        bridge.setDiscoveryService(discoveryService);
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof MyWarmupAccountHandler) {
            if (this.discoveryServiceRegs.containsKey(thingHandler.getThing().getUID())) {
                ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
                serviceReg.unregister();
            }
        }
    }
}
