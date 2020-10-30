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
package org.openhab.binding.remoteopenhab.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.remoteopenhab.internal.discovery.RemoteopenhabDiscoveryService;
import org.openhab.binding.remoteopenhab.internal.handler.RemoteopenhabBridgeHandler;
import org.openhab.binding.remoteopenhab.internal.handler.RemoteopenhabThingHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryService;
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
import org.osgi.service.jaxrs.client.SseEventSourceFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link RemoteopenhabHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.remoteopenhab")
public class RemoteopenhabHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(RemoteopenhabBindingConstants.SUPPORTED_BRIDGE_TYPES_UIDS.stream(),
                    RemoteopenhabBindingConstants.SUPPORTED_THING_TYPES_UIDS.stream())
            .collect(Collectors.toSet());

    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;
    private final RemoteopenhabChannelTypeProvider channelTypeProvider;
    private final RemoteopenhabStateDescriptionOptionProvider stateDescriptionProvider;
    private final Gson jsonParser;

    @Activate
    public RemoteopenhabHandlerFactory(final @Reference ClientBuilder clientBuilder,
            final @Reference SseEventSourceFactory eventSourceFactory,
            final @Reference RemoteopenhabChannelTypeProvider channelTypeProvider,
            final @Reference RemoteopenhabStateDescriptionOptionProvider stateDescriptionProvider) {
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
        this.channelTypeProvider = channelTypeProvider;
        this.stateDescriptionProvider = stateDescriptionProvider;
        jsonParser = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
    }

    /**
     * The things this factory supports creating.
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (thingTypeUID.equals(RemoteopenhabBindingConstants.BRIDGE_TYPE_SERVER)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else if (RemoteopenhabBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            ThingUID newThingUID;
            if (bridgeUID != null && thingUID != null) {
                newThingUID = new ThingUID(thingTypeUID, bridgeUID, thingUID.getId());
            } else {
                newThingUID = thingUID;
            }
            return super.createThing(thingTypeUID, configuration, newThingUID, bridgeUID);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the remote openHAB binding.");
    }

    /**
     * Creates a handler for the specific thing.
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(RemoteopenhabBindingConstants.BRIDGE_TYPE_SERVER)) {
            RemoteopenhabBridgeHandler handler = new RemoteopenhabBridgeHandler((Bridge) thing, clientBuilder,
                    eventSourceFactory, channelTypeProvider, stateDescriptionProvider, jsonParser);
            registerDiscoveryService(handler);
            return handler;
        } else if (RemoteopenhabBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            RemoteopenhabThingHandler handler = new RemoteopenhabThingHandler(thing);
            return handler;
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof RemoteopenhabBridgeHandler) {
            unregisterDiscoveryService(thingHandler.getThing());
        }
    }

    private synchronized void registerDiscoveryService(RemoteopenhabBridgeHandler bridgeHandler) {
        RemoteopenhabDiscoveryService discoveryService = new RemoteopenhabDiscoveryService(bridgeHandler);
        discoveryService.activate(null);
        discoveryServiceRegs.put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    private synchronized void unregisterDiscoveryService(Thing thing) {
        ServiceRegistration<?> serviceReg = discoveryServiceRegs.remove(thing.getUID());
        if (serviceReg != null) {
            // remove discovery service, if bridge handler is removed
            RemoteopenhabDiscoveryService service = (RemoteopenhabDiscoveryService) bundleContext
                    .getService(serviceReg.getReference());
            serviceReg.unregister();
            if (service != null) {
                service.deactivate();
            }
        }
    }
}
