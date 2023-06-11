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
package org.openhab.binding.millheat.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.millheat.internal.discovery.MillheatDiscoveryService;
import org.openhab.binding.millheat.internal.handler.MillheatAccountHandler;
import org.openhab.binding.millheat.internal.handler.MillheatHeaterHandler;
import org.openhab.binding.millheat.internal.handler.MillheatHomeHandler;
import org.openhab.binding.millheat.internal.handler.MillheatRoomHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MillheatHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.millheat", service = ThingHandlerFactory.class)
public class MillheatHandlerFactory extends BaseThingHandlerFactory {
    private HttpClient httpClient;
    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream
            .of(MillheatBindingConstants.THING_TYPE_ACCOUNT, MillheatBindingConstants.THING_TYPE_HEATER,
                    MillheatBindingConstants.THING_TYPE_ROOM, MillheatBindingConstants.THING_TYPE_HOME)
            .collect(Collectors.toSet()));

    @Activate
    public MillheatHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (MillheatBindingConstants.THING_TYPE_HEATER.equals(thingTypeUID)) {
            return new MillheatHeaterHandler(thing);
        } else if (MillheatBindingConstants.THING_TYPE_ROOM.equals(thingTypeUID)) {
            return new MillheatRoomHandler(thing);
        } else if (MillheatBindingConstants.THING_TYPE_HOME.equals(thingTypeUID)) {
            return new MillheatHomeHandler(thing);
        } else if (MillheatBindingConstants.THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            final MillheatAccountHandler handler = new MillheatAccountHandler((Bridge) thing, httpClient,
                    bundleContext);
            registerDeviceDiscoveryService(handler);
            return handler;
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof MillheatAccountHandler) {
            ThingUID thingUID = thingHandler.getThing().getUID();
            unregisterDeviceDiscoveryService(thingUID);
        }
        super.removeHandler(thingHandler);
    }

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    private void registerDeviceDiscoveryService(MillheatAccountHandler bridgeHandler) {
        MillheatDiscoveryService discoveryService = new MillheatDiscoveryService(bridgeHandler);
        discoveryServiceRegs.put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    private void unregisterDeviceDiscoveryService(ThingUID thingUID) {
        if (discoveryServiceRegs.containsKey(thingUID)) {
            ServiceRegistration<?> serviceReg = discoveryServiceRegs.get(thingUID);
            serviceReg.unregister();
            discoveryServiceRegs.remove(thingUID);
        }
    }
}
